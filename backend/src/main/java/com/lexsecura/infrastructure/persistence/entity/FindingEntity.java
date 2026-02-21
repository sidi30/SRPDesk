package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "findings")
public class FindingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "release_id", nullable = false)
    private UUID releaseId;

    @Column(name = "component_id", nullable = false)
    private UUID componentId;

    @Column(name = "vulnerability_id", nullable = false)
    private UUID vulnerabilityId;

    @Column(length = 50, nullable = false)
    private String status = "OPEN";

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(length = 50, nullable = false)
    private String source = "OSV";

    @PrePersist
    protected void onCreate() {
        if (detectedAt == null) {
            detectedAt = Instant.now();
        }
        if (status == null) {
            status = "OPEN";
        }
        if (source == null) {
            source = "OSV";
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(UUID releaseId) {
        this.releaseId = releaseId;
    }

    public UUID getComponentId() {
        return componentId;
    }

    public void setComponentId(UUID componentId) {
        this.componentId = componentId;
    }

    public UUID getVulnerabilityId() {
        return vulnerabilityId;
    }

    public void setVulnerabilityId(UUID vulnerabilityId) {
        this.vulnerabilityId = vulnerabilityId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
