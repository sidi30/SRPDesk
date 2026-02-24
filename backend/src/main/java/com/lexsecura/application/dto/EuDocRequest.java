package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;

public record EuDocRequest(
        @NotBlank String declarationNumber,
        @NotBlank String manufacturerName,
        @NotBlank String manufacturerAddress,
        String authorizedRepName,
        String authorizedRepAddress,
        @NotBlank String productName,
        @NotBlank String productIdentification,
        String conformityAssessmentModule,
        String notifiedBodyName,
        String notifiedBodyNumber,
        String notifiedBodyCertificate,
        String harmonisedStandards,
        String additionalInfo,
        @NotBlank String declarationText,
        @NotBlank String signedBy,
        @NotBlank String signedRole
) {}
