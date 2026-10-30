package com.myfitness.workout_service.dto.routine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRoutineRequest(

        @NotBlank(message = "Routine's name can not be blank")
        @Size(min = 3, max = 100, message = "Exercise's name must have between 3 and 100 characters")
        String name,

        @NotNull(message = "Exercises' list can not be null")
        List<RoutineExerciseItemDto> routineExerciseItems,

        @NotNull(message = "Privacy flag cannot be null")
        Boolean routinePrivate) {
}
