package com.myfitness.workout_service.dto;

import java.util.UUID;

public record ExerciseSummaryDto(UUID id, String name, String description, String muscleGroup) {
}
