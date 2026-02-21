package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.AiArtifact;
import com.lexsecura.domain.repository.AiArtifactRepository;
import com.lexsecura.infrastructure.persistence.entity.AiArtifactEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaAiArtifactRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AiArtifactRepositoryAdapter implements AiArtifactRepository {

    private final JpaAiArtifactRepository jpa;

    public AiArtifactRepositoryAdapter(JpaAiArtifactRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public AiArtifact save(AiArtifact artifact) {
        return toDomain(jpa.save(toEntity(artifact)));
    }

    @Override
    public List<AiArtifact> findAllByAiJobId(UUID aiJobId) {
        return jpa.findAllByAiJobId(aiJobId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private AiArtifact toDomain(AiArtifactEntity e) {
        AiArtifact m = new AiArtifact();
        m.setId(e.getId());
        m.setAiJobId(e.getAiJobId());
        m.setKind(e.getKind());
        m.setContentJson(e.getContentJson());
        m.setCreatedAt(e.getCreatedAt());
        return m;
    }

    private AiArtifactEntity toEntity(AiArtifact m) {
        AiArtifactEntity e = new AiArtifactEntity();
        e.setId(m.getId());
        e.setAiJobId(m.getAiJobId());
        e.setKind(m.getKind());
        e.setContentJson(m.getContentJson());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }
}
