package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.AppliedStandard;
import com.lexsecura.domain.repository.AppliedStandardRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AppliedStandardRepositoryAdapter implements AppliedStandardRepository {

    private final JpaAppliedStandardRepository jpa;
    private final PersistenceMapper mapper;

    public AppliedStandardRepositoryAdapter(JpaAppliedStandardRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public AppliedStandard save(AppliedStandard standard) {
        return mapper.toDomain(jpa.save(mapper.toEntity(standard)));
    }

    @Override
    public Optional<AppliedStandard> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<AppliedStandard> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgId(productId, orgId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
