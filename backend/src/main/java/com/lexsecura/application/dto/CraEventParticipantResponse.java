package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CraEventParticipantResponse(
        UUID id,
        UUID userId,
        String role,
        Instant createdAt
) {}
