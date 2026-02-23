package com.lexsecura.application.dto;

import java.util.UUID;

public record ApiKeyCreateResponse(
        UUID id,
        String name,
        String keyPrefix,
        String plainTextKey
) {}
