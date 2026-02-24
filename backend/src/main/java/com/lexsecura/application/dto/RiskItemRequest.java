package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RiskItemRequest(
        @NotBlank String threatCategory,
        @NotBlank String threatDescription,
        String affectedAsset,
        @NotBlank String likelihood,
        @NotBlank String impact,
        String existingControls,
        String mitigationPlan
) {}
