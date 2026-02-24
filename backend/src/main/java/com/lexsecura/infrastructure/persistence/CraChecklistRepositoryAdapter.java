package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CraChecklistItem;
import com.lexsecura.domain.repository.CraChecklistRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaCraChecklistItemRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CraChecklistRepositoryAdapter implements CraChecklistRepository {

    private final JpaCraChecklistItemRepository jpa;
    private final PersistenceMapper mapper;

    public CraChecklistRepositoryAdapter(JpaCraChecklistItemRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public CraChecklistItem save(CraChecklistItem item) {
        return mapper.toDomain(jpa.save(mapper.toEntity(item)));
    }

    @Override
    public Optional<CraChecklistItem> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<CraChecklistItem> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgIdOrderByRequirementRef(productId, orgId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<CraChecklistItem> findByProductIdAndRequirementRef(UUID productId, String requirementRef) {
        return jpa.findByProductIdAndRequirementRef(productId, requirementRef).map(mapper::toDomain);
    }

    @Override
    public long countByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.countByProductIdAndOrgId(productId, orgId);
    }

    @Override
    public long countByProductIdAndOrgIdAndStatus(UUID productId, UUID orgId, String status) {
        return jpa.countByProductIdAndOrgIdAndStatus(productId, orgId, status);
    }
}
