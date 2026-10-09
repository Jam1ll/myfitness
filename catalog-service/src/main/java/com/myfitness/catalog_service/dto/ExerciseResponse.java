package com.myfitness.catalog_service.dto;

import com.myfitness.catalog_service.entity.enums.MuscleGroup;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExerciseResponse(
        UUID id,
        String name,
        String description,
        MuscleGroup muscleGroup,
        boolean isPrivate,
        String createdBy,
        LocalDateTime createAt,
        boolean isOwner) {
}
