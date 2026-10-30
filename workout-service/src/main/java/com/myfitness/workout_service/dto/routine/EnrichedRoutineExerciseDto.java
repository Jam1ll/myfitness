package com.myfitness.workout_service.dto.routine;

import java.util.UUID;

public record EnrichedRoutineExerciseDto(
        UUID exerciseId,
        String name,
        String description,
        String muscleGroup,
        Integer targetSets,
        Integer targetReps,
        Integer restTimeSeconds,
        Integer orderIndex) {
}
