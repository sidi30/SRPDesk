package com.lexsecura.application.dto;

import jakarta.validation.constraints.NotNull;

public record VexGenerateRequest(
        @NotNull String format // OPENVEX, CYCLONEDX_VEX, CSAF
) {}
