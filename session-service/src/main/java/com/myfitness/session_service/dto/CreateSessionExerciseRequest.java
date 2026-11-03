package com.myfitness.session_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateSessionExerciseRequest(
        @NotNull UUID exerciseId,
        @NotNull Integer orderIndex,
        String notes,
        @NotNull List<CreateExerciseSetRequest> sets
) {}