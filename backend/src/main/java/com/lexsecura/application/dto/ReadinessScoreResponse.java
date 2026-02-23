package com.lexsecura.application.dto;

import java.util.List;
import java.util.UUID;

public record ReadinessScoreResponse(
        UUID productId,
        int overallScore,
        List<CategoryScore> categories,
        List<String> actionItems
) {
    public record CategoryScore(
            String name,
            int score,
            int maxScore,
            String label
    ) {}
}
