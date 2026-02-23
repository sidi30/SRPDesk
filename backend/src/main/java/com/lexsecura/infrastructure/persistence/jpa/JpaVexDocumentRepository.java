package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.VexDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaVexDocumentRepository extends JpaRepository<VexDocumentEntity, UUID> {
    Optional<VexDocumentEntity> findByIdAndOrgId(UUID id, UUID orgId);
    List<VexDocumentEntity> findAllByReleaseIdAndOrgIdOrderByCreatedAtDesc(UUID releaseId, UUID orgId);
}
