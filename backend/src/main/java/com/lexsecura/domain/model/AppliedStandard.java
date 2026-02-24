package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class AppliedStandard {

    private UUID id;
    private UUID orgId;
    private UUID productId;
    private String standardCode; // EN 303 645, IEC 62443-4-1, etc.
    private String standardTitle;
    private String version;
    private String complianceStatus; // CLAIMED, PARTIAL, FULL, NOT_APPLICABLE
    private String notes;
    private String evidenceIds; // comma-separated UUIDs
    private Instant createdAt;
    private Instant updatedAt;

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
