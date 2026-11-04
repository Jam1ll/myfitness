package com.myfitness.session_service.dto;

import java.util.List;
import java.util.UUID;

public record SessionExerciseDto(
        UUID id,
        UUID exerciseId,
        Integer orderIndex,
        String notes,
        List<ExerciseSetDto> sets) {
}
