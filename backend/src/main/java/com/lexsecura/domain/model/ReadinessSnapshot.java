package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ReadinessSnapshot {

    private UUID id;
    private UUID orgId;
    private UUID productId;
    private int overallScore;
    private String categoryScoresJson;
    private String actionItemsJson;
    private Instant snapshotAt;
    private UUID createdBy;

    public ReadinessSnapshot() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public String getCategoryScoresJson() { return categoryScoresJson; }
    public void setCategoryScoresJson(String categoryScoresJson) { this.categoryScoresJson = categoryScoresJson; }

    public String getActionItemsJson() { return actionItemsJson; }
    public void setActionItemsJson(String actionItemsJson) { this.actionItemsJson = actionItemsJson; }

    public Instant getSnapshotAt() { return snapshotAt; }
    public void setSnapshotAt(Instant snapshotAt) { this.snapshotAt = snapshotAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}
