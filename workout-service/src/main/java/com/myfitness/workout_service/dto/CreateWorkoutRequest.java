package com.myfitness.workout_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateWorkoutRequest(

        @NotBlank(message = "Workout's name can not be blank")
        @Size(min = 3, max = 100, message = "Exercise's name must have between 3 and 100 characters")
        String name,

        @NotEmpty(message = "There should be at least 1 valid exercise")
        List<UUID> exerciseIds,

        boolean isPrivate) {
}
