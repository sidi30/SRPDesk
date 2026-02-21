package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaAuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    List<AuditEventEntity> findAllByOrgIdOrderByCreatedAtAsc(UUID orgId);

    List<AuditEventEntity> findAllByEntityTypeAndEntityIdOrderByCreatedAtAsc(String entityType, UUID entityId);

    Optional<AuditEventEntity> findTopByOrgIdOrderByCreatedAtDesc(UUID orgId);
}
