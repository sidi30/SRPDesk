package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RiskAssessmentResponse(
        UUID id,
        UUID productId,
        String title,
        String methodology,
        String status,
        String overallRiskLevel,
        String summary,
        UUID approvedBy,
        Instant approvedAt,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt,
        List<RiskItemResponse> items
) {}
