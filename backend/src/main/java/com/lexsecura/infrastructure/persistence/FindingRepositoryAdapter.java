package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Finding;
import com.lexsecura.domain.repository.FindingRepository;
import com.lexsecura.infrastructure.persistence.entity.FindingEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaFindingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class FindingRepositoryAdapter implements FindingRepository {

    private final JpaFindingRepository jpa;

    public FindingRepositoryAdapter(JpaFindingRepository jpa) { this.jpa = jpa; }

    @Override
    public Finding save(Finding f) {
        var e = toEntity(f); e = jpa.save(e); return toDomain(e);
    }
    @Override
    public Optional<Finding> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }
    @Override
    public List<Finding> findAllByReleaseId(UUID releaseId) {
        return jpa.findAllByReleaseId(releaseId).stream().map(this::toDomain).collect(Collectors.toList());
    }
    @Override
    public List<Finding> findAllByReleaseIdAndStatus(UUID releaseId, String status) {
        return jpa.findAllByReleaseIdAndStatus(releaseId, status).stream().map(this::toDomain).collect(Collectors.toList());
    }
    @Override
    public boolean existsByReleaseIdAndComponentIdAndVulnerabilityId(UUID releaseId, UUID componentId, UUID vulnerabilityId) {
        return jpa.existsByReleaseIdAndComponentIdAndVulnerabilityId(releaseId, componentId, vulnerabilityId);
    }
    @Override
    public List<Finding> findAllByVulnerabilityId(UUID vulnerabilityId) {
        return jpa.findAllByVulnerabilityId(vulnerabilityId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private Finding toDomain(FindingEntity e) {
        Finding f = new Finding();
        f.setId(e.getId()); f.setReleaseId(e.getReleaseId()); f.setComponentId(e.getComponentId());
        f.setVulnerabilityId(e.getVulnerabilityId()); f.setStatus(e.getStatus());
        f.setDetectedAt(e.getDetectedAt()); f.setSource(e.getSource());
        return f;
    }
    private FindingEntity toEntity(Finding f) {
        FindingEntity e = new FindingEntity();
        e.setId(f.getId()); e.setReleaseId(f.getReleaseId()); e.setComponentId(f.getComponentId());
        e.setVulnerabilityId(f.getVulnerabilityId()); e.setStatus(f.getStatus());
        e.setDetectedAt(f.getDetectedAt()); e.setSource(f.getSource());
        return e;
    }
}
