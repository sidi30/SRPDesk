package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.Finding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindingRepository {

    Finding save(Finding finding);

    Optional<Finding> findById(UUID id);

    List<Finding> findAllByReleaseId(UUID releaseId);

    List<Finding> findAllByReleaseIdAndStatus(UUID releaseId, String status);

    boolean existsByReleaseIdAndComponentIdAndVulnerabilityId(UUID releaseId, UUID componentId, UUID vulnerabilityId);
}
