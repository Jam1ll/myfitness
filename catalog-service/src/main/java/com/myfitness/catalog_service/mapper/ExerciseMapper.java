package com.myfitness.catalog_service.mapper;

import com.myfitness.catalog_service.dto.CreateExerciseRequest;
import com.myfitness.catalog_service.dto.ExerciseResponse;
import com.myfitness.catalog_service.dto.UpdateExerciseRequest;
import com.myfitness.catalog_service.entity.ExerciseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExerciseMapper {

    ExerciseResponse toRecord(ExerciseEntity entity);

    ExerciseEntity toEntity(CreateExerciseRequest record);

    void updateEntityFromRequest(UpdateExerciseRequest record, @MappingTarget ExerciseEntity entity);
}
