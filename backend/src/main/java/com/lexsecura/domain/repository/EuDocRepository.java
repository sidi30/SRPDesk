package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.EuDeclarationOfConformity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EuDocRepository {

    EuDeclarationOfConformity save(EuDeclarationOfConformity doc);

    Optional<EuDeclarationOfConformity> findByIdAndOrgId(UUID id, UUID orgId);

    List<EuDeclarationOfConformity> findAllByProductIdAndOrgId(UUID productId, UUID orgId);

    List<EuDeclarationOfConformity> findAllByOrgId(UUID orgId);
}
