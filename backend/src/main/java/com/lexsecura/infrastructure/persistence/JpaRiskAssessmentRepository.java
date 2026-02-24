package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.RiskAssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaRiskAssessmentRepository extends JpaRepository<RiskAssessmentEntity, UUID> {

    Optional<RiskAssessmentEntity> findByIdAndOrgId(UUID id, UUID orgId);

    List<RiskAssessmentEntity> findAllByProductIdAndOrgId(UUID productId, UUID orgId);
}
