package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.CraChecklistItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CraChecklistRepository {

    CraChecklistItem save(CraChecklistItem item);

    Optional<CraChecklistItem> findByIdAndOrgId(UUID id, UUID orgId);

    List<CraChecklistItem> findAllByProductIdAndOrgId(UUID productId, UUID orgId);

    Optional<CraChecklistItem> findByProductIdAndRequirementRef(UUID productId, String requirementRef);

    long countByProductIdAndOrgId(UUID productId, UUID orgId);

    long countByProductIdAndOrgIdAndStatus(UUID productId, UUID orgId, String status);
}
