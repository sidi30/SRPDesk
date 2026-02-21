package com.lexsecura.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrgMemberCreateRequest(
        @NotNull UUID userId,
        @NotBlank @Email String email,
        @NotBlank String role
) {}
