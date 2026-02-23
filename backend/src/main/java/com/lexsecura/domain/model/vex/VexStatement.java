package com.lexsecura.domain.model.vex;

import java.time.Instant;
import java.util.UUID;

public class VexStatement {

    private UUID id;
    private UUID vexDocumentId;
    private UUID findingId;
    private UUID decisionId;
    private String vulnerabilityId; // CVE-xxxx-xxxx
    private UUID productId;
    private VexStatus status;
    private VexJustification justification;
    private String impactStatement;
    private String actionStatement;
    private Instant createdAt;

    public VexStatement() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getVexDocumentId() { return vexDocumentId; }
    public void setVexDocumentId(UUID vexDocumentId) { this.vexDocumentId = vexDocumentId; }
    public UUID getFindingId() { return findingId; }
    public void setFindingId(UUID findingId) { this.findingId = findingId; }
    public UUID getDecisionId() { return decisionId; }
    public void setDecisionId(UUID decisionId) { this.decisionId = decisionId; }
    public String getVulnerabilityId() { return vulnerabilityId; }
    public void setVulnerabilityId(String vulnerabilityId) { this.vulnerabilityId = vulnerabilityId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public VexStatus getStatus() { return status; }
    public void setStatus(VexStatus status) { this.status = status; }
    public VexJustification getJustification() { return justification; }
    public void setJustification(VexJustification justification) { this.justification = justification; }
    public String getImpactStatement() { return impactStatement; }
    public void setImpactStatement(String impactStatement) { this.impactStatement = impactStatement; }
    public String getActionStatement() { return actionStatement; }
    public void setActionStatement(String actionStatement) { this.actionStatement = actionStatement; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
