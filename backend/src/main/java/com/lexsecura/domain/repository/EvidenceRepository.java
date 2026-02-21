package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.Evidence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidenceRepository {

    Evidence save(Evidence evidence);

    Optional<Evidence> findByIdAndOrgId(UUID id, UUID orgId);

    List<Evidence> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId);

    void deleteById(UUID id);
}
