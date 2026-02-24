package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_items")
public class RiskItemEntity {

    @Id
    private UUID id;

    @Column(name = "risk_assessment_id", nullable = false)
    private UUID riskAssessmentId;

    @Column(name = "threat_category", length = 100, nullable = false)
    private String threatCategory;

    @Column(name = "threat_description", columnDefinition = "TEXT", nullable = false)
    private String threatDescription;

    @Column(name = "affected_asset", length = 500)
    private String affectedAsset;

    @Column(length = 50, nullable = false)
    private String likelihood;

    @Column(length = 50, nullable = false)
    private String impact;

    @Column(name = "risk_level", length = 50, nullable = false)
    private String riskLevel;

    @Column(name = "existing_controls", columnDefinition = "TEXT")
    private String existingControls;

    @Column(name = "mitigation_plan", columnDefinition = "TEXT")
    private String mitigationPlan;

    @Column(name = "mitigation_status", length = 50, nullable = false)
    private String mitigationStatus;

    @Column(name = "residual_risk_level", length = 50)
    private String residualRiskLevel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (likelihood == null) likelihood = "MEDIUM";
        if (impact == null) impact = "MEDIUM";
        if (mitigationStatus == null) mitigationStatus = "PENDING";
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRiskAssessmentId() { return riskAssessmentId; }
    public void setRiskAssessmentId(UUID riskAssessmentId) { this.riskAssessmentId = riskAssessmentId; }
    public String getThreatCategory() { return threatCategory; }
    public void setThreatCategory(String threatCategory) { this.threatCategory = threatCategory; }
    public String getThreatDescription() { return threatDescription; }
    public void setThreatDescription(String threatDescription) { this.threatDescription = threatDescription; }
    public String getAffectedAsset() { return affectedAsset; }
    public void setAffectedAsset(String affectedAsset) { this.affectedAsset = affectedAsset; }
    public String getLikelihood() { return likelihood; }
    public void setLikelihood(String likelihood) { this.likelihood = likelihood; }
    public String getImpact() { return impact; }
    public void setImpact(String impact) { this.impact = impact; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getExistingControls() { return existingControls; }
    public void setExistingControls(String existingControls) { this.existingControls = existingControls; }
    public String getMitigationPlan() { return mitigationPlan; }
    public void setMitigationPlan(String mitigationPlan) { this.mitigationPlan = mitigationPlan; }
    public String getMitigationStatus() { return mitigationStatus; }
    public void setMitigationStatus(String mitigationStatus) { this.mitigationStatus = mitigationStatus; }
    public String getResidualRiskLevel() { return residualRiskLevel; }
    public void setResidualRiskLevel(String residualRiskLevel) { this.residualRiskLevel = residualRiskLevel; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
