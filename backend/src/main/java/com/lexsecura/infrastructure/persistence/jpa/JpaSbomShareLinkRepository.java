package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.SbomShareLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaSbomShareLinkRepository extends JpaRepository<SbomShareLinkEntity, UUID> {
    Optional<SbomShareLinkEntity> findByToken(String token);
    Optional<SbomShareLinkEntity> findByIdAndOrgId(UUID id, UUID orgId);
    List<SbomShareLinkEntity> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId);
}
