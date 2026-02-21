package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.SrpSubmission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SrpSubmissionRepository {

    SrpSubmission save(SrpSubmission submission);

    Optional<SrpSubmission> findById(UUID id);

    Optional<SrpSubmission> findByIdAndCraEventId(UUID id, UUID craEventId);

    List<SrpSubmission> findAllByCraEventId(UUID craEventId);
}
