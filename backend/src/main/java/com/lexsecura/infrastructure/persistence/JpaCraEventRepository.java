package com.lexsecura.infrastructure.persistence;

import com.lexsecura.infrastructure.persistence.entity.CraEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCraEventRepository extends JpaRepository<CraEventEntity, UUID> {

    Optional<CraEventEntity> findByIdAndOrgId(UUID id, UUID orgId);

    List<CraEventEntity> findAllByOrgIdOrderByCreatedAtDesc(UUID orgId);

    List<CraEventEntity> findAllByOrgIdAndProductIdOrderByCreatedAtDesc(UUID orgId, UUID productId);

    List<CraEventEntity> findAllByOrgIdAndStatusOrderByCreatedAtDesc(UUID orgId, String status);

    List<CraEventEntity> findAllByOrgIdAndProductIdAndStatusOrderByCreatedAtDesc(UUID orgId, UUID productId, String status);

    List<CraEventEntity> findAllByStatus(String status);
}
