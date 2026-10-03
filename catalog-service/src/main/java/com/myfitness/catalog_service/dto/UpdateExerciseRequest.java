package com.myfitness.catalog_service.dto;

import com.myfitness.catalog_service.entity.enums.MuscleGroup;

public record UpdateExerciseRequest(
        String name,
        String description,
        MuscleGroup muscleGroup) {
}
