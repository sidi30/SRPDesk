package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eu_declarations_of_conformity")
public class EuDocEntity {

    @Id
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "declaration_number", length = 200, nullable = false)
    private String declarationNumber;

    @Column(name = "manufacturer_name", length = 500, nullable = false)
    private String manufacturerName;

    @Column(name = "manufacturer_address", columnDefinition = "TEXT", nullable = false)
    private String manufacturerAddress;

    @Column(name = "authorized_rep_name", length = 500)
    private String authorizedRepName;

    @Column(name = "authorized_rep_address", columnDefinition = "TEXT")
    private String authorizedRepAddress;

    @Column(name = "product_name", length = 500, nullable = false)
    private String productName;

    @Column(name = "product_identification", columnDefinition = "TEXT", nullable = false)
    private String productIdentification;

    @Column(name = "conformity_assessment_module", length = 50, nullable = false)
    private String conformityAssessmentModule;

    @Column(name = "notified_body_name", length = 500)
    private String notifiedBodyName;

    @Column(name = "notified_body_number", length = 50)
    private String notifiedBodyNumber;

    @Column(name = "notified_body_certificate", length = 200)
    private String notifiedBodyCertificate;

    @Column(name = "harmonised_standards", columnDefinition = "TEXT")
    private String harmonisedStandards;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(name = "declaration_text", columnDefinition = "TEXT", nullable = false)
    private String declarationText;

    @Column(name = "signed_by", length = 500, nullable = false)
    private String signedBy;

    @Column(name = "signed_role", length = 200, nullable = false)
    private String signedRole;

    @Column(name = "signed_at", nullable = false)
    private Instant signedAt;

    @Column(length = 50, nullable = false)
    private String status;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (status == null) status = "DRAFT";
        if (conformityAssessmentModule == null) conformityAssessmentModule = "MODULE_A";
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
