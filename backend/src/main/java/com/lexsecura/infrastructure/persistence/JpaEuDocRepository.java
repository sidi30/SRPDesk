package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.EuDocEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaEuDocRepository extends JpaRepository<EuDocEntity, UUID> {

    Optional<EuDocEntity> findByIdAndOrgId(UUID id, UUID orgId);

    List<EuDocEntity> findAllByProductIdAndOrgId(UUID productId, UUID orgId);

    List<EuDocEntity> findAllByOrgId(UUID orgId);
}
