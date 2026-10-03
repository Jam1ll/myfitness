package com.myfitness.workout_service.mapper;

import com.myfitness.workout_service.dto.UpdateWorkoutRequest;
import com.myfitness.workout_service.dto.CreateWorkoutRequest;
import com.myfitness.workout_service.dto.WorkoutResponse;
import com.myfitness.workout_service.entity.WorkoutEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkoutMapper {

    WorkoutResponse toRecord(WorkoutEntity entity);

    WorkoutEntity toEntity(CreateWorkoutRequest request);

    void updateEntityFromRequest(UpdateWorkoutRequest record, @MappingTarget WorkoutEntity entity);
}
