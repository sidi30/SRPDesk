package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AppliedStandardRequest(
        @NotBlank String standardCode,
        @NotBlank String standardTitle,
        String version,
        String complianceStatus,
        String notes,
        String evidenceIds
) {}
