package com.myfitness.session_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateSessionRequest(
        UUID routineId, // Nullable (entrenamiento libre)

        @NotBlank(message = "Session name is required")
        String name,

        @NotNull(message = "Start time is required")
        Instant startTime,

        @NotNull(message = "End time is required")
        Instant endTime,

        String notes,

        @NotNull
        List<CreateSessionExerciseRequest> exercises
) {}