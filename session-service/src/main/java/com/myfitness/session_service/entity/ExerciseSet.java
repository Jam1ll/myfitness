package com.myfitness.session_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "exercise_set")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExerciseSet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_exercise_id", nullable = false)
    private SessionExerciseEntity sessionExerciseEntity;

    @Column(name = "set_number", nullable = false)
    private Integer setNumber;

    private Integer reps;

    private Double weight;

    private Double rpe;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;
}
