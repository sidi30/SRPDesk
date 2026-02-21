package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record FindingDecisionRequest(
        @NotBlank String decisionType,
        @NotBlank String rationale,
        LocalDate dueDate,
        UUID fixReleaseId
) {}
