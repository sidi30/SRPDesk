package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CiPolicy;
import com.lexsecura.domain.repository.CiPolicyRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaCiPolicyRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CiPolicyRepositoryAdapter implements CiPolicyRepository {

    private final JpaCiPolicyRepository jpa;
    private final PersistenceMapper mapper;

    public CiPolicyRepositoryAdapter(JpaCiPolicyRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public CiPolicy save(CiPolicy policy) {
        var entity = mapper.toEntity(policy);
        entity = jpa.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<CiPolicy> findByOrgId(UUID orgId) {
        return jpa.findByOrgId(orgId).map(mapper::toDomain);
    }
}
