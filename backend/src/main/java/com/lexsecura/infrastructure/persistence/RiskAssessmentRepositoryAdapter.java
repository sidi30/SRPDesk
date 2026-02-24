package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.RiskAssessment;
import com.lexsecura.domain.repository.RiskAssessmentRepository;
import com.lexsecura.infrastructure.persistence.entity.RiskAssessmentEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RiskAssessmentRepositoryAdapter implements RiskAssessmentRepository {

    private final JpaRiskAssessmentRepository jpa;

    public RiskAssessmentRepositoryAdapter(JpaRiskAssessmentRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public RiskAssessment save(RiskAssessment assessment) {
        return toDomain(jpa.save(toEntity(assessment)));
    }

    @Override
    public Optional<RiskAssessment> findByIdAndOrgId(UUID id, UUID orgId) {
        return jpa.findByIdAndOrgId(id, orgId).map(this::toDomain);
    }

    @Override
    public List<RiskAssessment> findAllByProductIdAndOrgId(UUID productId, UUID orgId) {
        return jpa.findAllByProductIdAndOrgId(productId, orgId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private RiskAssessment toDomain(RiskAssessmentEntity e) {
        RiskAssessment m = new RiskAssessment();
        m.setId(e.getId());
        m.setOrgId(e.getOrgId());
        m.setProductId(e.getProductId());
        m.setTitle(e.getTitle());
        m.setMethodology(e.getMethodology());
        m.setStatus(e.getStatus());
        m.setOverallRiskLevel(e.getOverallRiskLevel());
        m.setSummary(e.getSummary());
        m.setApprovedBy(e.getApprovedBy());
        m.setApprovedAt(e.getApprovedAt());
        m.setCreatedBy(e.getCreatedBy());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    private RiskAssessmentEntity toEntity(RiskAssessment m) {
        RiskAssessmentEntity e = new RiskAssessmentEntity();
        e.setId(m.getId());
        e.setOrgId(m.getOrgId());
        e.setProductId(m.getProductId());
        e.setTitle(m.getTitle());
        e.setMethodology(m.getMethodology());
        e.setStatus(m.getStatus());
        e.setOverallRiskLevel(m.getOverallRiskLevel());
        e.setSummary(m.getSummary());
        e.setApprovedBy(m.getApprovedBy());
        e.setApprovedAt(m.getApprovedAt());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }
}
