package com.myfitness.workout_service.service;

import com.myfitness.workout_service.client.CatalogClient;
import com.myfitness.workout_service.dto.CreateWorkoutRequest;
import com.myfitness.workout_service.dto.ExerciseSummaryDto;
import com.myfitness.workout_service.dto.UpdateWorkoutRequest;
import com.myfitness.workout_service.dto.WorkoutDetailResponse;
import com.myfitness.workout_service.dto.WorkoutResponse;
import com.myfitness.workout_service.entity.WorkoutEntity;
import com.myfitness.workout_service.exception.ResourceNotFoundException;
import com.myfitness.workout_service.mapper.WorkoutMapper;
import com.myfitness.workout_service.repository.WorkoutRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    // External dependencies Mocks
    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private WorkoutMapper workoutMapper;

    @Mock
    private CatalogClient catalogClient;

    // Inject mocks into the real service
    @InjectMocks
    private WorkoutService workoutService;

    //
    // 1. CREATE WORKOUT - HAPPY PATH
    //
    @Test
    @DisplayName("Should create and save a workout successfully when all exercise IDs are valid")
    void createWorkout_WhenExercisesAreValid_ShouldReturnWorkoutResponse() {
        //
        // GIVEN
        //
        String userEmail = "test@gmail.com";
        UUID exerciseId1 = UUID.randomUUID();
        UUID exerciseId2 = UUID.randomUUID();
        List<UUID> exerciseIds = List.of(exerciseId1, exerciseId2);

        CreateWorkoutRequest request = new CreateWorkoutRequest("Test routine", exerciseIds);

        UUID generatedWorkoutId = UUID.randomUUID();

        WorkoutEntity savedEntity = WorkoutEntity.builder()
                .id(generatedWorkoutId)
                .userEmail(userEmail)
                .name(request.name())
                .exerciseIds(exerciseIds)
                .build();

        WorkoutResponse expectedResponse = new WorkoutResponse(
                generatedWorkoutId,
                userEmail,
                request.name(),
                exerciseIds
        );

        // 1. when using OpenFeign with those exercises, return true
        when(catalogClient.validateExercises(exerciseIds)).thenReturn(true);
        // 2. when repository saves any WorkoutEntity, return mocked savedEntity
        when(workoutRepository.save(any(WorkoutEntity.class))).thenReturn(savedEntity);
        // 3. when mapper is called, return the correct DTO (record)
        when(workoutMapper.toRecord(savedEntity)).thenReturn(expectedResponse);

        //
        // WHEN
        //
        WorkoutResponse actualResponse = workoutService.createWorkout(userEmail, request);

        //
        // THEN
        //
        assertNotNull(actualResponse);
        assertEquals(generatedWorkoutId, actualResponse.id());
        assertEquals("Test routine", actualResponse.name());
        assertEquals(2, actualResponse.exerciseIds().size());

        // workout repository was only called 1 time
        verify(workoutRepository, times(1)).save(any(WorkoutEntity.class));
        // OpenFeign validation method was only called 1 time
        verify(catalogClient, times(1)).validateExercises(exerciseIds);
    }

    //
    // 2. CREATE WORKOUT - FAILED VALIDATION
    //
    @Test
    @DisplayName("Should throw IllegalArgumentException and never save in DB when any exercise does not exist in catalog")
    void createWorkout_WhenExercisesAreInvalid_ShouldThrowExceptionAndNeverSave() {
        //
        // GIVEN
        //
        String userEmail = "test@gmail.com";
        List<UUID> fakeExerciseIds = List.of(UUID.randomUUID());
        CreateWorkoutRequest request = new CreateWorkoutRequest("Invalid routine", fakeExerciseIds);

        // 1. when using OpenFeign with invalid exercises, return false
        when(catalogClient.validateExercises(fakeExerciseIds)).thenReturn(false);

        //
        // WHEN & THEN
        //
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workoutService.createWorkout(userEmail, request)
        );

        assertTrue(exception.getMessage().contains("do not exist"));

        // workout repository and mapper must never be called if validation fails
        verify(workoutRepository, never()).save(any());
        verify(workoutMapper, never()).toRecord(any());
    }

    //
    // 3. GET WORKOUT BY ID - ENRICHED DETAIL
    //
    @Test
    @DisplayName("Should return workout detail enriched with exercises from catalog when workout exists and belongs to user")
    void getWorkoutById_WhenWorkoutExistsAndBelongsToUser_ShouldReturnDetailResponse() {
        //
        // GIVEN
        //
        UUID workoutId = UUID.randomUUID();
        String userEmail = "test@gmail.com";
        UUID exerciseId = UUID.randomUUID();
        List<UUID> exerciseIds = List.of(exerciseId);

        WorkoutEntity existingWorkout = WorkoutEntity.builder()
                .id(workoutId)
                .userEmail(userEmail)
                .name("Full Body")
                .exerciseIds(exerciseIds)
                .build();

        List<ExerciseSummaryDto> catalogExercises = List.of(
                new ExerciseSummaryDto(exerciseId, "Bench Press", "Barbell chest press", "CHEST")
        );

        // 1. when repository searches by ID and user email, return the existing workout
        when(workoutRepository.findWorkoutByIdAndUserEmail(workoutId, userEmail))
                .thenReturn(Optional.of(existingWorkout));
        // 2. when OpenFeign fetches exercise details in batch, return the exercise list
        when(catalogClient.getExercisesByIds(exerciseIds))
                .thenReturn(catalogExercises);

        //
        // WHEN
        //
        WorkoutDetailResponse detailResponse = workoutService.getWorkoutById(workoutId, userEmail);

        //
        // THEN
        //
        assertNotNull(detailResponse);
        assertEquals(workoutId, detailResponse.id());
        assertEquals(1, detailResponse.exercises().size());
        assertEquals("Bench Press", detailResponse.exercises().getFirst().name());

        // repository and OpenFeign client were only called 1 time each
        verify(workoutRepository, times(1)).findWorkoutByIdAndUserEmail(workoutId, userEmail);
        verify(catalogClient, times(1)).getExercisesByIds(exerciseIds);
    }

    //
    // 4. GET WORKOUT BY ID - UNAUTHORIZED OR NOT FOUND
    //
    @Test
    @DisplayName("Should throw ResourceNotFoundException when workout does not exist or belongs to another user")
    void getWorkoutById_WhenNotFoundOrWrongUser_ShouldThrowResourceNotFoundException() {
        //
        // GIVEN
        //
        UUID workoutId = UUID.randomUUID();
        String unauthorizedEmail = "attacker@gmail.com";

        // 1. when repository searches by ID and email, return empty Optional
        when(workoutRepository.findWorkoutByIdAndUserEmail(workoutId, unauthorizedEmail))
                .thenReturn(Optional.empty());

        //
        // WHEN & THEN
        //
        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.getWorkoutById(workoutId, unauthorizedEmail)
        );

        // OpenFeign must never be called if the workout was not found or user is not the owner
        verify(catalogClient, never()).getExercisesByIds(any());
    }

    //
    // 5. GET ALL WORKOUTS BY USER EMAIL (PAGINATED)
    //
    @Test
    @DisplayName("Should return a paginated list of workouts belonging to the specified user email")
    void getWorkoutsByUserEmail_ShouldReturnPageOfWorkoutResponse() {
        //
        // GIVEN
        //
        String userEmail = "test@gmail.com";
        Pageable pageable = PageRequest.of(0, 10);
        UUID workoutId = UUID.randomUUID();
        List<UUID> exerciseIds = List.of(UUID.randomUUID());

        WorkoutEntity entity = WorkoutEntity.builder()
                .id(workoutId)
                .userEmail(userEmail)
                .name("Leg Day")
                .exerciseIds(exerciseIds)
                .build();

        WorkoutResponse responseDto = new WorkoutResponse(workoutId, userEmail, "Leg Day", exerciseIds);
        Page<WorkoutEntity> entityPage = new PageImpl<>(List.of(entity));

        // 1. when repository searches paginated workouts by email, return entityPage
        when(workoutRepository.findAllWorkoutsByUserEmail(userEmail, pageable)).thenReturn(entityPage);
        // 2. when mapper converts entity, return expected DTO
        when(workoutMapper.toRecord(entity)).thenReturn(responseDto);

        //
        // WHEN
        //
        Page<WorkoutResponse> result = workoutService.getAllWorkoutsByUserEmail(userEmail, pageable);

        //
        // THEN
        //
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Leg Day", result.getContent().getFirst().name());

        // repository was only called 1 time with email and pageable
        verify(workoutRepository, times(1)).findAllWorkoutsByUserEmail(userEmail, pageable);
    }

    //
    // 6. UPDATE WORKOUT - HAPPY PATH
    //
    @Test
    @DisplayName("Should update an existing workout when new exercise IDs are valid")
    void updateWorkout_WhenValid_ShouldUpdateAndReturnWorkoutResponse() {
        //
        // GIVEN
        //
        UUID workoutId = UUID.randomUUID();
        String userEmail = "test@gmail.com";
        List<UUID> newExerciseIds = List.of(UUID.randomUUID());
        UpdateWorkoutRequest updateRequest = new UpdateWorkoutRequest("Updated Routine", newExerciseIds);

        WorkoutEntity existingEntity = WorkoutEntity.builder()
                .id(workoutId)
                .userEmail(userEmail)
                .name("Old Routine")
                .exerciseIds(List.of())
                .build();

        WorkoutResponse expectedResponse = new WorkoutResponse(
                workoutId,
                userEmail,
                "Updated Routine",
                newExerciseIds
        );

        // 1. when repository searches by ID, return the existing entity
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(existingEntity));
        // 2. when OpenFeign validates the new exercises from the request, return true
        when(catalogClient.validateExercises(newExerciseIds)).thenReturn(true);
        // 3. when repository saves the updated entity, return it
        when(workoutRepository.save(existingEntity)).thenReturn(existingEntity);
        // 4. when mapper converts entity to record, return expected response
        when(workoutMapper.toRecord(existingEntity)).thenReturn(expectedResponse);

        //
        // WHEN
        //
        WorkoutResponse actualResponse = workoutService.updateWorkout(workoutId, updateRequest);

        //
        // THEN
        //
        assertNotNull(actualResponse);
        assertEquals("Updated Routine", actualResponse.name());
        assertEquals("Updated Routine", existingEntity.getName());
        assertEquals(newExerciseIds, existingEntity.getExerciseIds());

        // verify OpenFeign validation and repository save were called once
        verify(catalogClient, times(1)).validateExercises(newExerciseIds);
        verify(workoutRepository, times(1)).save(existingEntity);
    }

    //
    // 7. DELETE WORKOUT
    //
    @Test
    @DisplayName("Should delete workout when ID exists")
    void deleteWorkout_WhenExists_ShouldDeleteEntity() {
        //
        // GIVEN
        //
        UUID workoutId = UUID.randomUUID();

        // 1. when repository deletes by ID, return 1 (indicating 1 row was deleted)
        when(workoutRepository.deleteWorkoutById(workoutId)).thenReturn(1);

        //
        // WHEN
        //
        workoutService.deleteWorkout(workoutId);

        //
        // THEN
        //
        // verify repository deleteWorkoutById was called 1 time
        verify(workoutRepository, times(1)).deleteWorkoutById(workoutId);
    }
}