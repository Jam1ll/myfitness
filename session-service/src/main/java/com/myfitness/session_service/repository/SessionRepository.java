package com.myfitness.session_service.repository;

import com.myfitness.session_service.entity.SessionEntity;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {
    Optional<SessionEntity> findSessionByIdAndUserEmail(UUID id, String userEmail);

    @Query("SELECT s FROM SessionEntity WHERE s.userEmail = :userEmail")
    Page<SessionEntity> findUserSessions(@Param("userEmail") String userEmail, Pageable pageable);
}
