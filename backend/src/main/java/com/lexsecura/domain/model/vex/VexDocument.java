package com.lexsecura.domain.model.vex;

import java.time.Instant;
import java.util.UUID;

public class VexDocument {

    private UUID id;
    private UUID orgId;
    private UUID releaseId;
    private VexFormat format;
    private int version;
    private String status; // DRAFT, PUBLISHED, SUPERSEDED
    private String documentJson;
    private String sha256Hash;
    private String generatedBy;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public VexDocument() {}

    public VexDocument(UUID orgId, UUID releaseId, VexFormat format, String documentJson,
                       String sha256Hash, String generatedBy) {
        this.orgId = orgId;
        this.releaseId = releaseId;
        this.format = format;
        this.version = 1;
        this.status = "DRAFT";
        this.documentJson = documentJson;
        this.sha256Hash = sha256Hash;
        this.generatedBy = generatedBy;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public UUID getReleaseId() { return releaseId; }
    public void setReleaseId(UUID releaseId) { this.releaseId = releaseId; }
    public VexFormat getFormat() { return format; }
    public void setFormat(VexFormat format) { this.format = format; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDocumentJson() { return documentJson; }
    public void setDocumentJson(String documentJson) { this.documentJson = documentJson; }
    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
