package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "supplier_sboms")
public class SupplierSbomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "release_id", nullable = false)
    private UUID releaseId;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    @Column(name = "supplier_url")
    private String supplierUrl;

    @Column(name = "evidence_id")
    private UUID evidenceId;

    @Column(name = "component_count", nullable = false)
    private int componentCount;

    @Column(name = "format", nullable = false)
    private String format = "CYCLONEDX";

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    @Column(name = "imported_by")
    private UUID importedBy;

    @PrePersist
    void prePersist() {
        if (importedAt == null) importedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public UUID getReleaseId() { return releaseId; }
    public void setReleaseId(UUID releaseId) { this.releaseId = releaseId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierUrl() { return supplierUrl; }
    public void setSupplierUrl(String supplierUrl) { this.supplierUrl = supplierUrl; }
    public UUID getEvidenceId() { return evidenceId; }
    public void setEvidenceId(UUID evidenceId) { this.evidenceId = evidenceId; }
    public int getComponentCount() { return componentCount; }
    public void setComponentCount(int componentCount) { this.componentCount = componentCount; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public Instant getImportedAt() { return importedAt; }
    public void setImportedAt(Instant importedAt) { this.importedAt = importedAt; }
    public UUID getImportedBy() { return importedBy; }
    public void setImportedBy(UUID importedBy) { this.importedBy = importedBy; }
}
