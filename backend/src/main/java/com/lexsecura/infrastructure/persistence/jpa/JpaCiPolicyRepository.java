package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.CiPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaCiPolicyRepository extends JpaRepository<CiPolicyEntity, UUID> {
    Optional<CiPolicyEntity> findByOrgId(UUID orgId);
}
