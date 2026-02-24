package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.AiArtifact;
import com.lexsecura.domain.repository.AiArtifactRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaAiArtifactRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AiArtifactRepositoryAdapter implements AiArtifactRepository {

    private final JpaAiArtifactRepository jpa;
    private final PersistenceMapper mapper;

    public AiArtifactRepositoryAdapter(JpaAiArtifactRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public AiArtifact save(AiArtifact artifact) {
        return mapper.toDomain(jpa.save(mapper.toEntity(artifact)));
    }

    @Override
    public List<AiArtifact> findAllByAiJobId(UUID aiJobId) {
        return jpa.findAllByAiJobId(aiJobId).stream().map(mapper::toDomain).toList();
    }
}
