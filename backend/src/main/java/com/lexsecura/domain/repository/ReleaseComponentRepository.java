package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.ReleaseComponent;

import java.util.List;
import java.util.UUID;

public interface ReleaseComponentRepository {

    ReleaseComponent save(ReleaseComponent releaseComponent);

    List<ReleaseComponent> findAllByReleaseId(UUID releaseId);

    void deleteAllByReleaseId(UUID releaseId);

    boolean existsByReleaseIdAndComponentId(UUID releaseId, UUID componentId);
}
