package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RiskAssessmentRequest(
        @NotBlank String title,
        String methodology,
        String summary
) {}
