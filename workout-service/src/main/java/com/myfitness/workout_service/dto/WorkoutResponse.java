package com.myfitness.workout_service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorkoutResponse(
        UUID id,
        String name,
        List<UUID> exerciseIds,
        boolean workoutPrivate,
        String createdBy,
        LocalDateTime createdAt,
        boolean workoutOwner) {
}
