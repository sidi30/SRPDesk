package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CvdPolicyResponse(
        UUID id,
        UUID productId,
        String contactEmail,
        String contactUrl,
        String pgpKeyUrl,
        String policyUrl,
        int disclosureTimelineDays,
        boolean acceptsAnonymous,
        String bugBountyUrl,
        String acceptedLanguages,
        String scopeDescription,
        String status,
        Instant publishedAt,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {}
