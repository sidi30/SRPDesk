package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.repository.CraEventRepository;
import com.lexsecura.infrastructure.persistence.entity.CraEventEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CraEventRepositoryAdapter implements CraEventRepository {

    private final JpaCraEventRepository jpa;

    public CraEventRepositoryAdapter(JpaCraEventRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CraEvent save(CraEvent event) {
        return toDomain(jpa.save(toEntity(event)));
    }

    @Override
    public Optional<CraEvent> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<CraEvent> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }

    @Override
    public List<CraEvent> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgIdOrderByCreatedAtDesc(orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<CraEvent> findAllByOrgIdAndProductId(UUID orgId, UUID productId) {
        return jpa.findAllByOrgIdAndProductIdOrderByCreatedAtDesc(orgId, productId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<CraEvent> findAllByOrgIdAndStatus(UUID orgId, String status) {
        return jpa.findAllByOrgIdAndStatusOrderByCreatedAtDesc(orgId, status).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<CraEvent> findAllByOrgIdAndProductIdAndStatus(UUID orgId, UUID productId, String status) {
        return jpa.findAllByOrgIdAndProductIdAndStatusOrderByCreatedAtDesc(orgId, productId, status).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<CraEvent> findAllByStatus(String status) {
        return jpa.findAllByStatus(status).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private CraEvent toDomain(CraEventEntity e) {
        CraEvent m = new CraEvent();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setEventType(e.getEventType());
        m.setTitle(e.getTitle());
        m.setDescription(e.getDescription());
        m.setStatus(e.getStatus());
        m.setStartedAt(e.getStartedAt());
        m.setDetectedAt(e.getDetectedAt());
        m.setPatchAvailableAt(e.getPatchAvailableAt());
        m.setResolvedAt(e.getResolvedAt());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    private CraEventEntity toEntity(CraEvent m) {
        CraEventEntity e = new CraEventEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setEventType(m.getEventType());
        e.setTitle(m.getTitle());
        e.setDescription(m.getDescription());
        e.setStatus(m.getStatus());
        e.setStartedAt(m.getStartedAt());
        e.setDetectedAt(m.getDetectedAt());
        e.setPatchAvailableAt(m.getPatchAvailableAt());
        e.setResolvedAt(m.getResolvedAt());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }
}
