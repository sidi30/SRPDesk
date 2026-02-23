package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.WebhookDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaWebhookDeliveryRepository extends JpaRepository<WebhookDeliveryEntity, UUID> {

    List<WebhookDeliveryEntity> findAllByWebhookIdOrderByDeliveredAtDesc(UUID webhookId);
}
