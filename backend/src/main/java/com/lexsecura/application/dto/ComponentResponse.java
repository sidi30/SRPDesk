package com.lexsecura.application.dto;

import java.util.UUID;

public record ComponentResponse(
        UUID id,
        String purl,
        String name,
        String version,
        String type
) {}
