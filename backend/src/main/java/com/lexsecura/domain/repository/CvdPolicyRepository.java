package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.CvdPolicy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CvdPolicyRepository {

    CvdPolicy save(CvdPolicy policy);

    Optional<CvdPolicy> findById(UUID id);

    Optional<CvdPolicy> findByProductIdAndOrgId(UUID productId, UUID orgId);

    List<CvdPolicy> findAllByOrgId(UUID orgId);

    void deleteById(UUID id);
}
