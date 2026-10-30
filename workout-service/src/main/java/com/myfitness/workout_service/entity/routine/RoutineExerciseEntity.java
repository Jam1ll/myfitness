package com.myfitness.workout_service.entity.routine;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "routine_exercises")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private RoutineEntity routine;

    @Column(name = "exercise_id", nullable = false)
    private UUID exerciseId;

    @Column(name = "target_sets", nullable = false)
    private Integer targetSets;

    @Column(name = "target_reps", nullable = false)
    private Integer targetReps;

    @Column(name = "rest_time_seconds", nullable = false)
    private Integer restTimeSeconds;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}