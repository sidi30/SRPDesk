package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ReleaseComponent {

    private UUID id;
    private UUID releaseId;
    private UUID componentId;
    private Instant createdAt;

    public ReleaseComponent() {}

    public ReleaseComponent(UUID releaseId, UUID componentId) {
        this.releaseId = releaseId;
        this.componentId = componentId;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getReleaseId() { return releaseId; }
    public void setReleaseId(UUID releaseId) { this.releaseId = releaseId; }
    public UUID getComponentId() { return componentId; }
    public void setComponentId(UUID componentId) { this.componentId = componentId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
