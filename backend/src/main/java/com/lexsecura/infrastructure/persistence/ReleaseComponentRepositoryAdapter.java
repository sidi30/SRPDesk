package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.ReleaseComponent;
import com.lexsecura.domain.repository.ReleaseComponentRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaReleaseComponentRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ReleaseComponentRepositoryAdapter implements ReleaseComponentRepository {

    private final JpaReleaseComponentRepository jpa;
    private final PersistenceMapper mapper;

    public ReleaseComponentRepositoryAdapter(JpaReleaseComponentRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public ReleaseComponent save(ReleaseComponent rc) {
        var entity = mapper.toEntity(rc);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public List<ReleaseComponent> findAllByReleaseId(UUID releaseId) {
        return jpa.findAllByReleaseId(releaseId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteAllByReleaseId(UUID releaseId) {
        jpa.deleteAllByReleaseId(releaseId);
    }

    @Override
    public boolean existsByReleaseIdAndComponentId(UUID releaseId, UUID componentId) {
        return jpa.existsByReleaseIdAndComponentId(releaseId, componentId);
    }
}
