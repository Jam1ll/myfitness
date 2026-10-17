package com.myfitness.workout_service.dto;

import java.util.UUID;

public record EnrichedWorkoutExerciseDto(
        UUID exerciseId,
        String name,
        String description,
        String muscleGroup,
        Integer targetSets,
        Integer targetReps,
        Integer restTimeSeconds,
        Integer orderIndex) {
}
