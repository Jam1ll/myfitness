package com.myfitness.workout_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String name;

    @Column(name = "workout_private", nullable = false)
    private boolean workoutPrivate;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<WorkoutExerciseEntity> workoutExerciseEntities = new ArrayList<>();

    //helpers to sync bidirectional relation
    public void addExercise(WorkoutExerciseEntity exercise) {
        workoutExerciseEntities.add(exercise);
        exercise.setWorkout(this);
    }

    public void clearExercises() {
        workoutExerciseEntities.clear();
    }
}
