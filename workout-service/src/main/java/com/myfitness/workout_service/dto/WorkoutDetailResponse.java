package com.myfitness.workout_service.dto;

import java.util.List;
import java.util.UUID;

public record WorkoutDetailResponse(UUID id, String userEmail, String name, List<ExerciseSummaryDto> exercises) {
}
