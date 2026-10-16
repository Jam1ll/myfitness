package com.myfitness.workout_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "workout_exercises", joinColumns = @JoinColumn(name = "workout_id"))
    @Column(name = "exercise_id")
    private List<UUID> exerciseIds;

    @Column(nullable = false)
    private boolean workoutPrivate;

    @Column(nullable = false)
    private String createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
