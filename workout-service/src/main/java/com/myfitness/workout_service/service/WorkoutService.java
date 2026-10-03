package com.myfitness.workout_service.service;

import com.myfitness.workout_service.client.CatalogClient;
import com.myfitness.workout_service.dto.*;
import com.myfitness.workout_service.entity.WorkoutEntity;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final CatalogClient catalogClient;
    private final WorkoutMapper workoutMapper;

    //CREATE (PROTECTED WITH CIRCUIT BREAKER)
    @CircuitBreaker(name = "catalogCircuitBreaker", fallbackMethod = "createWorkoutFallback")
    public WorkoutResponse createWorkout(String userEmail, CreateWorkoutRequest workoutRequest) {
        //batching validation
        boolean allValid = catalogClient.validateExercises(workoutRequest.exerciseIds());
        if (!allValid) throw new IllegalArgumentException
                ("Error: One or more provided exercise IDs do not exist in the catalog.");

        //if every exercise is valid, proceed to build and save the routine attached to the user's email
        WorkoutEntity workoutEntity = WorkoutEntity.builder()
                .userEmail(userEmail)
                .name(workoutRequest.name())
                .exerciseIds(workoutRequest.exerciseIds())
                .build();

        WorkoutEntity entity = workoutRepository.save(workoutEntity);

        return workoutMapper.toRecord(entity);
    }

    //
    //CREATE FALLBACK
    //
    public WorkoutResponse createWorkoutFallback(String userEmail, CreateWorkoutRequest workoutRequest, Throwable t) {
        log.error("Circuit Breaker triggered at createWorkout. Cause: {}", t.getMessage());

        //throw a 503 Service Unavailable without collapsing the server
        throw new ServiceUnavailableException
                ("Exercises' catalog's not available at the moment. Please try again later.");
    }

    //UPDATE (PROTECTED WITH CIRCUIT BREAKER)
    @CircuitBreaker(name = "catalogCircuitBreaker", fallbackMethod = "updateWorkoutFallback")
    public WorkoutResponse updateWorkout(UUID id, UpdateWorkoutRequest workoutRequest) {
        //search routine
        WorkoutEntity foundEntity = workoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout with id '" + id + "' not found"));

        //batching validation
        boolean allValid = catalogClient.validateExercises(workoutRequest.exerciseIds());
        if (!allValid) throw new IllegalArgumentException
                ("Error: One or more provided exercise IDs do not exist in the catalog.");

        //update
        foundEntity.setName(workoutRequest.name());
        foundEntity.setExerciseIds(workoutRequest.exerciseIds());

        //save changes
        WorkoutEntity updatedWorkout = workoutRepository.save(foundEntity);

        return workoutMapper.toRecord(updatedWorkout);
    }

    //
    //UPDATE FALLBACK
    //
    public WorkoutResponse updateWorkoutFallback(UUID id, UpdateWorkoutRequest workoutRequest, Throwable t) {
        log.error("Circuit Breaker triggered at updateWorkout. Cause: {}", t.getMessage());

        //throw a 503 Service Unavailable without collapsing the server
        throw new ServiceUnavailableException
                ("Exercises' catalog's not available at the moment. Please try again later.");
    }

    //DELETE
    @Transactional
    public boolean deleteWorkout(UUID id) {
        int rowsAffected = workoutRepository.deleteWorkoutById(id);
        if (rowsAffected == 0)
            throw new ResourceNotFoundException("Workout with id '" + id + "' not found");
        return rowsAffected > 0;
    }

    //GET ALL BY USER'S EMAIL
    public Page<WorkoutResponse> getAllWorkoutsByUserEmail(String userEmail, Pageable pageable) {
        return workoutRepository.findAllWorkoutsByUserEmail(userEmail, pageable).map(workoutMapper::toRecord);
    }

    //GET BY ID ENRICHED (CIRCUIT BREAKER)
    @CircuitBreaker(name = "catalogCircuitBreaker", fallbackMethod = "getWorkoutByIdFallback")
    public WorkoutDetailResponse getWorkoutById(UUID id, String userEmail) {
        //anti-IDOR check
        WorkoutEntity foundEntity = workoutRepository.findWorkoutByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Workout id id '" + id + "' not found."));

        //get all workout's exercises
        List<ExerciseSummaryDto> exercises = catalogClient.getExercisesByIds(foundEntity.getExerciseIds());

        return new WorkoutDetailResponse(
                foundEntity.getId(),
                foundEntity.getUserEmail(),
                foundEntity.getName(),
                exercises
        );
    }

    //
    //GET BY ID FALLBACK
    //
    public WorkoutDetailResponse getWorkoutByIdFallback(UUID id, String userEmail, Throwable t) {
        log.warn("Catalog down when consulting workout '{}'. Returning degraded view. Cause: {}", id, t.getMessage());

        WorkoutEntity foundEntity = workoutRepository.findWorkoutByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Workout with id '" + id + "' not found."));

        List<ExerciseSummaryDto> fallbackExercises = foundEntity.getExerciseIds()
                .stream()
                .map(exerciseId -> new ExerciseSummaryDto(
                        exerciseId,
                        "Details not available",
                        "Catalog service is under maintenance",
                        "UNKNOWN"
                )).toList();

        return new WorkoutDetailResponse(
                foundEntity.getId(),
                foundEntity.getUserEmail(),
                foundEntity.getName(),
                fallbackExercises
        );
    }
}
