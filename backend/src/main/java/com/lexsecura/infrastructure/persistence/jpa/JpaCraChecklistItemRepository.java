package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.CraChecklistItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCraChecklistItemRepository extends JpaRepository<CraChecklistItemEntity, UUID> {

    List<CraChecklistItemEntity> findAllByProductIdAndOrgIdOrderByRequirementRef(UUID productId, UUID orgId);

    Optional<CraChecklistItemEntity> findByIdAndOrgId(UUID id, UUID orgId);

    Optional<CraChecklistItemEntity> findByProductIdAndRequirementRef(UUID productId, String requirementRef);

    long countByProductIdAndOrgId(UUID productId, UUID orgId);

    long countByProductIdAndOrgIdAndStatus(UUID productId, UUID orgId, String status);
}
