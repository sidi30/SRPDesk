package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.ReleaseComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JpaReleaseComponentRepository extends JpaRepository<ReleaseComponentEntity, UUID> {
    List<ReleaseComponentEntity> findAllByReleaseId(UUID releaseId);
    void deleteAllByReleaseId(UUID releaseId);
    boolean existsByReleaseIdAndComponentId(UUID releaseId, UUID componentId);
}
