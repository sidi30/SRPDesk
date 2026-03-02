package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ci_policies")
public class CiPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false, unique = true)
    private UUID orgId;

    @Column(name = "max_critical", nullable = false)
    private int maxCritical;

    @Column(name = "max_high", nullable = false)
    private int maxHigh;

    @Column(name = "min_quality_score", nullable = false)
    private int minQualityScore;

    @Column(name = "block_on_fail", nullable = false)
    private boolean blockOnFail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public int getMaxCritical() { return maxCritical; }
    public void setMaxCritical(int maxCritical) { this.maxCritical = maxCritical; }
    public int getMaxHigh() { return maxHigh; }
    public void setMaxHigh(int maxHigh) { this.maxHigh = maxHigh; }
    public int getMinQualityScore() { return minQualityScore; }
    public void setMinQualityScore(int minQualityScore) { this.minQualityScore = minQualityScore; }
    public boolean isBlockOnFail() { return blockOnFail; }
    public void setBlockOnFail(boolean blockOnFail) { this.blockOnFail = blockOnFail; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
