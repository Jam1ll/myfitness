package com.myfitness.catalog_service.dto;

import com.myfitness.catalog_service.entity.enums.MuscleGroup;

import java.util.UUID;

public record ExerciseResponse(
        UUID id,
        String name,
        String description,
        MuscleGroup muscleGroup) {
}
