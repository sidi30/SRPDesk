package com.lexsecura.application.dto;

public record AuditVerifyResponse(
        boolean valid,
        int totalEvents,
        int verifiedEvents,
        String message
) {}
