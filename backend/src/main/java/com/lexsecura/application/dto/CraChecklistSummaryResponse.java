package com.lexsecura.application.dto;

import java.util.Map;
import java.util.UUID;

public record CraChecklistSummaryResponse(
        UUID productId,
        int totalItems,
        int compliant,
        int partiallyCompliant,
        int nonCompliant,
        int notAssessed,
        Map<String, CategorySummary> categories
) {
    public record CategorySummary(
            int total,
            int compliant,
            int partiallyCompliant,
            int nonCompliant,
            int notAssessed
    ) {}
}
