package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conformity_assessments")
public class ConformityAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(length = 50, nullable = false)
    private String module;

    @Column(length = 50, nullable = false)
    private String status;

    @Column(name = "current_step", nullable = false)
    private int currentStep;

    @Column(name = "total_steps", nullable = false)
    private int totalSteps;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps_data", columnDefinition = "jsonb", nullable = false)
    private String stepsData;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (status == null) status = "NOT_STARTED";
        if (stepsData == null) stepsData = "[]";
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

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
