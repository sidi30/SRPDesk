package com.lexsecura.application.dto;

import java.util.List;

public record DashboardResponse(
        int totalProducts,
        int totalReleases,
        int totalFindings,
        int openFindings,
        int criticalHighFindings,
        int totalCraEvents,
        int activeCraEvents,
        double averageReadinessScore,
        List<ProductReadiness> productReadiness
) {
    public record ProductReadiness(
            String productId,
            String productName,
            String type,
            String conformityPath,
            int readinessScore,
            int checklistTotal,
            int checklistCompliant
    ) {}
}
