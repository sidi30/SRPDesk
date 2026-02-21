package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Evidence;
import com.lexsecura.domain.repository.EvidenceRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaEvidenceRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class EvidenceRepositoryAdapter implements EvidenceRepository {

    private final JpaEvidenceRepository jpa;
    private final PersistenceMapper mapper;

    public EvidenceRepositoryAdapter(JpaEvidenceRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Evidence save(Evidence evidence) {
        var entity = mapper.toEntity(evidence);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Evidence> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<Evidence> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId) {
        return jpa.findAllByReleaseIdAndOrgId(releaseId, orgId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteByIdAndOrgId(UUID id, UUID orgId) {
        jpa.deleteByIdAndOrgId(id, orgId);
    }
}
