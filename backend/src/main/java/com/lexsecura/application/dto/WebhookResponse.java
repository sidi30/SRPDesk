package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record WebhookResponse(
        UUID id,
        String name,
        String url,
        String eventTypes,
        String channelType,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
