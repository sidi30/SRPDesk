package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.vex.VexDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VexDocumentRepository {
    VexDocument save(VexDocument doc);
    Optional<VexDocument> findById(UUID id);
    Optional<VexDocument> findByIdAndOrgId(UUID id, UUID orgId);
    List<VexDocument> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId);
    void deleteById(UUID id);
}
