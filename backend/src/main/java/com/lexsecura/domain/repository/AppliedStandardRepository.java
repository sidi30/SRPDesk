package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.AppliedStandard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppliedStandardRepository {

    AppliedStandard save(AppliedStandard standard);

    Optional<AppliedStandard> findByIdAndOrgId(UUID id, UUID orgId);

    List<AppliedStandard> findAllByProductIdAndOrgId(UUID productId, UUID orgId);

    void deleteById(UUID id);
}
