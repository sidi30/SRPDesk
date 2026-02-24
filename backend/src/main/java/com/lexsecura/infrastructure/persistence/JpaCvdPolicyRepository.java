package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.CvdPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCvdPolicyRepository extends JpaRepository<CvdPolicyEntity, UUID> {

    Optional<CvdPolicyEntity> findByProductIdAndOrgId(UUID productId, UUID orgId);

    List<CvdPolicyEntity> findAllByOrgId(UUID orgId);
}
