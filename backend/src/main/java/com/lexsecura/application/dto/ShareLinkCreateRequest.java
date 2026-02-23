package com.lexsecura.application.dto;

public record ShareLinkCreateRequest(
        String recipientEmail,
        String recipientOrg,
        int expiresInHours,
        int maxDownloads,
        boolean includeVex,
        boolean includeQualityScore
) {}
