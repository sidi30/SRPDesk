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
}
