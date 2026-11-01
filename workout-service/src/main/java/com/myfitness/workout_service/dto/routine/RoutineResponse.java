package com.myfitness.workout_service.dto.routine;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoutineResponse(
        UUID id,
        String name,
        List<UUID> exerciseIds,
        boolean routinePrivate,
        String createdBy,
        LocalDateTime createdAt,
        boolean routineOwner) implements Serializable {
}
