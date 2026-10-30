package com.myfitness.workout_service.mapper;

import com.myfitness.workout_service.dto.program.CreateWeeklyProgramRequest;
import com.myfitness.workout_service.dto.program.ProgramDayResponse;
import com.myfitness.workout_service.dto.program.WeeklyProgramResponse;
import com.myfitness.workout_service.entity.program.ProgramDayEntity;
import com.myfitness.workout_service.entity.program.WeeklyProgramEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WeeklyProgramMapper {

    @Mapping(target = "programDays", source = "entity.programDayEntities")
    @Mapping(target = "programOwner", expression = "java(entity.getUserEmail().equals(userEmail))")
    WeeklyProgramResponse toResponse(WeeklyProgramEntity entity, String userEmail);

    @Mapping(target = "routineName", ignore = true)
    ProgramDayResponse toResponse(ProgramDayEntity entity);

    @Mapping(target = "programDayEntities", ignore = true)
    WeeklyProgramEntity toEntity(CreateWeeklyProgramRequest request);
}