package com.myfitness.workout_service.repository;

import com.myfitness.workout_service.entity.WorkoutEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutRepository extends JpaRepository<WorkoutEntity, UUID> {

    Page<WorkoutEntity> findAllWorkoutsByUserEmail(String email, Pageable pageable);

    @Modifying
    @Query("DELETE FROM WorkoutEntity w WHERE w.id = :id")
    int deleteWorkoutById(@Param("id") UUID id);

    //anti-IDOR vulnerability check
    Optional<WorkoutEntity> findWorkoutByIdAndUserEmail(UUID id, String userEmail);
}
