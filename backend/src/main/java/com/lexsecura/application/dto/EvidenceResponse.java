package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record EvidenceResponse(
        UUID id,
        UUID releaseId,
        UUID orgId,
        String type,
        String filename,
        String contentType,
        long size,
        String sha256,
        Instant createdAt,
        UUID createdBy
) {}
