package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ReleaseResponse(
        UUID id,
        UUID productId,
        String version,
        String gitRef,
        String buildId,
        Instant releasedAt,
        Instant supportedUntil,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}
