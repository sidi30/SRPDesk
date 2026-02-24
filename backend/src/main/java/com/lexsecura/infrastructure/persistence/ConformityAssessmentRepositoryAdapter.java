package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.ConformityAssessment;
import com.lexsecura.domain.repository.ConformityAssessmentRepository;
import com.lexsecura.infrastructure.persistence.entity.ConformityAssessmentEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ConformityAssessmentRepositoryAdapter implements ConformityAssessmentRepository {

    private final JpaConformityAssessmentRepository jpa;

    public ConformityAssessmentRepositoryAdapter(JpaConformityAssessmentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ConformityAssessment save(ConformityAssessment assessment) {
        return toDomain(jpa.save(toEntity(assessment)));
    }

    @Override
    public Optional<ConformityAssessment> findByProductIdAndModuleAndOrgId(UUID productId, String module, UUID orgId) {
        return jpa.findByProductIdAndModuleAndOrgId(productId, module, orgId).map(this::toDomain);
    }

    @Override
    public Optional<ConformityAssessment> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }

    private ConformityAssessment toDomain(ConformityAssessmentEntity e) {
        ConformityAssessment m = new ConformityAssessment();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setModule(e.getModule());
        m.setStatus(e.getStatus());
        m.setCurrentStep(e.getCurrentStep());
        m.setTotalSteps(e.getTotalSteps());
        m.setStepsData(e.getStepsData());
        m.setStartedAt(e.getStartedAt());
        m.setCompletedAt(e.getCompletedAt());
        m.setApprovedBy(e.getApprovedBy());
        m.setApprovedAt(e.getApprovedAt());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    private ConformityAssessmentEntity toEntity(ConformityAssessment m) {
        ConformityAssessmentEntity e = new ConformityAssessmentEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setModule(m.getModule());
        e.setStatus(m.getStatus());
        e.setCurrentStep(m.getCurrentStep());
        e.setTotalSteps(m.getTotalSteps());
        e.setStepsData(m.getStepsData());
        e.setStartedAt(m.getStartedAt());
        e.setCompletedAt(m.getCompletedAt());
        e.setApprovedBy(m.getApprovedBy());
        e.setApprovedAt(m.getApprovedAt());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }
}
