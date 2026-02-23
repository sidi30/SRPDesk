package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CraChecklistItemResponse(
        UUID id,
        UUID productId,
        String requirementRef,
        String category,
        String title,
        String description,
        String status,
        List<UUID> evidenceIds,
        String notes,
        UUID assessedBy,
        Instant assessedAt,
        Instant createdAt,
        Instant updatedAt
) {}
