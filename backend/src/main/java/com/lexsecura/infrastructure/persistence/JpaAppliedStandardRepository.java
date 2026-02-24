package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.AppliedStandardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaAppliedStandardRepository extends JpaRepository<AppliedStandardEntity, UUID> {

    Optional<AppliedStandardEntity> findByIdAndOrgId(UUID id, UUID orgId);

    List<AppliedStandardEntity> findAllByProductIdAndOrgId(UUID productId, UUID orgId);
}
