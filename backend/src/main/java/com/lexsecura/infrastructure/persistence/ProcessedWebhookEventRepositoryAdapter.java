package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.repository.ProcessedWebhookEventRepository;
import com.lexsecura.infrastructure.persistence.entity.ProcessedWebhookEventEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaProcessedWebhookEventRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ProcessedWebhookEventRepositoryAdapter implements ProcessedWebhookEventRepository {

    private final JpaProcessedWebhookEventRepository jpa;

    public ProcessedWebhookEventRepositoryAdapter(JpaProcessedWebhookEventRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean existsByForgeAndEventId(String forge, String eventId) {
        return jpa.existsByForgeAndEventId(forge, eventId);
    }

    @Override
    public void save(String forge, String eventId, String eventType) {
        var entity = new ProcessedWebhookEventEntity();
        entity.setForge(forge);
        entity.setEventId(eventId);
        entity.setEventType(eventType);
        jpa.save(entity);
    }
}
