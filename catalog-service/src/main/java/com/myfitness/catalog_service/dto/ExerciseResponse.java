package com.myfitness.catalog_service.dto;

import com.myfitness.catalog_service.entity.enums.MuscleGroup;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExerciseResponse(
        UUID id,
        String name,
        String description,
        MuscleGroup muscleGroup,
        Boolean exercisePrivate,
        String createdBy,
        LocalDateTime createAt,
        Boolean exerciseOwner) implements Serializable {
}
