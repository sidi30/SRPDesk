package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record VexDocumentResponse(
        UUID id,
        UUID releaseId,
        String format,
        int version,
        String status,
        String sha256Hash,
        String generatedBy,
        int statementCount,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {}
