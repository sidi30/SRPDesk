package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_advisories")
public class SecurityAdvisoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "cra_event_id", nullable = false)
    private UUID craEventId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(length = 500, nullable = false)
    private String title;

    @Column(length = 20, nullable = false)
    private String severity;

    @Column(name = "affected_versions", columnDefinition = "TEXT")
    private String affectedVersions;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String remediation;

    @Column(name = "advisory_url", length = 2048)
    private String advisoryUrl;

    @Column(length = 30, nullable = false)
    private String status;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (status == null) status = "DRAFT";
        if (severity == null) severity = "HIGH";
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

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
