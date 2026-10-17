package com.myfitness.workout_service.service;

import com.myfitness.workout_service.client.CatalogClient;
import com.myfitness.workout_service.dto.*;
import com.myfitness.workout_service.entity.WorkoutEntity;
import com.myfitness.workout_service.entity.WorkoutExerciseEntity;
import com.myfitness.workout_service.exception.ResourceNotFoundException;
import com.myfitness.workout_service.exception.ServiceUnavailableException;
import com.myfitness.workout_service.mapper.WorkoutMapper;
import com.myfitness.workout_service.repository.WorkoutRepository;
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
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final CatalogClient catalogClient;
    private final WorkoutMapper workoutMapper;

    //CREATE (PROTECTED WITH CIRCUIT BREAKER)
    @CircuitBreaker(name = "catalogCircuitBreaker", fallbackMethod = "createWorkoutFallback")
    public WorkoutResponse createWorkout(String userEmail, String userName, CreateWorkoutRequest workoutRequest) {
        List<WorkoutExerciseItemDto> items = workoutRequest.workoutExerciseItems();

        if (items != null && !items.isEmpty()) {
            List<UUID> exerciseIds = items.stream()
                    .map(WorkoutExerciseItemDto::exerciseId)
                    .toList();

            boolean allValid = catalogClient.validateExercises(exerciseIds);
            if (!allValid) {
                throw new IllegalArgumentException("Error: One or more provided exercise IDs do not exist in the catalog.");
            }
        }

        WorkoutEntity workoutEntity = WorkoutEntity.builder()
                .userEmail(userEmail)
                .name(workoutRequest.name())
                .workoutPrivate(workoutRequest.workoutPrivate())
                .createdBy(userName)
                .build();

        if (items != null) {
            for (WorkoutExerciseItemDto item : items) {
                WorkoutExerciseEntity workoutExercise = WorkoutExerciseEntity.builder()
                        .exerciseId(item.exerciseId())
                        .targetSets(item.targetSets())
                        .targetReps(item.targetReps())
                        .restTimeSeconds(item.restTimeSeconds())
                        .orderIndex(item.orderIndex())
                        .build();
                workoutEntity.addExercise(workoutExercise);
            }
        }

        WorkoutEntity savedEntity = workoutRepository.save(workoutEntity);

        return workoutMapper.toRecord(savedEntity, userEmail);
    }

    //
    //CREATE FALLBACK
    //
    public WorkoutResponse createWorkoutFallback(String userEmail, String userName, CreateWorkoutRequest workoutRequest, Throwable t) {
        log.error("Circuit Breaker triggered at createWorkout. Cause: {}", t.getMessage());
        throw new ServiceUnavailableException("Exercises' catalog's not available at the moment. Please try again later.");
    }

    //UPDATE (PROTECTED WITH CIRCUIT BREAKER)
    @CircuitBreaker(name = "catalogCircuitBreaker", fallbackMethod = "updateWorkoutFallback")
    @Transactional
    public WorkoutResponse updateWorkout(String userEmail, UUID id, UpdateWorkoutRequest workoutRequest) {
        WorkoutEntity foundEntity = workoutRepository.findWorkoutByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Workout with id '" + id + "' not found"));

        List<WorkoutExerciseItemDto> items = workoutRequest.workoutExerciseItems();

        if (items != null && !items.isEmpty()) {
            List<UUID> exerciseIds = items.stream()
                    .map(WorkoutExerciseItemDto::exerciseId)
                    .toList();

            boolean allValid = catalogClient.validateExercises(exerciseIds);
            if (!allValid) {
                throw new IllegalArgumentException("Error: One or more provided exercise IDs do not exist in the catalog.");
            }
        }

        foundEntity.setName(workoutRequest.name());
        foundEntity.setWorkoutPrivate(workoutRequest.workoutPrivate());

        foundEntity.clearExercises();
        if (items != null) {
            for (WorkoutExerciseItemDto item : items) {
                WorkoutExerciseEntity workoutExercise = WorkoutExerciseEntity.builder()
                        .exerciseId(item.exerciseId())
                        .targetSets(item.targetSets())
                        .targetReps(item.targetReps())
                        .restTimeSeconds(item.restTimeSeconds())
                        .orderIndex(item.orderIndex())
                        .build();
                foundEntity.addExercise(workoutExercise);
            }
        }

        WorkoutEntity updatedWorkout = workoutRepository.save(foundEntity);

        return workoutMapper.toRecord(updatedWorkout, foundEntity.getUserEmail());
    }

    //
    //UPDATE FALLBACK
    //
    public WorkoutResponse updateWorkoutFallback(String userEmail, UUID id, UpdateWorkoutRequest workoutRequest, Throwable t) {
        log.error("Circuit Breaker triggered at updateWorkout. Cause: {}", t.getMessage());
        throw new ServiceUnavailableException("Exercises' catalog's not available at the moment. Please try again later.");
    }

    //DELETE
    @Transactional
    public boolean deleteWorkout(String userEmail, UUID id) {
        int rowsAffected = workoutRepository.deleteWorkoutByIdAndUserEmail(userEmail, id);
        if (rowsAffected == 0)
            throw new ResourceNotFoundException("Workout with id '" + id + "' not found");
        return rowsAffected > 0;
    }

    // GET ALL ACCESSIBLE (PAGINATED & SEARCHABLE)
    public Page<WorkoutResponse> getAllAccessibleWorkouts(String userEmail, String search, boolean mineOnly, Pageable pageable) {
        Page<WorkoutEntity> entities;

        if (mineOnly) {
            entities = workoutRepository.findUserWorkoutsWithSearch(userEmail, search, pageable);
        } else {
            entities = workoutRepository.findAccessibleWorkoutsWithSearch(userEmail, search, pageable);
        }

        return entities.map(entity -> workoutMapper.toRecord(entity, userEmail));
    }

    //GET BY ID ENRICHED (CIRCUIT BREAKER)
    @CircuitBreaker(name = "catalogCircuitBreaker", fallbackMethod = "getWorkoutByIdFallback")
    public WorkoutDetailResponse getWorkoutById(UUID id, String userEmail) {
        WorkoutEntity workout = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found."));

        boolean isOwner = workout.getUserEmail().equals(userEmail);

        if (workout.isWorkoutPrivate() && !isOwner) {
            throw new ResourceNotFoundException("Workout not found or you don't have access.");
        }

        List<EnrichedWorkoutExerciseDto> enrichedExercises = new ArrayList<>();

        if (workout.getWorkoutExerciseEntities() != null && !workout.getWorkoutExerciseEntities().isEmpty()) {
            List<UUID> exerciseIds = workout.getWorkoutExerciseEntities().stream()
                    .map(WorkoutExerciseEntity::getExerciseId)
                    .toList();

            List<ExerciseSummaryDto> catalogExercises = catalogClient.getExercisesByIds(userEmail, exerciseIds);
            Map<UUID, ExerciseSummaryDto> catalogMap = catalogExercises.stream()
                    .collect(Collectors.toMap(ExerciseSummaryDto::id, ex -> ex));

            for (WorkoutExerciseEntity we : workout.getWorkoutExerciseEntities()) {
                ExerciseSummaryDto summary = catalogMap.get(we.getExerciseId());
                String name = summary != null ? summary.name() : "Unknown";
                String description = summary != null ? summary.description() : "";
                String muscleGroup = summary != null ? summary.muscleGroup() : "UNKNOWN";

                enrichedExercises.add(new EnrichedWorkoutExerciseDto(
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

        return new WorkoutDetailResponse(
                workout.getId(),
                workout.getName(),
                workout.isWorkoutPrivate(),
                workout.getCreatedBy(),
                workout.getCreatedAt(),
                isOwner,
                enrichedExercises
        );
    }

    //
    //GET BY ID FALLBACK
    //
    public WorkoutDetailResponse getWorkoutByIdFallback(UUID id, String userEmail, Throwable t) {
        log.warn("Catalog down when consulting workout '{}'. Returning degraded view. Cause: {}", id, t.getMessage());

        WorkoutEntity foundEntity = workoutRepository.findWorkoutByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Workout with id '" + id + "' not found."));

        List<EnrichedWorkoutExerciseDto> fallbackExercises = foundEntity.getWorkoutExerciseEntities().stream()
                .map(we -> new EnrichedWorkoutExerciseDto(
                        we.getExerciseId(),
                        "Details not available",
                        "Catalog service is under maintenance",
                        "UNKNOWN",
                        we.getTargetSets(),
                        we.getTargetReps(),
                        we.getRestTimeSeconds(),
                        we.getOrderIndex()
                )).toList();

        return new WorkoutDetailResponse(
                foundEntity.getId(),
                foundEntity.getName(),
                foundEntity.isWorkoutPrivate(),
                foundEntity.getCreatedBy(),
                foundEntity.getCreatedAt(),
                true,
                fallbackExercises
        );
    }
}