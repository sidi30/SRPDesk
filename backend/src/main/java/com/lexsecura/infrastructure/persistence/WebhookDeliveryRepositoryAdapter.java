package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.WebhookDelivery;
import com.lexsecura.domain.repository.WebhookDeliveryRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaWebhookDeliveryRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class WebhookDeliveryRepositoryAdapter implements WebhookDeliveryRepository {

    private final JpaWebhookDeliveryRepository jpa;
    private final PersistenceMapper mapper;

    public WebhookDeliveryRepositoryAdapter(JpaWebhookDeliveryRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public WebhookDelivery save(WebhookDelivery d) {
        return mapper.toDomain(jpa.save(mapper.toEntity(d)));
    }

    @Override
    public List<WebhookDelivery> findAllByWebhookId(UUID webhookId) {
        return jpa.findAllByWebhookIdOrderByDeliveredAtDesc(webhookId).stream().map(mapper::toDomain).toList();
    }
}
