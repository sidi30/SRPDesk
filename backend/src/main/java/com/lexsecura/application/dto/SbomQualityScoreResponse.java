package com.lexsecura.application.dto;

import java.util.List;

public record SbomQualityScoreResponse(
        int totalScore,
        String grade,
        int componentCount,
        List<CriterionResult> criteria
) {
    public record CriterionResult(
            String id,
            String label,
            int maxPoints,
            int score,
            double coverage,
            String detail
    ) {}
}
