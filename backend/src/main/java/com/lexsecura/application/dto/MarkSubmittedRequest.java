package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarkSubmittedRequest(
        @NotBlank @Size(max = 500) String reference
) {}
