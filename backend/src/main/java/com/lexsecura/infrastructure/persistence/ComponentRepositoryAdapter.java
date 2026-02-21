package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.ComponentRepository;
import com.lexsecura.infrastructure.persistence.entity.ComponentEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaComponentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ComponentRepositoryAdapter implements ComponentRepository {

    private final JpaComponentRepository jpa;

    public ComponentRepositoryAdapter(JpaComponentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Component save(Component component) {
        var entity = toEntity(component);
        entity = jpa.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Component> findByPurl(String purl) {
        return jpa.findByPurl(purl).map(this::toDomain);
    }

    @Override
    public Optional<Component> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Component> findAllByReleaseId(UUID releaseId) {
        return jpa.findAllByReleaseId(releaseId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private Component toDomain(ComponentEntity e) {
        Component c = new Component();
        c.setId(e.getId());
        c.setPurl(e.getPurl());
        c.setName(e.getName());
        c.setVersion(e.getVersion());
        c.setType(e.getType());
        return c;
    }

    private ComponentEntity toEntity(Component c) {
        ComponentEntity e = new ComponentEntity();
        e.setId(c.getId());
        e.setPurl(c.getPurl());
        e.setName(c.getName());
        e.setVersion(c.getVersion());
        e.setType(c.getType());
        return e;
    }
}
