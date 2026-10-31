package com.myfitness.workout_service.dto.program;

import java.util.List;

public record UpdateWeeklyProgramRequest(
        String name,
        List<ProgramDayItemDto> programDays,
        boolean weeklyProgramActive) {
}
