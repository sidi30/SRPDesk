package com.lexsecura.application.dto;

import java.time.Instant;
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
        // New global metrics
        int totalVulnerabilities,
        int productsWithEuDoc,
        int productsFullyCompliant,
        int automationScore,
        // Alerts
        List<Alert> alerts,
        int alertsCritical,
        int alertsHigh,
        int alertsMedium,
        // Product readiness
        List<ProductReadiness> productReadiness
) {
    public record ProductReadiness(
            String productId,
            String productName,
            String type,
            String conformityPath,
            int readinessScore,
            int checklistTotal,
            int checklistCompliant,
            Instant lastCiUploadAt,
            String sbomFreshness,
            Integer lastQualityScore,
            String lastPolicyResult,
            // Enriched fields
            int openFindingsCount,
            int criticalFindingsCount,
            String conformityStatus,
            int conformityProgress,
            String riskLevel,
            String riskStatus,
            String euDocStatus,
            Instant supportedUntil,
            int releaseCount,
            String latestVersion
    ) {}

    public record Alert(
            String type,
            String severity,
            String productId,
            String productName,
            String message,
            Instant detectedAt
    ) {}
}
