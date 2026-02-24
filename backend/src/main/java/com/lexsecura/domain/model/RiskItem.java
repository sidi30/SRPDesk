package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class RiskItem {

    private UUID id;
    private UUID riskAssessmentId;
    private String threatCategory; // SPOOFING, TAMPERING, etc.
    private String threatDescription;
    private String affectedAsset;
    private String likelihood; // VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH
    private String impact; // VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH
    private String riskLevel; // CRITICAL, HIGH, MEDIUM, LOW
    private String existingControls;
    private String mitigationPlan;
    private String mitigationStatus; // PENDING, IN_PROGRESS, IMPLEMENTED, ACCEPTED
    private String residualRiskLevel;
    private Instant createdAt;
    private Instant updatedAt;

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

    public static String computeRiskLevel(String likelihood, String impact) {
        int likelihoodScore = scoreOf(likelihood);
        int impactScore = scoreOf(impact);
        int total = likelihoodScore * impactScore;
        if (total >= 16) return "CRITICAL";
        if (total >= 9) return "HIGH";
        if (total >= 4) return "MEDIUM";
        return "LOW";
    }

    private static int scoreOf(String level) {
        return switch (level) {
            case "VERY_HIGH" -> 5;
            case "HIGH" -> 4;
            case "MEDIUM" -> 3;
            case "LOW" -> 2;
            case "VERY_LOW" -> 1;
            default -> 3;
        };
    }
}
