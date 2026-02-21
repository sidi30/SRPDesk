package com.lexsecura.domain.repository;

public interface ProcessedWebhookEventRepository {

    boolean existsByForgeAndEventId(String forge, String eventId);

    void save(String forge, String eventId, String eventType);
}
