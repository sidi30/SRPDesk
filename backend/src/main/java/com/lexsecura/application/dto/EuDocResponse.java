package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record EuDocResponse(
        UUID id,
        UUID productId,
        String declarationNumber,
        String manufacturerName,
        String manufacturerAddress,
        String authorizedRepName,
        String authorizedRepAddress,
        String productName,
        String productIdentification,
        String conformityAssessmentModule,
        String notifiedBodyName,
        String notifiedBodyNumber,
        String notifiedBodyCertificate,
        String harmonisedStandards,
        String additionalInfo,
        String declarationText,
        String signedBy,
        String signedRole,
        Instant signedAt,
        String status,
        Instant publishedAt,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {}
