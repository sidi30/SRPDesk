package com.lexsecura.application.dto;

import java.util.UUID;

public record CiSbomEnrichedResponse(
        UUID releaseId,
        int componentCount,
        int newComponents,
        int removedComponents,
        int qualityScore,
        String qualityGrade,
        VulnSummary vulnerabilities,
        int newVulnerabilities,
        String policyResult,
        String sha256,
        String detailsUrl
) {
    public record VulnSummary(
            int critical,
            int high,
            int medium,
            int low,
            int total
    ) {}
}
