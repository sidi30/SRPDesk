package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.RiskAssessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskAssessmentRepository {

    RiskAssessment save(RiskAssessment assessment);

    Optional<RiskAssessment> findByIdAndOrgId(UUID id, UUID orgId);

    List<RiskAssessment> findAllByProductIdAndOrgId(UUID productId, UUID orgId);
}
