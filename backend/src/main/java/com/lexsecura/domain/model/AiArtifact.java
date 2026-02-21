package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class AiArtifact {

    private UUID id;
    private UUID aiJobId;
    private String kind;
    private String contentJson;
    private Instant createdAt;

    public AiArtifact() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAiJobId() { return aiJobId; }
    public void setAiJobId(UUID aiJobId) { this.aiJobId = aiJobId; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
