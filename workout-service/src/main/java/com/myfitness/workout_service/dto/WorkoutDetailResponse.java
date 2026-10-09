package com.myfitness.workout_service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorkoutDetailResponse(
        UUID id,
        String name,
        List<ExerciseSummaryDto> exercises,
        boolean isPrivate,
        String createdBy,
        LocalDateTime createdAt,
        boolean isOwner) {
}
