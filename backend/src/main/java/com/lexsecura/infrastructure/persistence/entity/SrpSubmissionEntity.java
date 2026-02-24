package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "srp_submissions")
public class SrpSubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cra_event_id", nullable = false)
    private UUID craEventId;

    @Column(name = "submission_type", length = 50, nullable = false)
    private String submissionType;

    @Column(length = 50, nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", columnDefinition = "jsonb", nullable = false)
    private String contentJson;

    @Column(name = "schema_version", length = 20, nullable = false)
    private String schemaVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_errors", columnDefinition = "jsonb")
    private String validationErrors;

    @Column(name = "submitted_reference", length = 500)
    private String submittedReference;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "acknowledgment_evidence_id")
    private UUID acknowledgmentEvidenceId;

    @Column(name = "enisa_reference")
    private String enisaReference;

    @Column(name = "enisa_submitted_at")
    private Instant enisaSubmittedAt;

    @Column(name = "enisa_status", length = 50)
    private String enisaStatus;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "csirt_reference", length = 500)
    private String csirtReference;

    @Column(name = "csirt_submitted_at")
    private Instant csirtSubmittedAt;

    @Column(name = "csirt_status", length = 50)
    private String csirtStatus;

    @Column(name = "csirt_country_code", length = 5)
    private String csirtCountryCode;

    @Column(name = "generated_by", nullable = false)
    private UUID generatedBy;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (status == null) status = "DRAFT";
        if (contentJson == null) contentJson = "{}";
        if (schemaVersion == null) schemaVersion = "1.0";
        if (generatedAt == null) generatedAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCraEventId() { return craEventId; }
    public void setCraEventId(UUID craEventId) { this.craEventId = craEventId; }
    public String getSubmissionType() { return submissionType; }
    public void setSubmissionType(String submissionType) { this.submissionType = submissionType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getValidationErrors() { return validationErrors; }
    public void setValidationErrors(String validationErrors) { this.validationErrors = validationErrors; }
    public String getSubmittedReference() { return submittedReference; }
    public void setSubmittedReference(String submittedReference) { this.submittedReference = submittedReference; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public UUID getAcknowledgmentEvidenceId() { return acknowledgmentEvidenceId; }
    public void setAcknowledgmentEvidenceId(UUID acknowledgmentEvidenceId) { this.acknowledgmentEvidenceId = acknowledgmentEvidenceId; }
    public UUID getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(UUID generatedBy) { this.generatedBy = generatedBy; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getEnisaReference() { return enisaReference; }
    public void setEnisaReference(String enisaReference) { this.enisaReference = enisaReference; }
    public Instant getEnisaSubmittedAt() { return enisaSubmittedAt; }
    public void setEnisaSubmittedAt(Instant enisaSubmittedAt) { this.enisaSubmittedAt = enisaSubmittedAt; }
    public String getEnisaStatus() { return enisaStatus; }
    public void setEnisaStatus(String enisaStatus) { this.enisaStatus = enisaStatus; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public String getCsirtReference() { return csirtReference; }
    public void setCsirtReference(String csirtReference) { this.csirtReference = csirtReference; }
    public Instant getCsirtSubmittedAt() { return csirtSubmittedAt; }
    public void setCsirtSubmittedAt(Instant csirtSubmittedAt) { this.csirtSubmittedAt = csirtSubmittedAt; }
    public String getCsirtStatus() { return csirtStatus; }
    public void setCsirtStatus(String csirtStatus) { this.csirtStatus = csirtStatus; }
    public String getCsirtCountryCode() { return csirtCountryCode; }
    public void setCsirtCountryCode(String csirtCountryCode) { this.csirtCountryCode = csirtCountryCode; }
}
