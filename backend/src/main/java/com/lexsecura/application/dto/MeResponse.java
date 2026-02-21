package com.lexsecura.application.dto;

import java.util.List;
import java.util.UUID;

public record MeResponse(
        UUID userId,
        String email,
        UUID currentOrgId,
        List<String> roles,
        List<OrgSummary> organizations
) {
    public record OrgSummary(
            UUID orgId,
            String name,
            String role
    ) {}
}
