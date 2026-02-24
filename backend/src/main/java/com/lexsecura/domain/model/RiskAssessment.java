package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class RiskAssessment {

    private UUID id;
    private UUID orgId;
    private UUID productId;
    private String title;
    private String methodology; // STRIDE, DREAD, CUSTOM
    private String status; // DRAFT, IN_REVIEW, APPROVED
    private String overallRiskLevel; // CRITICAL, HIGH, MEDIUM, LOW
    private String summary;
    private UUID approvedBy;
    private Instant approvedAt;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMethodology() { return methodology; }
    public void setMethodology(String methodology) { this.methodology = methodology; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOverallRiskLevel() { return overallRiskLevel; }
    public void setOverallRiskLevel(String overallRiskLevel) { this.overallRiskLevel = overallRiskLevel; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
