package com.myfitness.catalog_service.mapper;

import com.myfitness.catalog_service.dto.CreateExerciseRequest;
import com.myfitness.catalog_service.dto.ExerciseResponse;
import com.myfitness.catalog_service.dto.UpdateExerciseRequest;
import com.myfitness.catalog_service.entity.ExerciseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

    @Mapping(target = "exerciseOwner", expression = "java(entity.getUserEmail().equals(userEmail))")
    ExerciseResponse toRecord(ExerciseEntity entity, String userEmail);

    @Mapping(target = "exercisePrivate", expression = "java(record.exercisePrivate() != null ? record.exercisePrivate() : false)")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userEmail", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ExerciseEntity toEntity(CreateExerciseRequest record);

    @Mapping(target = "exercisePrivate", nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateExerciseRequest record, @MappingTarget ExerciseEntity entity);
}