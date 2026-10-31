package com.myfitness.workout_service.repository;

import com.myfitness.workout_service.entity.routine.RoutineEntity;
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
public interface RoutineRepository extends JpaRepository<RoutineEntity, UUID> {

    @Query("SELECT w FROM RoutineEntity w WHERE (w.routinePrivate = false OR w.userEmail = :userEmail) AND LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<RoutineEntity> findAccessibleRoutinesWithSearch(@Param("userEmail") String userEmail, @Param("search") String search, Pageable pageable);

    @Query("SELECT w FROM RoutineEntity w WHERE w.userEmail = :userEmail AND LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<RoutineEntity> findUserRoutinesWithSearch(@Param("userEmail") String userEmail, @Param("search") String search, Pageable pageable);

    //anti-IDOR vulnerability check
    Optional<RoutineEntity> findRoutineByIdAndUserEmail(UUID id, String userEmail);
}