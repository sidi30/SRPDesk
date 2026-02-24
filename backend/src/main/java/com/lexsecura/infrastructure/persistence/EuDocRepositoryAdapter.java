package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.EuDeclarationOfConformity;
import com.lexsecura.domain.repository.EuDocRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EuDocRepositoryAdapter implements EuDocRepository {

    private final JpaEuDocRepository jpa;
    private final PersistenceMapper mapper;

    public EuDocRepositoryAdapter(JpaEuDocRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public EuDeclarationOfConformity save(EuDeclarationOfConformity doc) {
        return mapper.toDomain(jpa.save(mapper.toEntity(doc)));
    }

    @Override
    public Optional<EuDeclarationOfConformity> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<EuDeclarationOfConformity> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgId(productId, orgId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<EuDeclarationOfConformity> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(mapper::toDomain).toList();
    }
}
