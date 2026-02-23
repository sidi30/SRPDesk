package com.lexsecura.application.dto;

import java.util.List;
import java.util.UUID;

public record CraChecklistUpdateRequest(
        String status,
        String notes,
        List<UUID> evidenceIds
) {}
