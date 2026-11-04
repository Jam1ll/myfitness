package com.myfitness.session_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionCompletedEvent {
    private UUID sessionId;
    private String userEmail;
    private Instant endTime;
}
