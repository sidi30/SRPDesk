package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Evidence {

    private UUID id;
    private UUID releaseId;
    private UUID orgId;
    private EvidenceType type;
    private String filename;
    private String contentType;
    private long size;
    private String sha256;
    private String storageUri;
    private Instant createdAt;
    private UUID createdBy;

    public Evidence() {
    }

    public Evidence(UUID releaseId, UUID orgId, EvidenceType type, String filename,
                    String contentType, long size, String sha256, String storageUri, UUID createdBy) {
        this.releaseId = releaseId;
        this.orgId = orgId;
        this.type = type;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.sha256 = sha256;
        this.storageUri = storageUri;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getReleaseId() { return releaseId; }
    public void setReleaseId(UUID releaseId) { this.releaseId = releaseId; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public EvidenceType getType() { return type; }
    public void setType(EvidenceType type) { this.type = type; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }

    public String getStorageUri() { return storageUri; }
    public void setStorageUri(String storageUri) { this.storageUri = storageUri; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}
