package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.repository.CraEventRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CraEventRepositoryAdapter implements CraEventRepository {

    private final JpaCraEventRepository jpa;
    private final PersistenceMapper mapper;

    public CraEventRepositoryAdapter(JpaCraEventRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public CraEvent save(CraEvent event) {
        return mapper.toDomain(jpa.save(mapper.toEntity(event)));
    }

    @Override
    public Optional<CraEvent> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<CraEvent> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<CraEvent> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgIdOrderByCreatedAtDesc(orgId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CraEvent> findAllByOrgIdAndProductId(UUID orgId, UUID productId) {
        return jpa.findAllByOrgIdAndProductIdOrderByCreatedAtDesc(orgId, productId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CraEvent> findAllByOrgIdAndStatus(UUID orgId, String status) {
        return jpa.findAllByOrgIdAndStatusOrderByCreatedAtDesc(orgId, status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CraEvent> findAllByOrgIdAndProductIdAndStatus(UUID orgId, UUID productId, String status) {
        return jpa.findAllByOrgIdAndProductIdAndStatusOrderByCreatedAtDesc(orgId, productId, status).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<CraEvent> findAllByStatus(String status) {
        return jpa.findAllByStatus(status).stream().map(mapper::toDomain).toList();
    }
}
