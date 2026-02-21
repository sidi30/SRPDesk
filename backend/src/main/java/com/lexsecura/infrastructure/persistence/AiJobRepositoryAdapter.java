package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.AiJob;
import com.lexsecura.domain.repository.AiJobRepository;
import com.lexsecura.infrastructure.persistence.entity.AiJobEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaAiJobRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AiJobRepositoryAdapter implements AiJobRepository {

    private final JpaAiJobRepository jpa;

    public AiJobRepositoryAdapter(JpaAiJobRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public AiJob save(AiJob job) {
        return toDomain(jpa.save(toEntity(job)));
    }

    @Override
    public Optional<AiJob> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<AiJob> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }

    @Override
    public List<AiJob> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgIdOrderByCreatedAtDesc(orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private AiJob toDomain(AiJobEntity e) {
        AiJob m = new AiJob();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setJobType(e.getJobType());
        m.setStatus(e.getStatus());
        m.setModel(e.getModel());
        m.setParamsJson(e.getParamsJson());
        m.setInputHash(e.getInputHash());
        m.setOutputHash(e.getOutputHash());
        m.setError(e.getError());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setCompletedAt(e.getCompletedAt());
        return m;
    }

    private AiJobEntity toEntity(AiJob m) {
        AiJobEntity e = new AiJobEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setJobType(m.getJobType());
        e.setStatus(m.getStatus());
        e.setModel(m.getModel());
        e.setParamsJson(m.getParamsJson());
        e.setInputHash(m.getInputHash());
        e.setOutputHash(m.getOutputHash());
        e.setError(m.getError());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setCompletedAt(m.getCompletedAt());
        return e;
    }
}
