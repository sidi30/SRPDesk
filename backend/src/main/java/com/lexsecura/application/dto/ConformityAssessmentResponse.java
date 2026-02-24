package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ConformityAssessmentResponse(
        UUID id,
        UUID productId,
        String module,
        String status,
        int currentStep,
        int totalSteps,
        String stepsData,
        Instant startedAt,
        Instant completedAt,
        UUID approvedBy,
        Instant approvedAt,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {}
