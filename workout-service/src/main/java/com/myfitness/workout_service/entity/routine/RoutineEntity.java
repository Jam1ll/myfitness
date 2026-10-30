package com.myfitness.workout_service.entity.routine;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String name;

    @Column(name = "routine_private", nullable = false)
    private boolean routinePrivate;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<RoutineExerciseEntity> routineExerciseEntities = new ArrayList<>();

    //helpers to sync bidirectional relation
    public void addExercise(RoutineExerciseEntity exercise) {
        routineExerciseEntities.add(exercise);
        exercise.setRoutine(this);
    }

    public void clearExercises() {
        routineExerciseEntities.clear();
    }
}
