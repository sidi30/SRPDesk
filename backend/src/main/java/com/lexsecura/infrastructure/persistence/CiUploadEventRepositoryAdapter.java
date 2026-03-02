package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CiUploadEvent;
import com.lexsecura.domain.repository.CiUploadEventRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaCiUploadEventRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CiUploadEventRepositoryAdapter implements CiUploadEventRepository {

    private final JpaCiUploadEventRepository jpa;
    private final PersistenceMapper mapper;

    public CiUploadEventRepositoryAdapter(JpaCiUploadEventRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public CiUploadEvent save(CiUploadEvent event) {
        var entity = mapper.toEntity(event);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<CiUploadEvent> findLatestByProductId(UUID productId) {
        return jpa.findFirstByProductIdOrderByCreatedAtDesc(productId).map(mapper::toDomain);
    }

    @Override
    public List<CiUploadEvent> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(mapper::toDomain).toList();
    }
}
