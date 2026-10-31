package com.myfitness.workout_service.repository;

import com.myfitness.workout_service.entity.program.WeeklyProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeeklyProgramRepository extends JpaRepository<WeeklyProgramEntity, UUID> {

    List<WeeklyProgramEntity> findAllByUserEmail(String userEmail);

    Optional<WeeklyProgramEntity> findByIdAndUserEmail(UUID id, String userEmail);

    Optional<WeeklyProgramEntity> findByIdAndUserEmailAndWeeklyProgramActiveTrue(UUID id, String userEmail);
}