package com.myfitness.workout_service.repository;

import com.myfitness.workout_service.entity.program.WeeklyProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeeklyProgramRepository extends JpaRepository<WeeklyProgramEntity, UUID> {

    List<WeeklyProgramEntity> findAllByUserEmail(String userEmail);

    Optional<WeeklyProgramEntity> findByIdAndUserEmail(UUID id, String userEmail);

    @Modifying
    @Query("DELETE FROM WeeklyProgramEntity wpe WHERE wpe.id = :id AND wpe.userEmail = :userEmail")
    int deleteByIdAndUserEmail(@Param("userEmail") String userEmail, @Param("id") UUID id);
}