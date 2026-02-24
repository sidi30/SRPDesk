package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.ConformityAssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaConformityAssessmentRepository extends JpaRepository<ConformityAssessmentEntity, UUID> {

    Optional<ConformityAssessmentEntity> findByProductIdAndModuleAndOrgId(UUID productId, String module, UUID orgId);

    Optional<ConformityAssessmentEntity> findByIdAndOrgId(UUID id, UUID orgId);
}
