package com.lexsecura.application.dto;

public record CiScanResponse(
        int componentCount,
        int qualityScore,
        String qualityGrade,
        CiSbomEnrichedResponse.VulnSummary vulnerabilities,
        String policyResult,
        String sha256
) {}
