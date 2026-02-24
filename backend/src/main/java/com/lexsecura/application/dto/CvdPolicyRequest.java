package com.lexsecura.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CvdPolicyRequest(
        @NotBlank @Email String contactEmail,
        String contactUrl,
        String pgpKeyUrl,
        String policyUrl,
        @Min(7) Integer disclosureTimelineDays,
        Boolean acceptsAnonymous,
        String bugBountyUrl,
        String acceptedLanguages,
        String scopeDescription
) {}
