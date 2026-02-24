package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.RiskAssessment;
import com.lexsecura.domain.repository.RiskAssessmentRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RiskAssessmentRepositoryAdapter implements RiskAssessmentRepository {

    private final JpaRiskAssessmentRepository jpa;
    private final PersistenceMapper mapper;

    public RiskAssessmentRepositoryAdapter(JpaRiskAssessmentRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public RiskAssessment save(RiskAssessment assessment) {
        return mapper.toDomain(jpa.save(mapper.toEntity(assessment)));
    }

    @Override
    public Optional<RiskAssessment> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }

    @Override
    public List<RiskAssessment> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgId(productId, orgId).stream().map(mapper::toDomain).toList();
    }
}
