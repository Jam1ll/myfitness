package com.myfitness.workout_service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UpdateWorkoutRequest(
        String name,
        List<UUID> exerciseIds,
        boolean isPrivate) {
}
