package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.CiUploadEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CiUploadEventRepository {

    CiUploadEvent save(CiUploadEvent event);

    Optional<CiUploadEvent> findLatestByProductId(UUID productId);

    List<CiUploadEvent> findAllByOrgId(UUID orgId);
}
