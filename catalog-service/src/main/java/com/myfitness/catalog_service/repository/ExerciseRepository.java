package com.myfitness.catalog_service.repository;

import com.myfitness.catalog_service.entity.ExerciseEntity;
import com.myfitness.catalog_service.entity.enums.MuscleGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, UUID> {
    Optional<ExerciseEntity> findByName(String name);

    Page<ExerciseEntity> findByMuscleGroup(MuscleGroup muscleGroup, Pageable pageable);

    @Query("SELECT e from ExerciseEntity e where e.exercisePrivate = false OR e.userEmail = :userEmail")
    Page<ExerciseEntity> findPublicOrUserOwned(@Param("userEmail") String userEmail, Pageable pageable);

    // Public and owned exercises filtered by name
    @Query("SELECT e FROM ExerciseEntity e WHERE (e.exercisePrivate = false OR e.userEmail = :userEmail) AND LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ExerciseEntity> findAccessibleExercisesWithSearch(@Param("userEmail") String userEmail, @Param("search") String search, Pageable pageable);

    // Only Owner exercises filtered by name
    @Query("SELECT e FROM ExerciseEntity e WHERE e.userEmail = :userEmail AND LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<ExerciseEntity> findUserExercisesWithSearch(@Param("userEmail") String userEmail, @Param("search") String search, Pageable pageable);

    @Modifying
    @Query("DELETE FROM ExerciseEntity e WHERE e.id = :id")
    int deleteExerciseById(@Param("id") UUID id);

    //Batching (optimization technique)
    long countByIdIn(List<UUID> ids);
}
