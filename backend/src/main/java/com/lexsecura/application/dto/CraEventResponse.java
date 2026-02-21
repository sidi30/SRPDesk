package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CraEventResponse(
        UUID id,
        UUID orgId,
        UUID productId,
        String productName,
        String eventType,
        String title,
        String description,
        String status,
        Instant startedAt,
        Instant detectedAt,
        Instant patchAvailableAt,
        Instant resolvedAt,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt,
        List<CraEventParticipantResponse> participants,
        List<CraEventLinkResponse> links
) {}
