package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.SrpSubmission;
import com.lexsecura.domain.repository.SrpSubmissionRepository;
import com.lexsecura.infrastructure.persistence.entity.SrpSubmissionEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class SrpSubmissionRepositoryAdapter implements SrpSubmissionRepository {

    private final JpaSrpSubmissionRepository jpa;

    public SrpSubmissionRepositoryAdapter(JpaSrpSubmissionRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public SrpSubmission save(SrpSubmission s) {
        return toDomain(jpa.save(toEntity(s)));
    }

    @Override
    public Optional<SrpSubmission> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<SrpSubmission> findByIdAndCraEventId(UUID id, UUID craEventId) {
        return jpa.findByIdAndCraEventId(id, craEventId).map(this::toDomain);
    }

    @Override
    public List<SrpSubmission> findAllByCraEventId(UUID craEventId) {
        return jpa.findAllByCraEventIdOrderByGeneratedAtDesc(craEventId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private SrpSubmission toDomain(SrpSubmissionEntity e) {
        SrpSubmission m = new SrpSubmission();
        m.setId(e.getId());
        m.setCraEventId(e.getCraEventId());
        m.setSubmissionType(e.getSubmissionType());
        m.setStatus(e.getStatus());
        m.setContentJson(e.getContentJson());
        m.setSchemaVersion(e.getSchemaVersion());
        m.setValidationErrors(e.getValidationErrors());
        m.setSubmittedReference(e.getSubmittedReference());
        m.setSubmittedAt(e.getSubmittedAt());
        m.setAcknowledgmentEvidenceId(e.getAcknowledgmentEvidenceId());
        m.setGeneratedBy(e.getGeneratedBy());
        m.setGeneratedAt(e.getGeneratedAt());
        m.setUpdatedAt(e.getUpdatedAt());
        return m;
    }

    private SrpSubmissionEntity toEntity(SrpSubmission m) {
        SrpSubmissionEntity e = new SrpSubmissionEntity();
        e.setId(m.getId());
        e.setCraEventId(m.getCraEventId());
        e.setSubmissionType(m.getSubmissionType());
        e.setStatus(m.getStatus());
        e.setContentJson(m.getContentJson());
        e.setSchemaVersion(m.getSchemaVersion());
        e.setValidationErrors(m.getValidationErrors());
        e.setSubmittedReference(m.getSubmittedReference());
        e.setSubmittedAt(m.getSubmittedAt());
        e.setAcknowledgmentEvidenceId(m.getAcknowledgmentEvidenceId());
        e.setGeneratedBy(m.getGeneratedBy());
        e.setGeneratedAt(m.getGeneratedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }
}
