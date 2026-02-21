package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Finding {

    private UUID id;
    private UUID releaseId;
    private UUID componentId;
    private UUID vulnerabilityId;
    private String status;
    private Instant detectedAt;
    private String source;

    public Finding() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getReleaseId() { return releaseId; }
    public void setReleaseId(UUID releaseId) { this.releaseId = releaseId; }
    public UUID getComponentId() { return componentId; }
    public void setComponentId(UUID componentId) { this.componentId = componentId; }
    public UUID getVulnerabilityId() { return vulnerabilityId; }
    public void setVulnerabilityId(UUID vulnerabilityId) { this.vulnerabilityId = vulnerabilityId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
