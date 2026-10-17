package com.myfitness.workout_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateWorkoutRequest(

        @NotBlank(message = "Workout's name can not be blank")
        @Size(min = 3, max = 100, message = "Exercise's name must have between 3 and 100 characters")
        String name,

        @NotNull(message = "Exercise list cannot be null")
        List<UUID> exerciseIds,

        @NotNull(message = "Privacy flag cannot be null")
        Boolean workoutPrivate) {
}
