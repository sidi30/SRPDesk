package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(length = 500, nullable = false)
    private String title;

    @Column(length = 100, nullable = false)
    private String methodology;

    @Column(length = 50, nullable = false)
    private String status;

    @Column(name = "overall_risk_level", length = 50)
    private String overallRiskLevel;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (methodology == null) methodology = "STRIDE";
        if (status == null) status = "DRAFT";
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

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
