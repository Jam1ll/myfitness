package com.myfitness.workout_service.dto;

import java.util.List;
import java.util.UUID;

public record WorkoutResponse(UUID id, String userEmail, String name, List<UUID> exerciseIds) {
}
