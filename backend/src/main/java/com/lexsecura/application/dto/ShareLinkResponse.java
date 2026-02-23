package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ShareLinkResponse(
        UUID id,
        String token,
        String downloadUrl,
        String recipientEmail,
        String recipientOrg,
        Instant expiresAt,
        int maxDownloads,
        int downloadCount,
        boolean includeVex,
        boolean includeQualityScore,
        boolean revoked,
        Instant createdAt
) {}
