package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.WebhookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaWebhookRepository extends JpaRepository<WebhookEntity, UUID> {

    Optional<WebhookEntity> findByIdAndOrgId(UUID id, UUID orgId);

    List<WebhookEntity> findAllByOrgId(UUID orgId);

    List<WebhookEntity> findAllByOrgIdAndEnabledTrue(UUID orgId);
}
