package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Finding;
import com.lexsecura.domain.repository.FindingRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaFindingRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FindingRepositoryAdapter implements FindingRepository {

    private final JpaFindingRepository jpa;
    private final PersistenceMapper mapper;

    public FindingRepositoryAdapter(JpaFindingRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Finding save(Finding f) {
        var e = mapper.toEntity(f);
        e = jpa.save(e);
        return mapper.toDomain(e);
    }

    @Override
    public Optional<Finding> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Finding> findAllByReleaseId(UUID releaseId) {
        return jpa.findAllByReleaseId(releaseId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Finding> findAllByReleaseIdAndStatus(UUID releaseId, String status) {
        return jpa.findAllByReleaseIdAndStatus(releaseId, status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByReleaseIdAndComponentIdAndVulnerabilityId(UUID releaseId, UUID componentId, UUID vulnerabilityId) {
        return jpa.existsByReleaseIdAndComponentIdAndVulnerabilityId(releaseId, componentId, vulnerabilityId);
    }

    @Override
    public List<Finding> findAllByVulnerabilityId(UUID vulnerabilityId) {
        return jpa.findAllByVulnerabilityId(vulnerabilityId).stream().map(mapper::toDomain).toList();
    }
}
