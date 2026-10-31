package com.myfitness.workout_service.service;

import com.myfitness.workout_service.client.CatalogClient;
import com.myfitness.workout_service.dto.routine.*;
import com.myfitness.workout_service.entity.routine.RoutineEntity;
import com.myfitness.workout_service.entity.routine.RoutineExerciseEntity;
import com.myfitness.workout_service.exception.ResourceNotFoundException;
import com.myfitness.workout_service.exception.ServiceUnavailableException;
import com.myfitness.workout_service.mapper.RoutineMapper;
import com.myfitness.workout_service.repository.RoutineRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final CatalogClient catalogClient;
    private final RoutineMapper routineMapper;

    //CREATE (PROTECTED WITH CIRCUIT BREAKER)
    @CircuitBreaker(name = "catalogCircuitBreaker", fallbackMethod = "createRoutineFallback")
    public RoutineResponse createRoutine(String userEmail, String userName, CreateRoutineRequest routineRequest) {
        List<RoutineExerciseItemDto> items = routineRequest.routineExerciseItems();

        if (items != null && !items.isEmpty()) {
            List<UUID> exerciseIds = items.stream()
                    .map(RoutineExerciseItemDto::exerciseId)
                    .toList();

            boolean allValid = catalogClient.validateExercises(exerciseIds);
            if (!allValid) {
                throw new IllegalArgumentException("Error: One or more provided exercise IDs do not exist in the catalog.");
            }
        }

        RoutineEntity routineEntity = RoutineEntity.builder()
                .userEmail(userEmail)
                .name(routineRequest.name())
                .routinePrivate(routineRequest.routinePrivate())
                .createdBy(userName)
                .build();

        if (items != null) {
            for (RoutineExerciseItemDto item : items) {
                RoutineExerciseEntity routineExercise = RoutineExerciseEntity.builder()
                        .exerciseId(item.exerciseId())
                        .targetSets(item.targetSets())
                        .targetReps(item.targetReps())
                        .restTimeSeconds(item.restTimeSeconds())
                        .orderIndex(item.orderIndex())
                        .build();
                routineEntity.addExercise(routineExercise);
            }
        }

        RoutineEntity savedEntity = routineRepository.save(routineEntity);

        return routineMapper.toRecord(savedEntity, userEmail);
    }

    //
    //CREATE FALLBACK
    //
    @SuppressWarnings("unused")
    public RoutineResponse createRoutineFallback(String userEmail, String userName, CreateRoutineRequest routineRequest, Throwable t) {
        log.error("Circuit Breaker triggered at createRoutine. Cause: {}", t.getMessage());
        throw new ServiceUnavailableException("Exercises' catalog's not available at the moment. Please try again later.");
    }

    //UPDATE (PROTECTED WITH CIRCUIT BREAKER)
    @CircuitBreaker(name = "catalogCircuitBreaker", fallbackMethod = "updateRoutineFallback")
    @Transactional
    public RoutineResponse updateRoutine(String userEmail, UUID id, UpdateRoutineRequest routineRequest) {
        RoutineEntity foundEntity = routineRepository.findRoutineByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Routine with id '" + id + "' not found"));

        List<RoutineExerciseItemDto> items = routineRequest.routineExerciseItems();

        if (items != null && !items.isEmpty()) {
            List<UUID> exerciseIds = items.stream()
                    .map(RoutineExerciseItemDto::exerciseId)
                    .toList();

            boolean allValid = catalogClient.validateExercises(exerciseIds);
            if (!allValid) {
                throw new IllegalArgumentException("Error: One or more provided exercise IDs do not exist in the catalog.");
            }
        }

        foundEntity.setName(routineRequest.name());
        foundEntity.setRoutinePrivate(routineRequest.routinePrivate());

        foundEntity.clearExercises();
        if (items != null) {
            for (RoutineExerciseItemDto item : items) {
                RoutineExerciseEntity routineExercise = RoutineExerciseEntity.builder()
                        .exerciseId(item.exerciseId())
                        .targetSets(item.targetSets())
                        .targetReps(item.targetReps())
                        .restTimeSeconds(item.restTimeSeconds())
                        .orderIndex(item.orderIndex())
                        .build();
                foundEntity.addExercise(routineExercise);
            }
        }

        RoutineEntity updatedRoutine = routineRepository.save(foundEntity);

        return routineMapper.toRecord(updatedRoutine, foundEntity.getUserEmail());
    }

    //
    //UPDATE FALLBACK
    //
    @SuppressWarnings("unused")
    public RoutineResponse updateRoutineFallback(String userEmail, UUID id, UpdateRoutineRequest routineRequest, Throwable t) {
        log.error("Circuit Breaker triggered at updateRoutine. Cause: {}", t.getMessage());
        throw new ServiceUnavailableException("Exercises' catalog's not available at the moment. Please try again later.");
    }

    //DELETE
    @Transactional
    public boolean deleteRoutine(String userEmail, UUID id) {
        RoutineEntity routine = routineRepository.findRoutineByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Routine with id '" + id + "' not found"));
        routineRepository.delete(routine);
        return true;
    }

    // GET ALL ACCESSIBLE (PAGINATED & SEARCHABLE)
    public Page<RoutineResponse> getAllAccessibleRoutines(String userEmail, String search, boolean mineOnly, Pageable pageable) {
        Page<RoutineEntity> entities;

        if (mineOnly) {
            entities = routineRepository.findUserRoutinesWithSearch(userEmail, search, pageable);
        } else {
            entities = routineRepository.findAccessibleRoutinesWithSearch(userEmail, search, pageable);
        }

        return entities.map(entity -> routineMapper.toRecord(entity, userEmail));
    }

    //GET BY ID ENRICHED (CIRCUIT BREAKER)
    @CircuitBreaker(name = "catalogCircuitBreaker", fallbackMethod = "getRoutineByIdFallback")
    public RoutineDetailResponse getRoutineById(UUID id, String userEmail) {
        RoutineEntity routine = routineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Routine not found."));

        boolean isOwner = routine.getUserEmail().equals(userEmail);

        if (routine.isRoutinePrivate() && !isOwner) {
            throw new ResourceNotFoundException("Routine not found or you don't have access.");
        }

        List<EnrichedRoutineExerciseDto> enrichedExercises = new ArrayList<>();

        if (routine.getRoutineExerciseEntities() != null && !routine.getRoutineExerciseEntities().isEmpty()) {
            List<UUID> exerciseIds = routine.getRoutineExerciseEntities().stream()
                    .map(RoutineExerciseEntity::getExerciseId)
                    .toList();

            List<ExerciseSummaryDto> catalogExercises = catalogClient.getExercisesByIds(userEmail, exerciseIds);
            Map<UUID, ExerciseSummaryDto> catalogMap = catalogExercises.stream()
                    .collect(Collectors.toMap(ExerciseSummaryDto::id, ex -> ex));

            for (RoutineExerciseEntity we : routine.getRoutineExerciseEntities()) {
                ExerciseSummaryDto summary = catalogMap.get(we.getExerciseId());
                String name = summary != null ? summary.name() : "Unknown";
                String description = summary != null ? summary.description() : "";
                String muscleGroup = summary != null ? summary.muscleGroup() : "UNKNOWN";

                enrichedExercises.add(new EnrichedRoutineExerciseDto(
                        we.getExerciseId(),
                        name,
                        description,
                        muscleGroup,
                        we.getTargetSets(),
                        we.getTargetReps(),
                        we.getRestTimeSeconds(),
                        we.getOrderIndex()
                ));
            }
        }

        return new RoutineDetailResponse(
                routine.getId(),
                routine.getName(),
                routine.isRoutinePrivate(),
                routine.getCreatedBy(),
                routine.getCreatedAt(),
                isOwner,
                enrichedExercises
        );
    }

    //
    //GET BY ID FALLBACK
    //
    @SuppressWarnings("unused")
    public RoutineDetailResponse getRoutineByIdFallback(UUID id, String userEmail, Throwable t) {
        log.warn("Catalog down when consulting routine '{}'. Returning degraded view. Cause: {}", id, t.getMessage());

        RoutineEntity foundEntity = routineRepository.findRoutineByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Routine with id '" + id + "' not found."));

        List<EnrichedRoutineExerciseDto> fallbackExercises = foundEntity.getRoutineExerciseEntities().stream()
                .map(we -> new EnrichedRoutineExerciseDto(
                        we.getExerciseId(),
                        "Details not available",
                        "Catalog service is under maintenance",
                        "UNKNOWN",
                        we.getTargetSets(),
                        we.getTargetReps(),
                        we.getRestTimeSeconds(),
                        we.getOrderIndex()
                )).toList();

        return new RoutineDetailResponse(
                foundEntity.getId(),
                foundEntity.getName(),
                foundEntity.isRoutinePrivate(),
                foundEntity.getCreatedBy(),
                foundEntity.getCreatedAt(),
                true,
                fallbackExercises
        );
    }
}