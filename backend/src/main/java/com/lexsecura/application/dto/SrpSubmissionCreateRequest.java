package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SrpSubmissionCreateRequest(
        @NotBlank @Size(max = 50) String submissionType
) {}
