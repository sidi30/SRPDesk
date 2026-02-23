package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WebhookCreateRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 2048) String url,
        String secret,
        String eventTypes,
        @NotBlank String channelType
) {}
