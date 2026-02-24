package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.ComponentRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaComponentRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ComponentRepositoryAdapter implements ComponentRepository {

    private final JpaComponentRepository jpa;
    private final PersistenceMapper mapper;

    public ComponentRepositoryAdapter(JpaComponentRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public Component save(Component component) {
        var entity = mapper.toEntity(component);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<Component> findByPurl(String purl) {
        return jpa.findByPurl(purl).map(mapper::toDomain);
    }

    @Override
    public Optional<Component> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Component> findAllByReleaseId(UUID releaseId) {
        return jpa.findAllByReleaseId(releaseId).stream().map(mapper::toDomain).toList();
    }
}
