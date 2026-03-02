package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class CiPolicy {

    private UUID id;
    private UUID orgId;
    private int maxCritical;
    private int maxHigh;
    private int minQualityScore;
    private boolean blockOnFail;
    private Instant createdAt;
    private Instant updatedAt;

    public CiPolicy() {}

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

    public String evaluate(int criticalCount, int highCount, int qualityScore) {
        if (criticalCount > maxCritical) return "FAIL";
        if (highCount > maxHigh) return "FAIL";
        if (qualityScore < minQualityScore) return "FAIL";
        if (criticalCount > 0 || highCount > 0) return "WARN";
        return "PASS";
    }
}
