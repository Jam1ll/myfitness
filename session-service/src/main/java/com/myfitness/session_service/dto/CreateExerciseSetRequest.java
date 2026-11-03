package com.myfitness.session_service.dto;

import jakarta.validation.constraints.NotNull;

public record CreateExerciseSetRequest(
        @NotNull Integer setNumber,
        Integer reps,
        Double weight,
        Double rpe,
        @NotNull Boolean completed
) {}