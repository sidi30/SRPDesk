package com.lexsecura.application.dto;

import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CraEventUpdateRequest(
        @Size(max = 500) String title,
        String description,
        @Size(max = 50) String status,
        Instant startedAt,
        Instant detectedAt,
        Instant patchAvailableAt,
        Instant resolvedAt
) {}
