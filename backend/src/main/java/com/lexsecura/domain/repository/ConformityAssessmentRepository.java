package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.ConformityAssessment;

import java.util.Optional;
import java.util.UUID;

public interface ConformityAssessmentRepository {

    ConformityAssessment save(ConformityAssessment assessment);

    Optional<ConformityAssessment> findByProductIdAndModuleAndOrgId(UUID productId, String module, UUID orgId);

    Optional<ConformityAssessment> findByIdAndOrgId(UUID id, UUID orgId);
}
