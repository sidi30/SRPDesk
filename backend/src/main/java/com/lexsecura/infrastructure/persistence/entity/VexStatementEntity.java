package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vex_statements")
public class VexStatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vex_document_id", nullable = false)
    private UUID vexDocumentId;

    @Column(name = "finding_id", nullable = false)
    private UUID findingId;

    @Column(name = "decision_id")
    private UUID decisionId;

    @Column(name = "vulnerability_id", length = 50, nullable = false)
    private String vulnerabilityId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(length = 30, nullable = false)
    private String status;

    @Column(length = 80)
    private String justification;

    @Column(name = "impact_statement", columnDefinition = "TEXT")
    private String impactStatement;

    @Column(name = "action_statement", columnDefinition = "TEXT")
    private String actionStatement;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    public String getImpactStatement() { return impactStatement; }
    public void setImpactStatement(String impactStatement) { this.impactStatement = impactStatement; }
    public String getActionStatement() { return actionStatement; }
    public void setActionStatement(String actionStatement) { this.actionStatement = actionStatement; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
