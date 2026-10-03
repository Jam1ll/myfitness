package com.myfitness.catalog_service.service;

import com.myfitness.catalog_service.dto.CreateExerciseRequest;
import com.myfitness.catalog_service.dto.ExerciseResponse;
import com.myfitness.catalog_service.dto.UpdateExerciseRequest;
import com.myfitness.catalog_service.entity.ExerciseEntity;
import com.myfitness.catalog_service.entity.enums.MuscleGroup;
import com.myfitness.catalog_service.exception.ResourceNotFoundException;
import com.myfitness.catalog_service.mapper.ExerciseMapper;
import com.myfitness.catalog_service.repository.ExerciseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private ExerciseMapper exerciseMapper;
    @InjectMocks
    private ExerciseService exerciseService;

    @Test
    @DisplayName("Must create an exercise and return an ExerciseResponse successfully")
    void createExercise_ReturnsResponse() {

        //entry data
        CreateExerciseRequest request = new CreateExerciseRequest
                ("Chest Press", "Good exercise", MuscleGroup.CHEST);

        //false entity
        ExerciseEntity mappedEntity = new ExerciseEntity();
        mappedEntity.setName("Chest Press");
        mappedEntity.setDescription("Good exercise");
        mappedEntity.setMuscleGroup(MuscleGroup.CHEST);

        //database false return
        UUID generatedId = UUID.randomUUID();
        ExerciseEntity savedEntity = new ExerciseEntity();
        savedEntity.setId(generatedId);
        savedEntity.setName("Chest Press");
        savedEntity.setMuscleGroup(MuscleGroup.CHEST);

        //final dto
        ExerciseResponse expectedResponse = new ExerciseResponse
                (generatedId, "Chest Press", "Good exercise", MuscleGroup.CHEST);

        //mock training
        when(exerciseMapper.toEntity(request)).thenReturn(mappedEntity);
        when(exerciseRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(exerciseMapper.toRecord(savedEntity)).thenReturn(expectedResponse);

        //exec action
        ExerciseResponse actualResponse = exerciseService.createExercise(request);

        //results check:

        //assertions
        assertNotNull(actualResponse, "response must not be null");
        assertEquals(expectedResponse.id(), actualResponse.id(), "id must coincide");
        assertEquals("Chest Press", actualResponse.name(), "name must coincide");
        assertEquals("Good exercise", actualResponse.description(), "description must coincide");

        //behavior check
        verify(exerciseMapper, times(1)).toEntity(request);
        verify(exerciseRepository, times(1)).save(mappedEntity);
        verify(exerciseMapper, times(1)).toRecord(savedEntity);
    }

    @Test
    @DisplayName("Must throw an exception if exercise to update does not exist")
    void updateExercise_NotFound_ThrowsException() {
        //given (prep)
        UUID nonExistentId = UUID.randomUUID();
        UpdateExerciseRequest request = new UpdateExerciseRequest("Leg Press", "desc", MuscleGroup.LEGS);

        //when (training)
        when(exerciseRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            exerciseService.updateExercise(nonExistentId, request);
        });

        //verify (check)
        verify(exerciseMapper, never()).updateEntityFromRequest(any(), any());
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Must return true after deleting an existing exercise")
    void deleteExercise_ExistingId_ReturnsTrue() {
        UUID existingId = UUID.randomUUID();

        when(exerciseRepository.deleteExerciseById(existingId)).thenReturn(1);

        boolean isDeleted = exerciseService.deleteExercise(existingId);

        assertTrue(isDeleted, "Must return true after successful deletion");

        verify(exerciseRepository, times(1)).deleteExerciseById(existingId);
    }

    @Test
    @DisplayName("Must throw ResourceNotFoundException when trying to delete a non-existing exercise")
    void deleteExercise_NonExistingId_ThrowsException() {
        UUID ghostId = UUID.randomUUID();

        when(exerciseRepository.deleteExerciseById(ghostId)).thenReturn(0);

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            exerciseService.deleteExercise(ghostId);
        }, "Must throw ResourceNotFoundException when the exercise does not exist");
    }

    @Test
    @DisplayName("Must throw ResourceNotFoundException when exercise ID does not exist")
    void getExerciseById_NotFound_ThrowsException() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        when(exerciseRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            exerciseService.getExerciseById(nonExistentId);
        }, "Must throw ResourceNotFoundException when the ID is not found");

        verify(exerciseRepository, times(1)).findById(nonExistentId);
        verify(exerciseMapper, never()).toRecord(any());
    }

    @Test
    @DisplayName("Must throw ResourceNotFoundException when exercise name does not exist")
    void getExerciseByName_NotFound_ThrowsException() {
        // given
        String nonExistentName = "Inexistent Curl";

        // when
        when(exerciseRepository.findByName(nonExistentName)).thenReturn(java.util.Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            exerciseService.getExerciseByName(nonExistentName);
        }, "Must throw ResourceNotFoundException when the name is not found");

        verify(exerciseRepository, times(1)).findByName(nonExistentName);
        verify(exerciseMapper, never()).toRecord(any());
    }

    @Test
    @DisplayName("Must return a paginated list of ExerciseResponse")
    void getAllExercises_ReturnsPaginatedList() {
        // given
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        ExerciseEntity entity = new ExerciseEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Squat");

        ExerciseResponse response = new ExerciseResponse(entity.getId(), "Squat", "Leg exercise", MuscleGroup.LEGS);

        org.springframework.data.domain.Page<ExerciseEntity> entityPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(entity));

        // when
        when(exerciseRepository.findAll(pageable)).thenReturn(entityPage);
        when(exerciseMapper.toRecord(entity)).thenReturn(response);

        // execute
        org.springframework.data.domain.Page<ExerciseResponse> resultPage = exerciseService.getAllExercises(pageable);

        // then
        assertNotNull(resultPage, "Page must not be null");
        assertEquals(1, resultPage.getTotalElements(), "Page must contain exactly 1 element");
        assertEquals("Squat", resultPage.getContent().getFirst().name(), "Exercise name must match");

        verify(exerciseRepository, times(1)).findAll(pageable);
        verify(exerciseMapper, times(1)).toRecord(entity);
    }

    @Test
    @DisplayName("Must return a paginated list of ExerciseResponse filtered by muscle group")
    void getAllExercisesByMuscleGroup_ReturnsPaginatedList() {
        // given
        MuscleGroup targetGroup = MuscleGroup.CHEST;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        ExerciseEntity entity = new ExerciseEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Bench Press");
        entity.setMuscleGroup(targetGroup);

        ExerciseResponse response = new ExerciseResponse(entity.getId(), "Bench Press", "Chest exercise", targetGroup);

        org.springframework.data.domain.Page<ExerciseEntity> entityPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(entity));

        // when
        when(exerciseRepository.findByMuscleGroup(targetGroup, pageable)).thenReturn(entityPage);
        when(exerciseMapper.toRecord(entity)).thenReturn(response);

        // execute
        org.springframework.data.domain.Page<ExerciseResponse> resultPage = exerciseService.getAllExercisesByMuscleGroup(targetGroup, pageable);

        // then
        assertNotNull(resultPage, "Page must not be null");
        assertEquals(1, resultPage.getTotalElements(), "Page must contain exactly 1 element");
        assertEquals(targetGroup, resultPage.getContent().getFirst().muscleGroup(), "Muscle group must match the filter");

        verify(exerciseRepository, times(1)).findByMuscleGroup(targetGroup, pageable);
        verify(exerciseMapper, times(1)).toRecord(entity);
    }
}