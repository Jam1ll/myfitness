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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private WorkoutMapper workoutMapper;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private WorkoutService workoutService;

    @Test
    @DisplayName("Should create and save a workout successfully when all exercise IDs are valid")
    void createWorkout_WhenExercisesAreValid_ShouldReturnWorkoutResponse() {
        String userEmail = "test@gmail.com";
        String userName = "JohnDoe";
        UUID exerciseId1 = UUID.randomUUID();
        UUID exerciseId2 = UUID.randomUUID();
        List<UUID> exerciseIds = List.of(exerciseId1, exerciseId2);

        CreateWorkoutRequest request = new CreateWorkoutRequest("Test routine", exerciseIds, true);
        UUID generatedWorkoutId = UUID.randomUUID();

        WorkoutEntity savedEntity = WorkoutEntity.builder()
                .id(generatedWorkoutId)
                .userEmail(userEmail)
                .name(request.name())
                .exerciseIds(exerciseIds)
                .workoutPrivate(true)
                .createdBy(userName)
                .build();

        WorkoutResponse expectedResponse = new WorkoutResponse(
                generatedWorkoutId,
                request.name(),
                exerciseIds,
                true,
                userName,
                LocalDateTime.MIN,
                true
        );

        when(catalogClient.validateExercises(exerciseIds)).thenReturn(true);
        when(workoutRepository.save(any(WorkoutEntity.class))).thenReturn(savedEntity);
        when(workoutMapper.toRecord(savedEntity, userEmail)).thenReturn(expectedResponse);

        WorkoutResponse actualResponse = workoutService.createWorkout(userEmail, userName, request);

        assertNotNull(actualResponse);
        assertEquals(generatedWorkoutId, actualResponse.id());
        assertEquals("Test routine", actualResponse.name());
        assertEquals(2, actualResponse.exerciseIds().size());

        verify(workoutRepository, times(1)).save(any(WorkoutEntity.class));
        verify(catalogClient, times(1)).validateExercises(exerciseIds);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException and never save in DB when any exercise does not exist in catalog")
    void createWorkout_WhenExercisesAreInvalid_ShouldThrowExceptionAndNeverSave() {
        String userEmail = "test@gmail.com";
        String userName = "JohnDoe";
        List<UUID> fakeExerciseIds = List.of(UUID.randomUUID());
        CreateWorkoutRequest request = new CreateWorkoutRequest("Invalid routine", fakeExerciseIds, true);

        when(catalogClient.validateExercises(fakeExerciseIds)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> workoutService.createWorkout(userEmail, userName, request)
        );

        assertTrue(exception.getMessage().contains("do not exist"));

        verify(workoutRepository, never()).save(any());
        verify(workoutMapper, never()).toRecord(any(), anyString());
    }

    @Test
    @DisplayName("Should return workout detail enriched with exercises from catalog when workout exists and belongs to user")
    void getWorkoutById_WhenWorkoutExistsAndBelongsToUser_ShouldReturnDetailResponse() {
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
                new ExerciseSummaryDto(exerciseId, "Bench Press", "Barbell chest press", "CHEST", true, "JohnDoe", LocalDateTime.MIN)
        );

        when(workoutRepository.findWorkoutByIdAndUserEmail(workoutId, userEmail))
                .thenReturn(Optional.of(existingWorkout));
        when(catalogClient.getExercisesByIds(userEmail, exerciseIds))
                .thenReturn(catalogExercises);

        WorkoutDetailResponse detailResponse = workoutService.getWorkoutById(workoutId, userEmail);

        assertNotNull(detailResponse);
        assertEquals(workoutId, detailResponse.id());
        assertEquals(1, detailResponse.exercises().size());
        assertEquals("Bench Press", detailResponse.exercises().getFirst().name());

        verify(workoutRepository, times(1)).findWorkoutByIdAndUserEmail(workoutId, userEmail);
        verify(catalogClient, times(1)).getExercisesByIds(userEmail, exerciseIds);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when workout does not exist or belongs to another user")
    void getWorkoutById_WhenNotFoundOrWrongUser_ShouldThrowResourceNotFoundException() {
        UUID workoutId = UUID.randomUUID();
        String unauthorizedEmail = "attacker@gmail.com";

        when(workoutRepository.findWorkoutByIdAndUserEmail(workoutId, unauthorizedEmail))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutService.getWorkoutById(workoutId, unauthorizedEmail)
        );

        verify(catalogClient, never()).getExercisesByIds(any(), any());
    }

    @Test
    @DisplayName("Should return a paginated list of workouts belonging to the specified user email")
    void getWorkoutsByUserEmail_ShouldReturnPageOfWorkoutResponse() {
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

        WorkoutResponse responseDto = new WorkoutResponse(
                workoutId,
                "Leg Day",
                exerciseIds,
                true,
                "JohnDoe",
                LocalDateTime.MIN,
                true
        );
        Page<WorkoutEntity> entityPage = new PageImpl<>(List.of(entity));

        when(workoutRepository.findAllWorkoutsByUserEmail(userEmail, pageable)).thenReturn(entityPage);
        when(workoutMapper.toRecord(entity, userEmail)).thenReturn(responseDto);

        Page<WorkoutResponse> result = workoutService.getAllAccessibleWorkouts(userEmail, "", false, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Leg Day", result.getContent().getFirst().name());

        verify(workoutRepository, times(1)).findAllWorkoutsByUserEmail(userEmail, pageable);
    }

    @Test
    @DisplayName("Should update an existing workout when new exercise IDs are valid")
    void updateWorkout_WhenValid_ShouldUpdateAndReturnWorkoutResponse() {
        UUID workoutId = UUID.randomUUID();
        String userEmail = "test@gmail.com";
        List<UUID> newExerciseIds = List.of(UUID.randomUUID());
        UpdateWorkoutRequest updateRequest = new UpdateWorkoutRequest("Updated Routine", newExerciseIds, true);

        WorkoutEntity existingEntity = WorkoutEntity.builder()
                .id(workoutId)
                .userEmail(userEmail)
                .name("Old Routine")
                .exerciseIds(List.of())
                .build();

        WorkoutResponse expectedResponse = new WorkoutResponse(
                workoutId,
                "Updated Routine",
                newExerciseIds,
                true,
                "JohnDoe",
                LocalDateTime.MIN,
                true
        );

        when(workoutRepository.findWorkoutByIdAndUserEmail(workoutId, userEmail)).thenReturn(Optional.of(existingEntity));
        when(catalogClient.validateExercises(newExerciseIds)).thenReturn(true);
        when(workoutRepository.save(existingEntity)).thenReturn(existingEntity);
        when(workoutMapper.toRecord(existingEntity, userEmail)).thenReturn(expectedResponse);

        WorkoutResponse actualResponse = workoutService.updateWorkout(userEmail, workoutId, updateRequest);

        assertNotNull(actualResponse);
        assertEquals("Updated Routine", actualResponse.name());
        assertEquals("Updated Routine", existingEntity.getName());
        assertEquals(newExerciseIds, existingEntity.getExerciseIds());

        verify(catalogClient, times(1)).validateExercises(newExerciseIds);
        verify(workoutRepository, times(1)).save(existingEntity);
    }

    @Test
    @DisplayName("Should delete workout when ID exists and belongs to user")
    void deleteWorkout_WhenExists_ShouldDeleteEntity() {
        UUID workoutId = UUID.randomUUID();
        String userEmail = "test@gmail.com";

        // EL ORDEN CORREGIDO DE LOS PARÁMETROS: (String userEmail, UUID id)
        when(workoutRepository.deleteWorkoutByIdAndUserEmail(userEmail, workoutId)).thenReturn(1);

        workoutService.deleteWorkout(userEmail, workoutId);

        verify(workoutRepository, times(1)).deleteWorkoutByIdAndUserEmail(userEmail, workoutId);
    }
}