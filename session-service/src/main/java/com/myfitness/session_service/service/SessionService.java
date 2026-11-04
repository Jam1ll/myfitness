package com.myfitness.session_service.service;

import com.myfitness.session_service.dto.CreateSessionRequest;
import com.myfitness.session_service.dto.SessionResponse;
import com.myfitness.session_service.dto.SessionSummaryResponse;
import com.myfitness.session_service.entity.SessionEntity;
import com.myfitness.session_service.event.SessionCompletedEvent;
import com.myfitness.session_service.exception.ResourceNotFoundException;
import com.myfitness.session_service.mapper.SessionMapper;
import com.myfitness.session_service.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String TOPIC_WORKOUT_COMPLETED = "workout-completed-topic";

    // CREATE
    @Transactional
    public void saveSession(String userEmail, CreateSessionRequest sessionRequest) {
        SessionEntity session = sessionMapper.toEntity(sessionRequest, userEmail);
        SessionEntity savedSession = sessionRepository.save(session);
        //event creation
        SessionCompletedEvent event = SessionCompletedEvent.builder()
                .sessionId(savedSession.getId())
                .userEmail(userEmail)
                .endTime(savedSession.getEndTime())
                .build();
        //event emission
        kafkaTemplate.send(TOPIC_WORKOUT_COMPLETED, userEmail, event);
    }

    // DELETE
    @Transactional
    public boolean deleteSession(String userEmail, UUID id) {
        SessionEntity foundSession = sessionRepository.findSessionByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Session with id '" + id + "' not found."));
        sessionRepository.delete(foundSession);
        return true;
    }

    // GET ALL BY USER
    @Transactional(readOnly = true)
    public Page<SessionSummaryResponse> getAllUserSessions(String userEmail, Pageable pageable) {
        Page<SessionEntity> sessionEntities = sessionRepository.findUserSessions(userEmail, pageable);
        return sessionEntities.map(sessionMapper::toSessionSummaryResponse);
    }

    // GET BY ID
    @Transactional(readOnly = true)
    public SessionResponse getUserSessionById(String userEmail, UUID id) {
        SessionEntity foundSession = sessionRepository.findSessionByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Session with id '" + id + "' not found."));
        return sessionMapper.toSessionResponse(foundSession);
    }
}