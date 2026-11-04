package com.myfitness.session_service.dto;

import java.util.UUID;

public record ExerciseSetDto(
        UUID id,
        Integer setNumber,
        Integer reps,
        Double weight,
        Double rpe,
        Boolean completed) {
}
