package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Release {

    private UUID id;
    private UUID productId;
    private UUID orgId;
    private String version;
    private String gitRef;
    private String buildId;
    private Instant releasedAt;
    private Instant supportedUntil;
    private ReleaseStatus status;
    private String updateType;
    private String securityImpact;
    private String cveIds;
    private Instant createdAt;
    private Instant updatedAt;

    public Release() {
    }

    public Release(UUID productId, String version) {
        this.productId = productId;
        this.version = version;
        this.status = ReleaseStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getGitRef() { return gitRef; }
    public void setGitRef(String gitRef) { this.gitRef = gitRef; }

    public String getBuildId() { return buildId; }
    public void setBuildId(String buildId) { this.buildId = buildId; }

    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }

    public Instant getSupportedUntil() { return supportedUntil; }
    public void setSupportedUntil(Instant supportedUntil) { this.supportedUntil = supportedUntil; }

    public ReleaseStatus getStatus() { return status; }
    public void setStatus(ReleaseStatus status) { this.status = status; }

    public String getUpdateType() { return updateType; }
    public void setUpdateType(String updateType) { this.updateType = updateType; }

    public String getSecurityImpact() { return securityImpact; }
    public void setSecurityImpact(String securityImpact) { this.securityImpact = securityImpact; }

    public String getCveIds() { return cveIds; }
    public void setCveIds(String cveIds) { this.cveIds = cveIds; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
