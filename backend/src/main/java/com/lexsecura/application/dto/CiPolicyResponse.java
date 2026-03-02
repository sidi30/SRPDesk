package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CiPolicyResponse(
        UUID id,
        int maxCritical,
        int maxHigh,
        int minQualityScore,
        boolean blockOnFail,
        Instant createdAt,
        Instant updatedAt
) {}
