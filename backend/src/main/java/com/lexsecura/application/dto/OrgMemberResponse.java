package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record OrgMemberResponse(
        UUID id,
        UUID userId,
        String email,
        String role,
        Instant joinedAt
) {}
