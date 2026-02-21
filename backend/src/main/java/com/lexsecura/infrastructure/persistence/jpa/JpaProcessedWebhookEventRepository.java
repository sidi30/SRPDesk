package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.ProcessedWebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaProcessedWebhookEventRepository extends JpaRepository<ProcessedWebhookEventEntity, UUID> {
    boolean existsByForgeAndEventId(String forge, String eventId);
}
