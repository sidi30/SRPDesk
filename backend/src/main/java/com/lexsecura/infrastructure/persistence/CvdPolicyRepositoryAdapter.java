package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.CvdPolicy;
import com.lexsecura.domain.repository.CvdPolicyRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CvdPolicyRepositoryAdapter implements CvdPolicyRepository {

    private final JpaCvdPolicyRepository jpa;
    private final PersistenceMapper mapper;

    public CvdPolicyRepositoryAdapter(JpaCvdPolicyRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public CvdPolicy save(CvdPolicy p) {
        return mapper.toDomain(jpa.save(mapper.toEntity(p)));
    }

    @Override
    public Optional<CvdPolicy> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<CvdPolicy> findByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findByProductIdAndOrgId(productId, orgId).map(mapper::toDomain);
    }

    @Override
    public List<CvdPolicy> findAllByOrgId(UUID orgId) {
        return jpa.findAllByOrgId(orgId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
