package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.AiJob;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiJobRepository {

    AiJob save(AiJob job);

    Optional<AiJob> findById(UUID id);

    Optional<AiJob> findByIdAndOrgId(UUID id, UUID orgId);

    List<AiJob> findAllByOrgId(UUID orgId);
}
