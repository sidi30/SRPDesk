package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SecurityAdvisory {

    private UUID id;
    private UUID orgId;
    private UUID craEventId;
    private UUID productId;
    private String title;
    private String severity;
    private String affectedVersions;
    private String description;
    private String remediation;
    private String advisoryUrl;
    private String status;
    private Instant publishedAt;
    private Instant notifiedAt;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public SecurityAdvisory() {}

    public SecurityAdvisory(UUID orgId, UUID craEventId, UUID productId, String title,
                            String severity, String description, UUID createdBy) {
        this.orgId = orgId;
        this.craEventId = craEventId;
        this.productId = productId;
        this.title = title;
        this.severity = severity;
        this.description = description;
        this.status = "DRAFT";
        this.createdBy = createdBy;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public UUID getCraEventId() { return craEventId; }
    public void setCraEventId(UUID craEventId) { this.craEventId = craEventId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getAffectedVersions() { return affectedVersions; }
    public void setAffectedVersions(String affectedVersions) { this.affectedVersions = affectedVersions; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRemediation() { return remediation; }
    public void setRemediation(String remediation) { this.remediation = remediation; }
    public String getAdvisoryUrl() { return advisoryUrl; }
    public void setAdvisoryUrl(String advisoryUrl) { this.advisoryUrl = advisoryUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getNotifiedAt() { return notifiedAt; }
    public void setNotifiedAt(Instant notifiedAt) { this.notifiedAt = notifiedAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
