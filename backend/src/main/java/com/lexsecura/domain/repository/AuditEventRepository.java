package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.AuditEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditEventRepository {

    AuditEvent save(AuditEvent event);

    List<AuditEvent> findAllByOrgIdOrderByCreatedAt(UUID orgId);

    List<AuditEvent> findAllByEntityTypeAndEntityIdOrderByCreatedAt(String entityType, UUID entityId);

    Optional<AuditEvent> findTopByOrgIdOrderByCreatedAtDesc(UUID orgId);
}
