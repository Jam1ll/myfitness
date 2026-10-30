package com.myfitness.workout_service.dto.routine;

import java.util.List;

public record UpdateRoutineRequest(
        String name,
        List<RoutineExerciseItemDto> routineExerciseItems,
        Boolean routinePrivate) {
}
