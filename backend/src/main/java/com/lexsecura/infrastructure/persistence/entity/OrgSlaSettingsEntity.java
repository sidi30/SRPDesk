package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "org_sla_settings")
public class OrgSlaSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false, unique = true)
    private UUID orgId;

    @Column(name = "early_warning_hours", nullable = false)
    private int earlyWarningHours;

    @Column(name = "notification_hours", nullable = false)
    private int notificationHours;

    @Column(name = "final_report_days_after_patch", nullable = false)
    private int finalReportDaysAfterPatch;

    @Column(name = "final_report_days_after_resolve", nullable = false)
    private int finalReportDaysAfterResolve;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
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
    public int getEarlyWarningHours() { return earlyWarningHours; }
    public void setEarlyWarningHours(int earlyWarningHours) { this.earlyWarningHours = earlyWarningHours; }
    public int getNotificationHours() { return notificationHours; }
    public void setNotificationHours(int notificationHours) { this.notificationHours = notificationHours; }
    public int getFinalReportDaysAfterPatch() { return finalReportDaysAfterPatch; }
    public void setFinalReportDaysAfterPatch(int finalReportDaysAfterPatch) { this.finalReportDaysAfterPatch = finalReportDaysAfterPatch; }
    public int getFinalReportDaysAfterResolve() { return finalReportDaysAfterResolve; }
    public void setFinalReportDaysAfterResolve(int finalReportDaysAfterResolve) { this.finalReportDaysAfterResolve = finalReportDaysAfterResolve; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
