package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CraChecklistItem {

    private UUID id;
    private UUID orgId;
    private UUID productId;
    private String requirementRef;
    private String category;
    private String title;
    private String description;
    private String status;
    private List<UUID> evidenceIds;
    private String notes;
    private UUID assessedBy;
    private Instant assessedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public CraChecklistItem() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getRequirementRef() { return requirementRef; }
    public void setRequirementRef(String requirementRef) { this.requirementRef = requirementRef; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<UUID> getEvidenceIds() { return evidenceIds; }
    public void setEvidenceIds(List<UUID> evidenceIds) { this.evidenceIds = evidenceIds; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public UUID getAssessedBy() { return assessedBy; }
    public void setAssessedBy(UUID assessedBy) { this.assessedBy = assessedBy; }

    public Instant getAssessedAt() { return assessedAt; }
    public void setAssessedAt(Instant assessedAt) { this.assessedAt = assessedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
