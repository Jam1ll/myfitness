package com.myfitness.session_service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID routineId,
        String name,
        Instant startTime,
        Instant endTime,
        String notes,
        List<SessionExerciseDto> exercises) {
}
