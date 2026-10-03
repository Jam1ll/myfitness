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

    @Modifying
    @Query("DELETE FROM ExerciseEntity e WHERE e.id = :id")
    int deleteExerciseById(@Param("id") UUID id);

    //Batching (optimization technique)
    long countByIdIn(List<UUID> ids);
}
