package com.myfitness.workout_service.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExerciseSummaryDto(
        UUID id,
        String name,
        String description,
        String muscleGroup,
        boolean isPrivate,
        String createdBy,
        LocalDateTime createdAt) {
}
