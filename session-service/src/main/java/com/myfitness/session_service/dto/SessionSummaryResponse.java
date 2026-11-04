package com.myfitness.session_service.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionSummaryResponse(
        UUID id,
        UUID routineId,
        String name,
        Instant startTime,
        Instant endTime
) {}