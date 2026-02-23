package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.SbomShareLink;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SbomShareLinkRepository {

    SbomShareLink save(SbomShareLink link);

    Optional<SbomShareLink> findByToken(String token);

    Optional<SbomShareLink> findByIdAndOrgId(UUID id, UUID orgId);

    List<SbomShareLink> findAllByReleaseIdAndOrgId(UUID releaseId, UUID orgId);
}
