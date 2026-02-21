package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID orgId,
        String name,
        String type,
        String criticality,
        List<Map<String, String>> contacts,
        Instant createdAt,
        Instant updatedAt
) {}
