package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CraEventCreateRequest(
        @NotNull UUID productId,
        @NotBlank @Size(max = 50) String eventType,
        @NotBlank @Size(max = 500) String title,
        String description,
        Instant startedAt,
        @NotNull Instant detectedAt
) {}
