package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ConformityAssessment {

    private UUID id;
    private UUID orgId;
    private UUID productId;
    private String module; // MODULE_A, MODULE_H
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED, APPROVED
    private int currentStep;
    private int totalSteps;
    private String stepsData; // JSON string
    private Instant startedAt;
    private Instant completedAt;
    private UUID approvedBy;
    private Instant approvedAt;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCurrentStep() { return currentStep; }
    public void setCurrentStep(int currentStep) { this.currentStep = currentStep; }

    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

    public String getStepsData() { return stepsData; }
    public void setStepsData(String stepsData) { this.stepsData = stepsData; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
