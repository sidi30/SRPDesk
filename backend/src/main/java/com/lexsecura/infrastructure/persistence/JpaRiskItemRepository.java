package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.RiskItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaRiskItemRepository extends JpaRepository<RiskItemEntity, UUID> {

    List<RiskItemEntity> findAllByRiskAssessmentId(UUID riskAssessmentId);
}
