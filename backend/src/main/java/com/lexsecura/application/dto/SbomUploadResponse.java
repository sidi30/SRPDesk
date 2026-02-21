package com.lexsecura.application.dto;

import java.util.UUID;

public record SbomUploadResponse(
        UUID evidenceId,
        int componentCount,
        String sha256
) {}
