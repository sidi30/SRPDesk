package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.Webhook;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookRepository {

    Webhook save(Webhook webhook);

    Optional<Webhook> findByIdAndOrgId(UUID id, UUID orgId);

    List<Webhook> findAllByOrgId(UUID orgId);

    List<Webhook> findAllEnabledByOrgId(UUID orgId);

    void deleteById(UUID id);
}
