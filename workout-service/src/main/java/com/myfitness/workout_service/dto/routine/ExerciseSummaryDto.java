package com.myfitness.workout_service.dto.routine;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExerciseSummaryDto(
        UUID id,
        String name,
        String description,
        String muscleGroup,
        boolean routinePrivate,
        String createdBy,
        LocalDateTime createdAt) {
}
