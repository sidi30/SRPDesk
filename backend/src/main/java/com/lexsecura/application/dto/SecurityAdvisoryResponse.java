package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record SecurityAdvisoryResponse(
        UUID id,
        UUID craEventId,
        UUID productId,
        String title,
        String severity,
        String affectedVersions,
        String description,
        String remediation,
        String advisoryUrl,
        String status,
        Instant publishedAt,
        Instant notifiedAt,
        Instant createdAt,
        Instant updatedAt
) {}
