package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "applied_standards")
public class AppliedStandardEntity {

    @Id
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "standard_code", length = 200, nullable = false)
    private String standardCode;

    @Column(name = "standard_title", length = 500, nullable = false)
    private String standardTitle;

    @Column(length = 100)
    private String version;

    @Column(name = "compliance_status", length = 50, nullable = false)
    private String complianceStatus;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "evidence_ids", columnDefinition = "TEXT")
    private String evidenceIds;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (complianceStatus == null) complianceStatus = "CLAIMED";
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
    public String getStandardCode() { return standardCode; }
    public void setStandardCode(String standardCode) { this.standardCode = standardCode; }
    public String getStandardTitle() { return standardTitle; }
    public void setStandardTitle(String standardTitle) { this.standardTitle = standardTitle; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getComplianceStatus() { return complianceStatus; }
    public void setComplianceStatus(String complianceStatus) { this.complianceStatus = complianceStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getEvidenceIds() { return evidenceIds; }
    public void setEvidenceIds(String evidenceIds) { this.evidenceIds = evidenceIds; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
