package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CraChecklistItem;
import com.lexsecura.domain.repository.CraChecklistRepository;
import com.lexsecura.infrastructure.persistence.entity.CraChecklistItemEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaCraChecklistItemRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CraChecklistRepositoryAdapter implements CraChecklistRepository {

    private final JpaCraChecklistItemRepository jpa;

    public CraChecklistRepositoryAdapter(JpaCraChecklistItemRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public CraChecklistItem save(CraChecklistItem item) {
        return toDomain(jpa.save(toEntity(item)));
    }

    @Override
    public Optional<CraChecklistItem> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }

    @Override
    public List<CraChecklistItem> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgIdOrderByRequirementRef(productId, orgId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<CraChecklistItem> findByProductIdAndRequirementRef(UUID productId, String requirementRef) {
        return jpa.findByProductIdAndRequirementRef(productId, requirementRef).map(this::toDomain);
    }

    @Override
    public long countByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.countByProductIdAndOrgId(productId, orgId);
    }

    @Override
    public long countByProductIdAndOrgIdAndStatus(UUID productId, UUID orgId, String status) {
        return jpa.countByProductIdAndOrgIdAndStatus(productId, orgId, status);
    }

    private CraChecklistItem toDomain(CraChecklistItemEntity e) {
        CraChecklistItem m = new CraChecklistItem();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setRequirementRef(e.getRequirementRef());
        m.setCategory(e.getCategory());
        m.setTitle(e.getTitle());
        m.setDescription(e.getDescription());
        m.setStatus(e.getStatus());
        m.setEvidenceIds(e.getEvidenceIds() != null ? Arrays.asList(e.getEvidenceIds()) : List.of());
        m.setNotes(e.getNotes());
        m.setAssessedBy(e.getAssessedBy());
        m.setAssessedAt(e.getAssessedAt());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    private CraChecklistItemEntity toEntity(CraChecklistItem m) {
        CraChecklistItemEntity e = new CraChecklistItemEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setRequirementRef(m.getRequirementRef());
        e.setCategory(m.getCategory());
        e.setTitle(m.getTitle());
        e.setDescription(m.getDescription());
        e.setStatus(m.getStatus());
        e.setEvidenceIds(m.getEvidenceIds() != null ? m.getEvidenceIds().toArray(new UUID[0]) : new UUID[0]);
        e.setNotes(m.getNotes());
        e.setAssessedBy(m.getAssessedBy());
        e.setAssessedAt(m.getAssessedAt());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }
}
