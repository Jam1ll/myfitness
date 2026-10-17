package com.myfitness.workout_service.mapper;

import com.myfitness.workout_service.dto.UpdateWorkoutRequest;
import com.myfitness.workout_service.dto.CreateWorkoutRequest;
import com.myfitness.workout_service.dto.WorkoutResponse;
import com.myfitness.workout_service.entity.WorkoutEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkoutMapper {

    @Mapping(target = "workoutOwner", expression = "java(entity.getUserEmail().equals(userEmail))")
    @Mapping(target = "exerciseIds", expression = "java(entity.getWorkoutExerciseEntities() != null ? entity.getWorkoutExerciseEntities().stream().map(com.myfitness.workout_service.entity.WorkoutExerciseEntity::getExerciseId).toList() : java.util.List.of())")
    WorkoutResponse toRecord(WorkoutEntity entity, String userEmail);

    //we ignore this column so we deal with it on the service
    @Mapping(target = "workoutExerciseEntities", ignore = true)
    WorkoutEntity toEntity(CreateWorkoutRequest request);

    @Mapping(target = "workoutExerciseEntities", ignore = true)
    void updateEntityFromRequest(UpdateWorkoutRequest record, @MappingTarget WorkoutEntity entity);
}