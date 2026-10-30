package com.myfitness.workout_service.dto.routine;

import java.util.UUID;

public record RoutineExerciseItemDto(
        UUID exerciseId,
        Integer targetSets,
        Integer targetReps,
        Integer restTimeSeconds,
        Integer orderIndex) {
}
