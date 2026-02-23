package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sbom_share_links")
public class SbomShareLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "release_id", nullable = false)
    private UUID releaseId;

    @Column(name = "evidence_id", nullable = false)
    private UUID evidenceId;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "recipient_org")
    private String recipientOrg;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "max_downloads")
    private int maxDownloads;

    @Column(name = "download_count", nullable = false)
    private int downloadCount;

    @Column(name = "include_vex", nullable = false)
    private boolean includeVex;

    @Column(name = "include_quality_score", nullable = false)
    private boolean includeQualityScore;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public UUID getReleaseId() { return releaseId; }
    public void setReleaseId(UUID releaseId) { this.releaseId = releaseId; }
    public UUID getEvidenceId() { return evidenceId; }
    public void setEvidenceId(UUID evidenceId) { this.evidenceId = evidenceId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getRecipientOrg() { return recipientOrg; }
    public void setRecipientOrg(String recipientOrg) { this.recipientOrg = recipientOrg; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public int getMaxDownloads() { return maxDownloads; }
    public void setMaxDownloads(int maxDownloads) { this.maxDownloads = maxDownloads; }
    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }
    public boolean isIncludeVex() { return includeVex; }
    public void setIncludeVex(boolean includeVex) { this.includeVex = includeVex; }
    public boolean isIncludeQualityScore() { return includeQualityScore; }
    public void setIncludeQualityScore(boolean includeQualityScore) { this.includeQualityScore = includeQualityScore; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}
