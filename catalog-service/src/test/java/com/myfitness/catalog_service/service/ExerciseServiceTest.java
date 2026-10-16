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

import java.time.LocalDateTime;
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
        String userEmail = "JohnDoe@gmail.com";
        String userName = "JohnDoe";

        CreateExerciseRequest request = new CreateExerciseRequest
                ("Chest Press", "Good exercise", MuscleGroup.CHEST, true);

        ExerciseEntity mappedEntity = new ExerciseEntity();
        mappedEntity.setName("Chest Press");
        mappedEntity.setDescription("Good exercise");
        mappedEntity.setMuscleGroup(MuscleGroup.CHEST);

        UUID generatedId = UUID.randomUUID();
        ExerciseEntity savedEntity = new ExerciseEntity();
        savedEntity.setId(generatedId);
        savedEntity.setName("Chest Press");
        savedEntity.setMuscleGroup(MuscleGroup.CHEST);
        savedEntity.setUserEmail(userEmail);

        ExerciseResponse expectedResponse = new ExerciseResponse
                (generatedId, "Chest Press", "Good exercise", MuscleGroup.CHEST, true, userName, LocalDateTime.MIN, true);

        when(exerciseMapper.toEntity(request)).thenReturn(mappedEntity);
        when(exerciseRepository.save(mappedEntity)).thenReturn(savedEntity);

        // Se añade el userEmail para satisfacer la firma de toRecord
        when(exerciseMapper.toRecord(savedEntity, userEmail)).thenReturn(expectedResponse);

        ExerciseResponse actualResponse = exerciseService.createExercise(userEmail, userName, request);

        assertNotNull(actualResponse, "response must not be null");
        assertEquals(expectedResponse.id(), actualResponse.id(), "id must coincide");
        assertEquals("Chest Press", actualResponse.name(), "name must coincide");
        assertEquals("Good exercise", actualResponse.description(), "description must coincide");

        verify(exerciseMapper, times(1)).toEntity(request);
        verify(exerciseRepository, times(1)).save(mappedEntity);
        verify(exerciseMapper, times(1)).toRecord(savedEntity, userEmail);
    }

    @Test
    @DisplayName("Must throw an exception if exercise to update does not exist")
    void updateExercise_NotFound_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        UpdateExerciseRequest request = new UpdateExerciseRequest("Leg Press", "desc", MuscleGroup.LEGS, true);

        when(exerciseRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            exerciseService.updateExercise(nonExistentId, request);
        });

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

        assertThrows(ResourceNotFoundException.class, () -> {
            exerciseService.deleteExercise(ghostId);
        }, "Must throw ResourceNotFoundException when the exercise does not exist");
    }

    @Test
    @DisplayName("Must throw ResourceNotFoundException when exercise ID does not exist")
    void getExerciseById_NotFound_ThrowsException() {
        UUID nonExistentId = UUID.randomUUID();
        String requesterEmail = "test@gmail.com";

        when(exerciseRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            // Se añade el requesterEmail a la llamada
            exerciseService.getExerciseById(nonExistentId, requesterEmail);
        }, "Must throw ResourceNotFoundException when the ID is not found");

        verify(exerciseRepository, times(1)).findById(nonExistentId);
        verify(exerciseMapper, never()).toRecord(any(), anyString());
    }

    @Test
    @DisplayName("Must throw ResourceNotFoundException when exercise name does not exist")
    void getExerciseByName_NotFound_ThrowsException() {
        String nonExistentName = "Inexistent Curl";
        String requesterEmail = "test@gmail.com";

        when(exerciseRepository.findByName(nonExistentName)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            // Se asume que el método getExerciseByName también pide el requesterEmail
            exerciseService.getExerciseByName(nonExistentName, requesterEmail);
        }, "Must throw ResourceNotFoundException when the name is not found");

        verify(exerciseRepository, times(1)).findByName(nonExistentName);
        verify(exerciseMapper, never()).toRecord(any(), anyString());
    }

    @Test
    @DisplayName("Must return a paginated list of ExerciseResponse")
    void getAllExercises_ReturnsPaginatedList() {
        String requesterEmail = "test@gmail.com";
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        ExerciseEntity entity = new ExerciseEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Squat");

        ExerciseResponse response = new ExerciseResponse(entity.getId(), "Squat", "Leg exercise", MuscleGroup.LEGS, true, "JohnDoe", LocalDateTime.MIN, true);
        org.springframework.data.domain.Page<ExerciseEntity> entityPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(entity));

        when(exerciseRepository.findAll(pageable)).thenReturn(entityPage);

        // Se añade el requesterEmail para la firma del mapper
        when(exerciseMapper.toRecord(entity, requesterEmail)).thenReturn(response);

        // Se pasa el requesterEmail a la llamada
        org.springframework.data.domain.Page<ExerciseResponse> resultPage = exerciseService.getAllAccessibleExercises(requesterEmail, "", false, pageable);

        assertNotNull(resultPage, "Page must not be null");
        assertEquals(1, resultPage.getTotalElements(), "Page must contain exactly 1 element");
        assertEquals("Squat", resultPage.getContent().getFirst().name(), "Exercise name must match");

        verify(exerciseRepository, times(1)).findAll(pageable);
        verify(exerciseMapper, times(1)).toRecord(entity, requesterEmail);
    }

    @Test
    @DisplayName("Must return a paginated list of ExerciseResponse filtered by muscle group")
    void getAllExercisesByMuscleGroup_ReturnsPaginatedList() {
        MuscleGroup targetGroup = MuscleGroup.CHEST;
        String requesterEmail = "test@gmail.com";
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        ExerciseEntity entity = new ExerciseEntity();
        entity.setId(UUID.randomUUID());
        entity.setName("Bench Press");
        entity.setMuscleGroup(targetGroup);

        ExerciseResponse response = new ExerciseResponse(entity.getId(), "Bench Press", "Chest exercise", targetGroup, true, "JohnDoe", LocalDateTime.MIN, true);
        org.springframework.data.domain.Page<ExerciseEntity> entityPage = new org.springframework.data.domain.PageImpl<>(java.util.List.of(entity));

        when(exerciseRepository.findByMuscleGroup(targetGroup, pageable)).thenReturn(entityPage);

        // Se añade el requesterEmail al mock del mapper
        when(exerciseMapper.toRecord(entity, requesterEmail)).thenReturn(response);

        // Se añade el requesterEmail a la llamada al servicio
        org.springframework.data.domain.Page<ExerciseResponse> resultPage = exerciseService.getAllExercisesByMuscleGroup(targetGroup, requesterEmail, pageable);

        assertNotNull(resultPage, "Page must not be null");
        assertEquals(1, resultPage.getTotalElements(), "Page must contain exactly 1 element");
        assertEquals(targetGroup, resultPage.getContent().getFirst().muscleGroup(), "Muscle group must match the filter");

        verify(exerciseRepository, times(1)).findByMuscleGroup(targetGroup, pageable);
        verify(exerciseMapper, times(1)).toRecord(entity, requesterEmail);
    }
}