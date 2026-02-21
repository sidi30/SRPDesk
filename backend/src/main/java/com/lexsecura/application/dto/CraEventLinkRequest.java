package com.lexsecura.application.dto;

import java.util.List;
import java.util.UUID;

public record CraEventLinkRequest(
        List<UUID> releaseIds,
        List<UUID> findingIds,
        List<UUID> evidenceIds
) {}
