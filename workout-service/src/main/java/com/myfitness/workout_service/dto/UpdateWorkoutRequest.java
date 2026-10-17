package com.myfitness.workout_service.dto;

import java.util.List;

public record UpdateWorkoutRequest(
        String name,
        List<WorkoutExerciseItemDto> workoutExerciseItems,
        Boolean workoutPrivate) {
}
