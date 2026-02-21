package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.ReleaseComponent;
import com.lexsecura.domain.repository.ReleaseComponentRepository;
import com.lexsecura.infrastructure.persistence.entity.ReleaseComponentEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaReleaseComponentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ReleaseComponentRepositoryAdapter implements ReleaseComponentRepository {

    private final JpaReleaseComponentRepository jpa;

    public ReleaseComponentRepositoryAdapter(JpaReleaseComponentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ReleaseComponent save(ReleaseComponent rc) {
        var entity = new ReleaseComponentEntity();
        entity.setId(rc.getId());
        entity.setReleaseId(rc.getReleaseId());
        entity.setComponentId(rc.getComponentId());
        entity.setCreatedAt(rc.getCreatedAt());
        entity = jpa.save(entity);
        return toDomain(entity);
    }

    @Override
    public List<ReleaseComponent> findAllByReleaseId(UUID releaseId) {
        return jpa.findAllByReleaseId(releaseId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteAllByReleaseId(UUID releaseId) {
        jpa.deleteAllByReleaseId(releaseId);
    }

    @Override
    public boolean existsByReleaseIdAndComponentId(UUID releaseId, UUID componentId) {
        return jpa.existsByReleaseIdAndComponentId(releaseId, componentId);
    }

    private ReleaseComponent toDomain(ReleaseComponentEntity e) {
        ReleaseComponent rc = new ReleaseComponent();
        rc.setId(e.getId());
        rc.setReleaseId(e.getReleaseId());
        rc.setComponentId(e.getComponentId());
        rc.setCreatedAt(e.getCreatedAt());
        return rc;
    }
}
