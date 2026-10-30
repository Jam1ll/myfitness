package com.myfitness.workout_service.dto.program;

import com.myfitness.workout_service.entity.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProgramDayItemDto(

        @NotNull(message = "Routine cannot be null")
        UUID routineId,

        @NotNull(message = "Day of the week cannot be null")
        DayOfWeek dayOfWeek) {
}
