package com.lexsecura.application.dto;

import java.util.UUID;

public record CiSbomUploadResponse(
        UUID releaseId,
        UUID evidenceId,
        int componentCount,
        String sha256,
        boolean releaseCreated
) {}
