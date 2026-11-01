package com.myfitness.workout_service.dto.program;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WeeklyProgramResponse(
        UUID id,
        String name,
        String createdBy,
        LocalDateTime createdAt,
        boolean programOwner,
        List<ProgramDayResponse> programDays,
        boolean weeklyProgramActive
) implements Serializable {
}