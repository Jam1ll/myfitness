package com.myfitness.workout_service.dto.routine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoutineDetailResponse(
        UUID id,
        String name,
        boolean routinePrivate,
        String createdBy,
        LocalDateTime createdAt,
        boolean routineOwner,
        List<EnrichedRoutineExerciseDto> enrichedRoutineExercises) {
}
