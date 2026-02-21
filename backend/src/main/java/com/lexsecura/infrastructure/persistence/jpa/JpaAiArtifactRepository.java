package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.AiArtifactEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaAiArtifactRepository extends JpaRepository<AiArtifactEntity, UUID> {

    List<AiArtifactEntity> findAllByAiJobId(UUID aiJobId);
}
