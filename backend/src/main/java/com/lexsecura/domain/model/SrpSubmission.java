package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SrpSubmission {

    private UUID id;
    private UUID craEventId;
    private String submissionType;
    private String status;
    private String contentJson;
    private String schemaVersion;
    private String validationErrors;
    private String submittedReference;
    private Instant submittedAt;
    private UUID acknowledgmentEvidenceId;
    private String enisaReference;
    private Instant enisaSubmittedAt;
    private String enisaStatus;
    private int retryCount;
    private String lastError;
    private String csirtReference;
    private Instant csirtSubmittedAt;
    private String csirtStatus;
    private String csirtCountryCode;
    private UUID generatedBy;
    private Instant generatedAt;
    private Instant updatedAt;

    public SrpSubmission() {}

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
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
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
