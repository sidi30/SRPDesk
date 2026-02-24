package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AppliedStandardResponse(
        UUID id,
        UUID productId,
        String standardCode,
        String standardTitle,
        String version,
        String complianceStatus,
        String notes,
        String evidenceIds,
        Instant createdAt,
        Instant updatedAt
) {}
