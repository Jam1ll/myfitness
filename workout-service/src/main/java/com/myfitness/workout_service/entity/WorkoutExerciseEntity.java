package com.myfitness.workout_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "workoutExercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private WorkoutEntity workout;

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
