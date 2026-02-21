package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.AiArtifact;

import java.util.List;
import java.util.UUID;

public interface AiArtifactRepository {

    AiArtifact save(AiArtifact artifact);

    List<AiArtifact> findAllByAiJobId(UUID aiJobId);
}
