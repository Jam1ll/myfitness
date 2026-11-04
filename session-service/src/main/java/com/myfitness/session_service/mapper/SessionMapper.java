package com.myfitness.session_service.mapper;

import com.myfitness.session_service.dto.*; // Simplifiqué los imports
import com.myfitness.session_service.entity.SessionEntity;
import com.myfitness.session_service.entity.SessionExerciseEntity;
import com.myfitness.session_service.entity.ExerciseSet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SessionMapper {

    public SessionEntity toEntity(CreateSessionRequest request, String userEmail) {
        SessionEntity session = SessionEntity.builder()
                .userEmail(userEmail)
                .routineId(request.routineId())
                .name(request.name())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .notes(request.notes())
                .build();

        if (request.exercises() != null) {
            List<SessionExerciseEntity> exercises = request.exercises().stream()
                    .map(exRequest -> toExerciseEntity(exRequest, session))
                    .collect(Collectors.toList());

            session.setSessionExerciseEntities(exercises);
        }

        return session;
    }

    private SessionExerciseEntity toExerciseEntity(CreateSessionExerciseRequest request, SessionEntity parentSession) {
        SessionExerciseEntity exercise = SessionExerciseEntity.builder()
                .sessionEntity(parentSession)
                .exerciseId(request.exerciseId())
                .orderIndex(request.orderIndex())
                .notes(request.notes())
                .build();

        if (request.sets() != null) {
            List<ExerciseSet> sets = request.sets().stream()
                    .map(setRequest -> toSetEntity(setRequest, exercise))
                    .collect(Collectors.toList());

            exercise.setExerciseSets(sets);
        }

        return exercise;
    }

    private ExerciseSet toSetEntity(CreateExerciseSetRequest request, SessionExerciseEntity parentExercise) {
        return ExerciseSet.builder()
                .sessionExerciseEntity(parentExercise)
                .setNumber(request.setNumber())
                .reps(request.reps())
                .weight(request.weight())
                .rpe(request.rpe())
                .completed(request.completed())
                .build();
    }

    public SessionResponse toSessionResponse(SessionEntity entity) {
        List<SessionExerciseDto> exerciseDtos = entity.getSessionExerciseEntities() != null
                ? entity.getSessionExerciseEntities().stream()
                .map(this::toSessionExerciseDto)
                .toList() // En Java 16+ esto reemplaza a Collectors.toList()
                : List.of();

        return new SessionResponse(
                entity.getId(),
                entity.getRoutineId(),
                entity.getName(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getNotes(),
                exerciseDtos
        );
    }

    public SessionSummaryResponse toSessionSummaryResponse(SessionEntity entity) {
        return new SessionSummaryResponse(
                entity.getId(),
                entity.getRoutineId(),
                entity.getName(),
                entity.getStartTime(),
                entity.getEndTime()
        );
    }

    private SessionExerciseDto toSessionExerciseDto(SessionExerciseEntity entity) {
        List<ExerciseSetDto> setDtos = entity.getExerciseSets() != null
                ? entity.getExerciseSets().stream()
                .map(this::toExerciseSetDto)
                .toList()
                : List.of();

        return new SessionExerciseDto(
                entity.getId(),
                entity.getExerciseId(),
                entity.getOrderIndex(),
                entity.getNotes(),
                setDtos
        );
    }

    private ExerciseSetDto toExerciseSetDto(ExerciseSet entity) {
        return new ExerciseSetDto(
                entity.getId(),
                entity.getSetNumber(),
                entity.getReps(),
                entity.getWeight(),
                entity.getRpe(),
                entity.getCompleted()
        );
    }
}