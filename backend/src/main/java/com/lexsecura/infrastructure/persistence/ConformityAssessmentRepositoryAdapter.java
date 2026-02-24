package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.ConformityAssessment;
import com.lexsecura.domain.repository.ConformityAssessmentRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ConformityAssessmentRepositoryAdapter implements ConformityAssessmentRepository {

    private final JpaConformityAssessmentRepository jpa;
    private final PersistenceMapper mapper;

    public ConformityAssessmentRepositoryAdapter(JpaConformityAssessmentRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public ConformityAssessment save(ConformityAssessment assessment) {
        return mapper.toDomain(jpa.save(mapper.toEntity(assessment)));
    }

    @Override
    public Optional<ConformityAssessment> findByProductIdAndModuleAndOrgId(UUID productId, String module, UUID orgId) {
        return jpa.findByProductIdAndModuleAndOrgId(productId, module, orgId).map(mapper::toDomain);
    }

    @Override
    public Optional<ConformityAssessment> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(mapper::toDomain);
    }
}
