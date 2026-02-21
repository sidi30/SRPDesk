package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CraEventParticipantRequest(
        @NotNull UUID userId,
        @NotBlank @Size(max = 50) String role
) {}
