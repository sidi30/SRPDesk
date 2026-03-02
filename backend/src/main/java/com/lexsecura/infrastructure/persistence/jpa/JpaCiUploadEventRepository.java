package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.CiUploadEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCiUploadEventRepository extends JpaRepository<CiUploadEventEntity, UUID> {
    Optional<CiUploadEventEntity> findFirstByProductIdOrderByCreatedAtDesc(UUID productId);
    List<CiUploadEventEntity> findAllByOrgId(UUID orgId);
}
