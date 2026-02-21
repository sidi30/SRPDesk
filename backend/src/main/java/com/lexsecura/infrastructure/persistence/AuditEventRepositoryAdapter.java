package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.AuditEvent;
import com.lexsecura.domain.repository.AuditEventRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaAuditEventRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AuditEventRepositoryAdapter implements AuditEventRepository {

    private final JpaAuditEventRepository jpa;
    private final PersistenceMapper mapper;

    public AuditEventRepositoryAdapter(JpaAuditEventRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        var entity = mapper.toEntity(event);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public List<AuditEvent> findAllByOrgIdOrderByCreatedAt(UUID orgId) {
        return jpa.findAllByOrgIdOrderByCreatedAtAsc(orgId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<AuditEvent> findAllByEntityTypeAndEntityIdOrderByCreatedAt(String entityType, UUID entityId) {
        return jpa.findAllByEntityTypeAndEntityIdOrderByCreatedAtAsc(entityType, entityId).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<AuditEvent> findTopByOrgIdOrderByCreatedAtDesc(UUID orgId) {
        return jpa.findTopByOrgIdOrderByCreatedAtDesc(orgId).map(mapper::toDomain);
    }
}
