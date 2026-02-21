package com.lexsecura.application.dto;

public record AuditVerifyResponse(
        boolean valid,
        long totalEvents,
        int verifiedEvents,
        String message
) {}
