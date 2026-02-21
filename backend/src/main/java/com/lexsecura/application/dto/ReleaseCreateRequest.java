package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ReleaseCreateRequest(
        @NotBlank @Size(max = 100) String version,
        @Size(max = 255) String gitRef,
        @Size(max = 255) String buildId,
        Instant releasedAt,
        Instant supportedUntil
) {}
