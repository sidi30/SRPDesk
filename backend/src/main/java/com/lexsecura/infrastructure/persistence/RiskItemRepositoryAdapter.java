package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.RiskItem;
import com.lexsecura.domain.repository.RiskItemRepository;
import com.lexsecura.infrastructure.persistence.entity.RiskItemEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RiskItemRepositoryAdapter implements RiskItemRepository {

    private final JpaRiskItemRepository jpa;

    public RiskItemRepositoryAdapter(JpaRiskItemRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public RiskItem save(RiskItem item) {
        return toDomain(jpa.save(toEntity(item)));
    }

    @Override
    public Optional<RiskItem> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<RiskItem> findAllByRiskAssessmentId(UUID riskAssessmentId) {
        return jpa.findAllByRiskAssessmentId(riskAssessmentId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    private RiskItem toDomain(RiskItemEntity e) {
        RiskItem m = new RiskItem();
        m.setId(e.getId());
        m.setRiskAssessmentId(e.getRiskAssessmentId());
        m.setThreatCategory(e.getThreatCategory());
        m.setThreatDescription(e.getThreatDescription());
        m.setAffectedAsset(e.getAffectedAsset());
        m.setLikelihood(e.getLikelihood());
        m.setImpact(e.getImpact());
        m.setRiskLevel(e.getRiskLevel());
        m.setExistingControls(e.getExistingControls());
        m.setMitigationPlan(e.getMitigationPlan());
        m.setMitigationStatus(e.getMitigationStatus());
        m.setResidualRiskLevel(e.getResidualRiskLevel());
        m.setCreatedAt(e.getCreatedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    private RiskItemEntity toEntity(RiskItem m) {
        RiskItemEntity e = new RiskItemEntity();
        e.setId(m.getId());
        e.setRiskAssessmentId(m.getRiskAssessmentId());
        e.setThreatCategory(m.getThreatCategory());
        e.setThreatDescription(m.getThreatDescription());
        e.setAffectedAsset(m.getAffectedAsset());
        e.setLikelihood(m.getLikelihood());
        e.setImpact(m.getImpact());
        e.setRiskLevel(m.getRiskLevel());
        e.setExistingControls(m.getExistingControls());
        e.setMitigationPlan(m.getMitigationPlan());
        e.setMitigationStatus(m.getMitigationStatus());
        e.setResidualRiskLevel(m.getResidualRiskLevel());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }
}
