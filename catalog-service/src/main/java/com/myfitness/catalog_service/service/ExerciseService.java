package com.myfitness.catalog_service.service;

import com.myfitness.catalog_service.dto.CreateExerciseRequest;
import com.myfitness.catalog_service.dto.ExerciseResponse;
import com.myfitness.catalog_service.dto.UpdateExerciseRequest;
import com.myfitness.catalog_service.entity.ExerciseEntity;
import com.myfitness.catalog_service.entity.enums.MuscleGroup;
import com.myfitness.catalog_service.exception.ResourceNotFoundException;
import com.myfitness.catalog_service.mapper.ExerciseMapper;
import com.myfitness.catalog_service.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    //CREATE
    public ExerciseResponse createExercise(String userEmail, String userName, CreateExerciseRequest request) {
        ExerciseEntity entity = exerciseMapper.toEntity(request);
        entity.setUserEmail(userEmail);
        entity.setCreatedBy(userName);
        return exerciseMapper.toRecord(exerciseRepository.save(entity), userEmail);
    }

    //UPDATE
    public ExerciseResponse updateExercise(UUID id, UpdateExerciseRequest request) {
        ExerciseEntity foundEntity = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise with id '" + id + "' not found"));

        exerciseMapper.updateEntityFromRequest(request, foundEntity);

        ExerciseEntity updatedEntity = exerciseRepository.save(foundEntity);

        // Pasamos el email del dueño original para la respuesta
        return exerciseMapper.toRecord(updatedEntity, foundEntity.getUserEmail());
    }

    //DELETE
    @Transactional
    public boolean deleteExercise(UUID id) {
        int rowsAffected = exerciseRepository.deleteExerciseById(id);
        if (rowsAffected == 0)
            throw new ResourceNotFoundException("Exercise with id '" + id + "' not found");
        return rowsAffected > 0;
    }

    //MASSIVE VALIDATION (Batching)
    public boolean validateExercises(List<UUID> exerciseIds){
        //clean duplicates
        List<UUID> uniqueIds = exerciseIds.stream().distinct().toList();
        //count how many of the IDs exist in the DB
        long foundCount = exerciseRepository.countByIdIn(uniqueIds);
        //if the quantity's the same, as delivered, return true;
        return foundCount == uniqueIds.size();
    }

    //GET ALL
    public Page<ExerciseResponse> getAllAccessibleExercises(String userEmail, String search, boolean mineOnly, Pageable pageable) {
        Page<ExerciseEntity> entities;

        if (mineOnly) {
            entities = exerciseRepository.findUserExercisesWithSearch(userEmail, search, pageable);
        } else {
            entities = exerciseRepository.findAccessibleExercisesWithSearch(userEmail, search, pageable);
        }

        return entities.map(entity -> exerciseMapper.toRecord(entity, userEmail));
    }

    //GET BY MUSCLE GROUP
    public Page<ExerciseResponse> getAllExercisesByMuscleGroup(MuscleGroup muscleGroup, String userEmail, Pageable pageable) {
        return exerciseRepository.findByMuscleGroup(muscleGroup, pageable).map(entity -> exerciseMapper.toRecord(entity, userEmail));
    }

    //GET BY ID
    public ExerciseResponse getExerciseById(UUID id, String userEmail) {
        return exerciseRepository.findById(id)
                .map(entity -> exerciseMapper.toRecord(entity, userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Exercise with id '" + id + "' not found"));
    }

    //GET BY NAME
    public ExerciseResponse getExerciseByName(String name, String userEmail) {
        return exerciseRepository.findByName(name)
                .map(entity -> exerciseMapper.toRecord(entity, userEmail))
                .orElseThrow(() -> new ResourceNotFoundException("Exercise with name '" + name + "' not found"));
    }

    //GET ALL BY IDS (BATCHING)
    public List<ExerciseResponse> getExercisesByIds(List<UUID> ids, String userEmail){
        return exerciseRepository.findAllById(ids)
                .stream()
                .map(entity -> exerciseMapper.toRecord(entity, userEmail))
                .toList();
    }
}