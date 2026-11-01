package com.myfitness.workout_service.dto.program;

import com.myfitness.workout_service.entity.enums.DayOfWeek;

import java.io.Serializable;
import java.util.UUID;

public record ProgramDayResponse(
        UUID id,
        DayOfWeek dayOfWeek,
        UUID routineId,
        String routineName
) implements Serializable {
}
