package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class EuDeclarationOfConformity {

    private UUID id;
    private UUID orgId;
    private UUID productId;
    private String declarationNumber;
    private String manufacturerName;
    private String manufacturerAddress;
    private String authorizedRepName;
    private String authorizedRepAddress;
    private String productName;
    private String productIdentification;
    private String conformityAssessmentModule; // MODULE_A, MODULE_H
    private String notifiedBodyName;
    private String notifiedBodyNumber;
    private String notifiedBodyCertificate;
    private String harmonisedStandards;
    private String additionalInfo;
    private String declarationText;
    private String signedBy;
    private String signedRole;
    private Instant signedAt;
    private String status; // DRAFT, SIGNED, PUBLISHED
    private Instant publishedAt;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getDeclarationNumber() { return declarationNumber; }
    public void setDeclarationNumber(String declarationNumber) { this.declarationNumber = declarationNumber; }

    public String getManufacturerName() { return manufacturerName; }
    public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }

    public String getManufacturerAddress() { return manufacturerAddress; }
    public void setManufacturerAddress(String manufacturerAddress) { this.manufacturerAddress = manufacturerAddress; }

    public String getAuthorizedRepName() { return authorizedRepName; }
    public void setAuthorizedRepName(String authorizedRepName) { this.authorizedRepName = authorizedRepName; }

    public String getAuthorizedRepAddress() { return authorizedRepAddress; }
    public void setAuthorizedRepAddress(String authorizedRepAddress) { this.authorizedRepAddress = authorizedRepAddress; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductIdentification() { return productIdentification; }
    public void setProductIdentification(String productIdentification) { this.productIdentification = productIdentification; }

    public String getConformityAssessmentModule() { return conformityAssessmentModule; }
    public void setConformityAssessmentModule(String conformityAssessmentModule) { this.conformityAssessmentModule = conformityAssessmentModule; }

    public String getNotifiedBodyName() { return notifiedBodyName; }
    public void setNotifiedBodyName(String notifiedBodyName) { this.notifiedBodyName = notifiedBodyName; }

    public String getNotifiedBodyNumber() { return notifiedBodyNumber; }
    public void setNotifiedBodyNumber(String notifiedBodyNumber) { this.notifiedBodyNumber = notifiedBodyNumber; }

    public String getNotifiedBodyCertificate() { return notifiedBodyCertificate; }
    public void setNotifiedBodyCertificate(String notifiedBodyCertificate) { this.notifiedBodyCertificate = notifiedBodyCertificate; }

    public String getHarmonisedStandards() { return harmonisedStandards; }
    public void setHarmonisedStandards(String harmonisedStandards) { this.harmonisedStandards = harmonisedStandards; }

    public String getAdditionalInfo() { return additionalInfo; }
    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getDeclarationText() { return declarationText; }
    public void setDeclarationText(String declarationText) { this.declarationText = declarationText; }

    public String getSignedBy() { return signedBy; }
    public void setSignedBy(String signedBy) { this.signedBy = signedBy; }

    public String getSignedRole() { return signedRole; }
    public void setSignedRole(String signedRole) { this.signedRole = signedRole; }

    public Instant getSignedAt() { return signedAt; }
    public void setSignedAt(Instant signedAt) { this.signedAt = signedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
