package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record SrpSubmissionResponse(
        UUID id,
        UUID craEventId,
        String submissionType,
        String status,
        Object contentJson,
        String schemaVersion,
        Object validationErrors,
        String submittedReference,
        Instant submittedAt,
        UUID acknowledgmentEvidenceId,
        UUID generatedBy,
        Instant generatedAt,
        Instant updatedAt
) {}
