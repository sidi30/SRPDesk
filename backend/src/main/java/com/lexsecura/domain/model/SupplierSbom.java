package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SupplierSbom {

    private UUID id;
    private UUID orgId;
    private UUID releaseId;
    private String supplierName;
    private String supplierUrl;
    private UUID evidenceId;
    private int componentCount;
    private String format;
    private Instant importedAt;
    private UUID importedBy;

    public SupplierSbom() {}

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
