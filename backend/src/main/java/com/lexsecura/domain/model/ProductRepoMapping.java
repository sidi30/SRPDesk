package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ProductRepoMapping {

    private UUID id;
    private UUID orgId;
    private UUID productId;
    private String forge;
    private long projectId;
    private String repoUrl;
    private Instant createdAt;

    public ProductRepoMapping() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getForge() { return forge; }
    public void setForge(String forge) { this.forge = forge; }
    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }
    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
