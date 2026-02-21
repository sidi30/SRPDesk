package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        UUID orgId,
        String entityType,
        UUID entityId,
        String action,
        UUID actor,
        String payloadJson,
        Instant createdAt,
        String prevHash,
        String hash
) {}
