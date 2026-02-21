package com.lexsecura.application.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public final class AiDtos {

    private AiDtos() {}

    public record SrpDraftRequest(
            @NotNull UUID craEventId,
            @NotBlank String submissionType
    ) {}

    public record CommPackRequest(
            @NotNull UUID craEventId
    ) {}

    public record QuestionnaireFillRequest(
            @NotBlank String questionnaireText,
            UUID productId
    ) {}

    public record AiJobResponse(
            UUID id,
            String jobType,
            String status,
            String model,
            String error,
            String createdAt,
            String completedAt,
            java.util.List<AiArtifactResponse> artifacts
    ) {}

    public record AiArtifactResponse(
            UUID id,
            String kind,
            Object contentJson,
            String createdAt
    ) {}
}
