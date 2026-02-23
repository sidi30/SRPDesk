package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.WebhookDelivery;
import com.lexsecura.domain.repository.WebhookDeliveryRepository;
import com.lexsecura.infrastructure.persistence.entity.WebhookDeliveryEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaWebhookDeliveryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class WebhookDeliveryRepositoryAdapter implements WebhookDeliveryRepository {

    private final JpaWebhookDeliveryRepository jpa;

    public WebhookDeliveryRepositoryAdapter(JpaWebhookDeliveryRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public WebhookDelivery save(WebhookDelivery d) { return toDomain(jpa.save(toEntity(d))); }

    @Override
    public List<WebhookDelivery> findAllByWebhookId(UUID webhookId) {
        return jpa.findAllByWebhookIdOrderByDeliveredAtDesc(webhookId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    private WebhookDelivery toDomain(WebhookDeliveryEntity e) {
        WebhookDelivery d = new WebhookDelivery();
        d.setId(e.getId()); d.setWebhookId(e.getWebhookId()); d.setEventType(e.getEventType());
        d.setPayload(e.getPayload()); d.setHttpStatus(e.getHttpStatus());
        d.setResponseBody(e.getResponseBody()); d.setSuccess(e.isSuccess());
        d.setAttempt(e.getAttempt()); d.setDeliveredAt(e.getDeliveredAt());
        return d;
    }

    private WebhookDeliveryEntity toEntity(WebhookDelivery d) {
        WebhookDeliveryEntity e = new WebhookDeliveryEntity();
        e.setId(d.getId()); e.setWebhookId(d.getWebhookId()); e.setEventType(d.getEventType());
        e.setPayload(d.getPayload()); e.setHttpStatus(d.getHttpStatus());
        e.setResponseBody(d.getResponseBody()); e.setSuccess(d.isSuccess());
        e.setAttempt(d.getAttempt()); e.setDeliveredAt(d.getDeliveredAt());
        return e;
    }
}
