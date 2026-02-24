package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "releases")
public class ReleaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(nullable = false, length = 100)
    private String version;

    @Column(name = "git_ref")
    private String gitRef;

    @Column(name = "build_id")
    private String buildId;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "supported_until")
    private Instant supportedUntil;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "update_type", length = 50)
    private String updateType;

    @Column(name = "security_impact", length = 50)
    private String securityImpact;

    @Column(name = "cve_ids", columnDefinition = "TEXT")
    private String cveIds;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getUpdateType() { return updateType; }
    public void setUpdateType(String updateType) { this.updateType = updateType; }
    public String getSecurityImpact() { return securityImpact; }
    public void setSecurityImpact(String securityImpact) { this.securityImpact = securityImpact; }
    public String getCveIds() { return cveIds; }
    public void setCveIds(String cveIds) { this.cveIds = cveIds; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
        if (status == null) status = "DRAFT";
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
