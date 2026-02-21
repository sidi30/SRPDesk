package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CraEventLinkResponse(
        UUID id,
        String linkType,
        UUID targetId,
        Instant createdAt
) {}
