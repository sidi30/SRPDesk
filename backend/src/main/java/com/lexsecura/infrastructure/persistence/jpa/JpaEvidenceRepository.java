package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.EvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaEvidenceRepository extends JpaRepository<EvidenceEntity, UUID> {
    List<EvidenceEntity> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId);
    Optional<EvidenceEntity> findByIdAndOrgId(UUID id, UUID orgId);
    void deleteByIdAndOrgId(UUID id, UUID orgId);
}
