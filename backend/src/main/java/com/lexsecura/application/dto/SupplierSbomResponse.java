package com.lexsecura.application.dto;

import java.time.Instant;
import java.util.UUID;

public record SupplierSbomResponse(
        UUID id,
        String supplierName,
        String supplierUrl,
        UUID evidenceId,
        int componentCount,
        String format,
        Instant importedAt
) {}
