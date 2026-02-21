package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComponentRepository {

    Component save(Component component);

    Optional<Component> findByPurl(String purl);

    Optional<Component> findById(UUID id);

    List<Component> findAllByReleaseId(UUID releaseId);
}
