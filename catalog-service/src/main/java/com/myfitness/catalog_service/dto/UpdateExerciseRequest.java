package com.myfitness.catalog_service.dto;

import com.myfitness.catalog_service.entity.enums.MuscleGroup;

import java.time.LocalDateTime;

public record UpdateExerciseRequest(
        String name,
        String description,
        MuscleGroup muscleGroup,
        boolean isPrivate) {
}
