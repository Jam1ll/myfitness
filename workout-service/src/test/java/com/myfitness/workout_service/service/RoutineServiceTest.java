package com.myfitness.workout_service.service;

import com.myfitness.workout_service.client.CatalogClient;
import com.myfitness.workout_service.dto.routine.*;
import com.myfitness.workout_service.entity.routine.RoutineEntity;
import com.myfitness.workout_service.entity.routine.RoutineExerciseEntity;
import com.myfitness.workout_service.exception.ResourceNotFoundException;
import com.myfitness.workout_service.mapper.RoutineMapper;
import com.myfitness.workout_service.repository.RoutineRepository;
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
class RoutineServiceTest {

    @Mock
    private RoutineRepository routineRepository;

    @Mock
    private RoutineMapper routineMapper;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private RoutineService routineService;

    @Test
    @DisplayName("Should create and save a routine successfully when all exercise IDs are valid")
    void createRoutine_WhenExercisesAreValid_ShouldReturnRoutineResponse() {
        String userEmail = "test@gmail.com";
        String userName = "JohnDoe";
        UUID exerciseId1 = UUID.randomUUID();
        UUID exerciseId2 = UUID.randomUUID();

        List<RoutineExerciseItemDto> exerciseItems = List.of(
                new RoutineExerciseItemDto(exerciseId1, 3, 10, 60, 0),
                new RoutineExerciseItemDto(exerciseId2, 4, 12, 90, 1)
        );
        List<UUID> exerciseIds = List.of(exerciseId1, exerciseId2);

        CreateRoutineRequest request = new CreateRoutineRequest("Test routine", exerciseItems, true);
        UUID generatedRoutineId = UUID.randomUUID();

        RoutineEntity savedEntity = RoutineEntity.builder()
                .id(generatedRoutineId)
                .userEmail(userEmail)
                .name(request.name())
                .routinePrivate(true) // Corregido: antes decía workoutPrivate
                .createdBy(userName)
                .build();

        savedEntity.addExercise(RoutineExerciseEntity.builder().exerciseId(exerciseId1).targetSets(3).targetReps(10).restTimeSeconds(60).orderIndex(0).build());
        savedEntity.addExercise(RoutineExerciseEntity.builder().exerciseId(exerciseId2).targetSets(4).targetReps(12).restTimeSeconds(90).orderIndex(1).build());

        RoutineResponse expectedResponse = new RoutineResponse(
                generatedRoutineId,
                request.name(),
                exerciseIds,
                true,
                userName,
                LocalDateTime.MIN,
                true
        );

        when(catalogClient.validateExercises(exerciseIds)).thenReturn(true);
        when(routineRepository.save(any(RoutineEntity.class))).thenReturn(savedEntity);
        when(routineMapper.toRecord(savedEntity, userEmail)).thenReturn(expectedResponse);

        // Corregido: llamado a createRoutine en vez de createWorkout
        RoutineResponse actualResponse = routineService.createRoutine(userEmail, userName, request);

        assertNotNull(actualResponse);
        assertEquals(generatedRoutineId, actualResponse.id());
        assertEquals("Test routine", actualResponse.name());
        assertEquals(2, actualResponse.exerciseIds().size());

        verify(routineRepository, times(1)).save(any(RoutineEntity.class));
        verify(catalogClient, times(1)).validateExercises(exerciseIds);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException and never save in DB when any exercise does not exist in catalog")
    void createRoutine_WhenExercisesAreInvalid_ShouldThrowExceptionAndNeverSave() {
        String userEmail = "test@gmail.com";
        String userName = "JohnDoe";
        UUID fakeId = UUID.randomUUID();
        List<RoutineExerciseItemDto> fakeItems = List.of(new RoutineExerciseItemDto(fakeId, 3, 10, 60, 0));
        List<UUID> fakeExerciseIds = List.of(fakeId);

        CreateRoutineRequest request = new CreateRoutineRequest("Invalid routine", fakeItems, true);

        when(catalogClient.validateExercises(fakeExerciseIds)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> routineService.createRoutine(userEmail, userName, request)
        );

        assertTrue(exception.getMessage().contains("do not exist"));

        verify(routineRepository, never()).save(any());
        verify(routineMapper, never()).toRecord(any(), anyString());
    }

    @Test
    @DisplayName("Should return routine detail enriched with exercises from catalog when routine exists and belongs to user")
    void getRoutineById_WhenRoutineExistsAndBelongsToUser_ShouldReturnDetailResponse() {
        UUID routineId = UUID.randomUUID();
        String userEmail = "test@gmail.com";
        UUID exerciseId = UUID.randomUUID();
        List<UUID> exerciseIds = List.of(exerciseId);

        RoutineEntity existingRoutine = RoutineEntity.builder()
                .id(routineId)
                .userEmail(userEmail)
                .name("Full Body")
                .build();

        existingRoutine.addExercise(RoutineExerciseEntity.builder()
                .exerciseId(exerciseId)
                .targetSets(3)
                .targetReps(10)
                .restTimeSeconds(60)
                .orderIndex(0)
                .build());

        List<ExerciseSummaryDto> catalogExercises = List.of(
                new ExerciseSummaryDto(exerciseId, "Bench Press", "Barbell chest press", "CHEST", true, "JohnDoe", LocalDateTime.MIN)
        );

        when(routineRepository.findById(routineId))
                .thenReturn(Optional.of(existingRoutine));

        when(catalogClient.getExercisesByIds(userEmail, exerciseIds))
                .thenReturn(catalogExercises);

        // Corregido: llamado a getRoutineById en vez de getWorkoutById
        RoutineDetailResponse detailResponse = routineService.getRoutineById(routineId, userEmail);

        assertNotNull(detailResponse);
        assertEquals(routineId, detailResponse.id());
        assertEquals(1, detailResponse.enrichedRoutineExercises().size());
        assertEquals("Bench Press", detailResponse.enrichedRoutineExercises().getFirst().name());

        verify(routineRepository, times(1)).findById(routineId);
        verify(catalogClient, times(1)).getExercisesByIds(userEmail, exerciseIds);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when routine does not exist")
    void getRoutineById_WhenNotFoundOrWrongUser_ShouldThrowResourceNotFoundException() {
        UUID routineId = UUID.randomUUID();
        String unauthorizedEmail = "attacker@gmail.com";

        when(routineRepository.findById(routineId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> routineService.getRoutineById(routineId, unauthorizedEmail) // Corregido
        );

        verify(routineRepository, times(1)).findById(routineId);
        verify(catalogClient, never()).getExercisesByIds(any(), any());
    }

    @Test
    @DisplayName("Should return a paginated list of accessible routines with search")
    void getAllAccessibleRoutines_ShouldReturnPageOfRoutineResponse() {
        String userEmail = "test@gmail.com";
        String searchQuery = "";
        boolean mineOnly = false;
        Pageable pageable = PageRequest.of(0, 10);
        UUID routineId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        List<UUID> exerciseIds = List.of(exerciseId);

        RoutineEntity entity = RoutineEntity.builder()
                .id(routineId)
                .userEmail(userEmail)
                .name("Leg Day")
                .build();

        entity.addExercise(RoutineExerciseEntity.builder()
                .exerciseId(exerciseId)
                .targetSets(3)
                .targetReps(12)
                .restTimeSeconds(90)
                .orderIndex(0)
                .build());

        RoutineResponse responseDto = new RoutineResponse(
                routineId,
                "Leg Day",
                exerciseIds,
                true,
                "JohnDoe",
                LocalDateTime.MIN,
                true
        );
        Page<RoutineEntity> entityPage = new PageImpl<>(List.of(entity));

        when(routineRepository.findAccessibleRoutinesWithSearch(eq(userEmail), eq(searchQuery), eq(pageable)))
                .thenReturn(entityPage);

        when(routineMapper.toRecord(entity, userEmail)).thenReturn(responseDto);

        // Corregido: llamado a getAllAccessibleRoutines
        Page<RoutineResponse> result = routineService.getAllAccessibleRoutines(userEmail, searchQuery, mineOnly, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Leg Day", result.getContent().getFirst().name());

        verify(routineRepository, times(1)).findAccessibleRoutinesWithSearch(userEmail, searchQuery, pageable);
        verify(routineMapper, times(1)).toRecord(entity, userEmail);
    }

    @Test
    @DisplayName("Should update an existing routine when new exercise IDs are valid")
    void updateRoutine_WhenValid_ShouldUpdateAndReturnRoutineResponse() {
        UUID routineId = UUID.randomUUID();
        String userEmail = "test@gmail.com";
        UUID newExerciseId = UUID.randomUUID();
        List<RoutineExerciseItemDto> newItems = List.of(new RoutineExerciseItemDto(newExerciseId, 3, 10, 60, 0));
        List<UUID> newExerciseIds = List.of(newExerciseId);

        UpdateRoutineRequest updateRequest = new UpdateRoutineRequest("Updated Routine", newItems, true);

        RoutineEntity existingEntity = RoutineEntity.builder()
                .id(routineId)
                .userEmail(userEmail)
                .name("Old Routine")
                .build();

        RoutineResponse expectedResponse = new RoutineResponse(
                routineId,
                "Updated Routine",
                newExerciseIds,
                true,
                "JohnDoe",
                LocalDateTime.MIN,
                true
        );

        when(routineRepository.findRoutineByIdAndUserEmail(routineId, userEmail)).thenReturn(Optional.of(existingEntity));
        when(catalogClient.validateExercises(newExerciseIds)).thenReturn(true);
        when(routineRepository.save(existingEntity)).thenReturn(existingEntity);
        when(routineMapper.toRecord(existingEntity, userEmail)).thenReturn(expectedResponse);

        // Corregido: llamado a updateRoutine
        RoutineResponse actualResponse = routineService.updateRoutine(userEmail, routineId, updateRequest);

        assertNotNull(actualResponse);
        assertEquals("Updated Routine", actualResponse.name());
        assertEquals("Updated Routine", existingEntity.getName());
        assertEquals(1, existingEntity.getRoutineExerciseEntities().size()); // Corregido

        verify(catalogClient, times(1)).validateExercises(newExerciseIds);
        verify(routineRepository, times(1)).save(existingEntity);
    }

    @Test
    @DisplayName("Should delete routine when ID exists and belongs to user")
    void deleteRoutine_WhenExists_ShouldDeleteEntity() {
        UUID routineId = UUID.randomUUID();
        String userEmail = "test@gmail.com";

        when(routineRepository.deleteRoutineByIdAndUserEmail(userEmail, routineId)).thenReturn(1);

        routineService.deleteRoutine(userEmail, routineId); // Corregido

        verify(routineRepository, times(1)).deleteRoutineByIdAndUserEmail(userEmail, routineId);
    }
}