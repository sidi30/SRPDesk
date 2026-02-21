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

    long countByOrgId(UUID orgId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM AuditEventEntity e WHERE e.orgId = :orgId ORDER BY e.createdAt ASC")
    List<AuditEventEntity> findByOrgIdPaged(@org.springframework.data.repository.query.Param("orgId") UUID orgId, org.springframework.data.domain.Pageable pageable);
}
