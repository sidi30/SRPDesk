package com.lexsecura.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class FindingDecision {

    private UUID id;
    private UUID findingId;
    private String decisionType;
    private String rationale;
    private LocalDate dueDate;
    private UUID decidedBy;
    private UUID fixReleaseId;
    private Instant createdAt;

    public FindingDecision() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFindingId() { return findingId; }
    public void setFindingId(UUID findingId) { this.findingId = findingId; }
    public String getDecisionType() { return decisionType; }
    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }
    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public UUID getDecidedBy() { return decidedBy; }
    public void setDecidedBy(UUID decidedBy) { this.decidedBy = decidedBy; }
    public UUID getFixReleaseId() { return fixReleaseId; }
    public void setFixReleaseId(UUID fixReleaseId) { this.fixReleaseId = fixReleaseId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
