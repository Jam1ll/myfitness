package com.myfitness.session_service.mapper;

import com.myfitness.session_service.dto.CreateSessionRequest;
import com.myfitness.session_service.dto.CreateSessionExerciseRequest;
import com.myfitness.session_service.dto.CreateExerciseSetRequest;
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
                    .map(exRequest -> toExerciseEntity(exRequest, session)) // Pasamos el padre
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
                    .map(setRequest -> toSetEntity(setRequest, exercise)) // Pasamos el padre (ejercicio)
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
}