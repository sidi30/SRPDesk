package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.WebhookDelivery;

import java.util.List;
import java.util.UUID;

public interface WebhookDeliveryRepository {

    WebhookDelivery save(WebhookDelivery delivery);

    List<WebhookDelivery> findAllByWebhookId(UUID webhookId);
}
