package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class OrgSlaSettings {

    private UUID id;
    private UUID orgId;
    private int earlyWarningHours;
    private int notificationHours;
    private int finalReportDaysAfterPatch;
    private int finalReportDaysAfterResolve;
    private Instant createdAt;
    private Instant updatedAt;

    public OrgSlaSettings() {
        this.earlyWarningHours = 24;
        this.notificationHours = 72;
        this.finalReportDaysAfterPatch = 14;
        this.finalReportDaysAfterResolve = 30;
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
