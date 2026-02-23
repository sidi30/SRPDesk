package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SbomShareLink {

    private UUID id;
    private UUID orgId;
    private UUID releaseId;
    private UUID evidenceId;
    private String token;
    private String recipientEmail;
    private String recipientOrg;
    private Instant expiresAt;
    private int maxDownloads;
    private int downloadCount;
    private boolean includeVex;
    private boolean includeQualityScore;
    private UUID createdBy;
    private Instant createdAt;
    private boolean revoked;
    private Instant revokedAt;

    public SbomShareLink() {}

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isExhausted() {
        return maxDownloads > 0 && downloadCount >= maxDownloads;
    }

    public boolean isValid() {
        return !revoked && !isExpired() && !isExhausted();
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
