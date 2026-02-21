package com.lexsecura.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FindingDecisionResponse(
        UUID id,
        String decisionType,
        String rationale,
        LocalDate dueDate,
        UUID decidedBy,
        UUID fixReleaseId,
        Instant createdAt
) {}
