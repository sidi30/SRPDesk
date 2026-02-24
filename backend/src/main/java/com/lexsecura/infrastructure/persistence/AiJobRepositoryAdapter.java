package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.AiJob;
import com.lexsecura.domain.repository.AiJobRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaAiJobRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AiJobRepositoryAdapter implements AiJobRepository {

    private final JpaAiJobRepository jpa;
    private final PersistenceMapper mapper;

    public AiJobRepositoryAdapter(JpaAiJobRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public AiJob save(AiJob job) {
        return mapper.toDomain(jpa.save(mapper.toEntity(job)));
    }

    @Override
    public Optional<AiJob> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<AiJob> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<AiJob> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgIdOrderByCreatedAtDesc(orgId).stream().map(mapper::toDomain).toList();
    }
}
