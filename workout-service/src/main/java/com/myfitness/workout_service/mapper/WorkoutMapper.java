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

    @Mapping(target = "isOwner", expression = "java(entity.getUserEmail().equals(userEmail))")
    WorkoutResponse toRecord(WorkoutEntity entity, String userEmail);

    WorkoutEntity toEntity(CreateWorkoutRequest request);

    void updateEntityFromRequest(UpdateWorkoutRequest record, @MappingTarget WorkoutEntity entity);
}
