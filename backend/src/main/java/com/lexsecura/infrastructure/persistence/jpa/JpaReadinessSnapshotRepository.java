package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.ReadinessSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaReadinessSnapshotRepository extends JpaRepository<ReadinessSnapshotEntity, UUID> {

    List<ReadinessSnapshotEntity> findAllByProductIdAndOrgIdOrderBySnapshotAtDesc(UUID productId, UUID orgId);
}
