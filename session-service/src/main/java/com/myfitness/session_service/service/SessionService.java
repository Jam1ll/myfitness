package com.myfitness.session_service.service;

import com.myfitness.session_service.dto.CreateSessionRequest;
import com.myfitness.session_service.entity.SessionEntity;
import com.myfitness.session_service.mapper.SessionMapper;
import com.myfitness.session_service.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;

    @Transactional
    public void saveSession(String userEmail, CreateSessionRequest sessionRequest) {
        SessionEntity session = sessionMapper.toEntity(sessionRequest, userEmail);
        sessionRepository.save(session);
    }
}
