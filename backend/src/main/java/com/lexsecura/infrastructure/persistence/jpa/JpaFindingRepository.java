package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.FindingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JpaFindingRepository extends JpaRepository<FindingEntity, UUID> {
    List<FindingEntity> findAllByReleaseId(UUID releaseId);
    List<FindingEntity> findAllByReleaseIdAndStatus(UUID releaseId, String status);
    boolean existsByReleaseIdAndComponentIdAndVulnerabilityId(UUID releaseId, UUID componentId, UUID vulnerabilityId);
    List<FindingEntity> findAllByVulnerabilityId(UUID vulnerabilityId);
}
