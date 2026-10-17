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

    @Query("SELECT w FROM WorkoutEntity w WHERE (w.workoutPrivate = false OR w.userEmail = :userEmail) AND LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<WorkoutEntity> findAccessibleWorkoutsWithSearch(@Param("userEmail") String userEmail, @Param("search") String search, Pageable pageable);

    @Query("SELECT w FROM WorkoutEntity w WHERE w.userEmail = :userEmail AND LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<WorkoutEntity> findUserWorkoutsWithSearch(@Param("userEmail") String userEmail, @Param("search") String search, Pageable pageable);

    @Modifying
    @Query("DELETE FROM WorkoutEntity w WHERE w.id = :id AND w.userEmail = :userEmail")
    int deleteWorkoutByIdAndUserEmail(@Param("userEmail") String userEmail, @Param("id") UUID id);

    //anti-IDOR vulnerability check
    Optional<WorkoutEntity> findWorkoutByIdAndUserEmail(UUID id, String userEmail);
}
