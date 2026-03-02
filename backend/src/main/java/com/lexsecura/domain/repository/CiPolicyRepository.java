package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.CiPolicy;

import java.util.Optional;
import java.util.UUID;

public interface CiPolicyRepository {

    CiPolicy save(CiPolicy policy);

    Optional<CiPolicy> findByOrgId(UUID orgId);
}
