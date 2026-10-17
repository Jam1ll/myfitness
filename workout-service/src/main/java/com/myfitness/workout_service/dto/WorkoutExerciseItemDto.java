package com.myfitness.workout_service.dto;

import java.util.UUID;

public record WorkoutExerciseItemDto(
        UUID exerciseId,
        Integer targetSets,
        Integer targetReps,
        Integer restTimeSeconds,
        Integer orderIndex) {
}
