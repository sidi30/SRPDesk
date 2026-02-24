package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record RiskItemResponse(
        UUID id,
        UUID riskAssessmentId,
        String threatCategory,
        String threatDescription,
        String affectedAsset,
        String likelihood,
        String impact,
        String riskLevel,
        String existingControls,
        String mitigationPlan,
        String mitigationStatus,
        String residualRiskLevel,
        Instant createdAt,
        Instant updatedAt
) {}
