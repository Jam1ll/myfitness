package com.myfitness.workout_service.mapper;

import com.myfitness.workout_service.dto.routine.RoutineResponse;
import com.myfitness.workout_service.entity.routine.RoutineEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoutineMapper {

    @Mapping(target = "routineOwner", expression = "java(entity.getUserEmail().equals(userEmail))")
    @Mapping(target = "exerciseIds", expression = "java(entity.getRoutineExerciseEntities() != null ? entity.getRoutineExerciseEntities().stream().map(com.myfitness.workout_service.entity.routine.RoutineExerciseEntity::getExerciseId).toList() : java.util.List.of())")
    RoutineResponse toRecord(RoutineEntity entity, String userEmail);
}