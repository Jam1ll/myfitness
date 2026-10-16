package com.myfitness.catalog_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.myfitness.catalog_service.entity.enums.MuscleGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateExerciseRequest(

        @NotBlank(message = "Exercise's name can not be blank")
        @Size(min = 3, max = 100, message = "Exercise's name must have between 3 and 100 characters")
        String name,

        @NotBlank(message = "Exercise's description can not be blank")
        @Size(min = 10, max = 500, message = "Exercise's description must have between 10 and 500 characters")
        String description,

        @NotNull(message = "Exercise's muscle group can not be null")
        MuscleGroup muscleGroup,

        boolean exercisePrivate) {
}
