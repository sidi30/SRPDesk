package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SecurityAdvisoryCreateRequest(
        @NotNull UUID craEventId,
        @NotBlank @Size(max = 500) String title,
        @NotBlank String severity,
        String affectedVersions,
        @NotBlank String description,
        String remediation
) {}
