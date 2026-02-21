package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FindingResponse(
        UUID id,
        UUID releaseId,
        UUID componentId,
        String componentName,
        String componentPurl,
        UUID vulnerabilityId,
        String osvId,
        String summary,
        String details,
        String severity,
        List<String> aliases,
        Instant publishedAt,
        String osvUrl,
        String status,
        Instant detectedAt,
        String source,
        List<FindingDecisionResponse> decisions
) {}
