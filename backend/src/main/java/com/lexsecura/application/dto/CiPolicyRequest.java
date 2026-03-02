package com.lexsecura.application.dto;

public record CiPolicyRequest(
        int maxCritical,
        int maxHigh,
        int minQualityScore,
        boolean blockOnFail
) {}
