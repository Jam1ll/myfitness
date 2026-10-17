package com.myfitness.workout_service.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorkoutDetailResponse(
        UUID id,
        String name,
        boolean workoutPrivate,
        String createdBy,
        LocalDateTime createdAt,
        boolean workoutOwner,
        List<EnrichedWorkoutExerciseDto> enrichedWorkoutExercises) {
}
