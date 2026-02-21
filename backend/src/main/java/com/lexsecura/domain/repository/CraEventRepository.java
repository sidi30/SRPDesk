package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.CraEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CraEventRepository {

    CraEvent save(CraEvent event);

    Optional<CraEvent> findById(UUID id);

    Optional<CraEvent> findByIdAndOrgId(UUID id, UUID orgId);

    List<CraEvent> findAllByOrgId(UUID orgId);

    List<CraEvent> findAllByOrgIdAndProductId(UUID orgId, UUID productId);

    List<CraEvent> findAllByOrgIdAndStatus(UUID orgId, String status);

    List<CraEvent> findAllByOrgIdAndProductIdAndStatus(UUID orgId, UUID productId, String status);

    List<CraEvent> findAllByStatus(String status);
}
