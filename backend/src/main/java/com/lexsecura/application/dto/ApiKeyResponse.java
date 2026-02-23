package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String name,
        String keyPrefix,
        String scopes,
        Instant createdAt,
        Instant lastUsedAt,
        boolean revoked,
        Instant revokedAt
) {}
