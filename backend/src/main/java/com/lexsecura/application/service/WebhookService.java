package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.WebhookCreateRequest;
import com.lexsecura.application.dto.WebhookResponse;
import com.lexsecura.domain.model.Webhook;
import com.lexsecura.domain.model.WebhookDelivery;
import com.lexsecura.domain.repository.WebhookDeliveryRepository;
import com.lexsecura.domain.repository.WebhookRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;

@Service
@Transactional
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final int MAX_RETRIES = 3;

    private final WebhookRepository webhookRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WebhookService(WebhookRepository webhookRepository,
                          WebhookDeliveryRepository deliveryRepository,
                          ObjectMapper objectMapper) {
        this.webhookRepository = webhookRepository;
        this.deliveryRepository = deliveryRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public WebhookResponse create(WebhookCreateRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        Webhook webhook = new Webhook(orgId, request.name(), request.url(), request.channelType(), userId);
        webhook.setSecret(request.secret());
        webhook.setEventTypes(request.eventTypes() != null ? request.eventTypes() : "*");
        webhook = webhookRepository.save(webhook);

        log.info("Webhook created: id={}, org={}, channel={}", webhook.getId(), orgId, request.channelType());
        return toResponse(webhook);
    }

    @Transactional(readOnly = true)
    public List<WebhookResponse> list() {
        UUID orgId = TenantContext.getOrgId();
        return webhookRepository.findAllByOrgId(orgId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public void delete(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        webhookRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Webhook not found: " + id));
        webhookRepository.deleteById(id);
        log.info("Webhook deleted: id={}, org={}", id, orgId);
    }

    public void toggleEnabled(UUID id, boolean enabled) {
        UUID orgId = TenantContext.getOrgId();
        Webhook webhook = webhookRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Webhook not found: " + id));
        webhook.setEnabled(enabled);
        webhookRepository.save(webhook);
    }

    @Async
    public void dispatch(UUID orgId, String eventType, Map<String, Object> payload) {
        List<Webhook> webhooks = webhookRepository.findAllEnabledByOrgId(orgId);

        for (Webhook webhook : webhooks) {
            if (!webhook.matchesEventType(eventType)) continue;

            try {
                String json = objectMapper.writeValueAsString(Map.of(
                        "event", eventType,
                        "timestamp", java.time.Instant.now().toString(),
                        "data", payload
                ));

                deliver(webhook, eventType, json, 1);
            } catch (Exception e) {
                log.error("Webhook dispatch failed for webhook={}: {}", webhook.getId(), e.getMessage());
            }
        }
    }

    private void deliver(Webhook webhook, String eventType, String json, int attempt) {
        WebhookDelivery delivery = new WebhookDelivery(webhook.getId(), eventType, json);
        delivery.setAttempt(attempt);

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(webhook.getUrl()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "SRPDesk-Webhook/1.0");

            if (webhook.getSecret() != null && !webhook.getSecret().isBlank()) {
                String signature = computeHmac(json, webhook.getSecret());
                builder.header("X-SRPDesk-Signature", "sha256=" + signature);
            }

            // Format payload based on channel type
            String body = formatPayload(webhook.getChannelType(), json, eventType);
            builder.POST(HttpRequest.BodyPublishers.ofString(body));

            HttpResponse<String> response = httpClient.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());

            delivery.setHttpStatus(response.statusCode());
            delivery.setResponseBody(truncate(response.body(), 1000));
            delivery.setSuccess(response.statusCode() >= 200 && response.statusCode() < 300);

            if (!delivery.isSuccess() && attempt < MAX_RETRIES) {
                log.warn("Webhook delivery failed (attempt {}/{}): status={}, webhook={}",
                        attempt, MAX_RETRIES, response.statusCode(), webhook.getId());
                deliver(webhook, eventType, json, attempt + 1);
                return;
            }
        } catch (Exception e) {
            delivery.setSuccess(false);
            delivery.setResponseBody(truncate(e.getMessage(), 1000));
            log.error("Webhook delivery error: webhook={}, attempt={}: {}",
                    webhook.getId(), attempt, e.getMessage());
        }

        deliveryRepository.save(delivery);
    }

    private String formatPayload(String channelType, String json, String eventType) {
        try {
            if ("SLACK".equalsIgnoreCase(channelType)) {
                Map<String, Object> data = objectMapper.readValue(json, Map.class);
                String text = String.format("*[SRPDesk CRA Alert]* %s\n%s", eventType, data.get("data"));
                return objectMapper.writeValueAsString(Map.of("text", text));
            }
            if ("TEAMS".equalsIgnoreCase(channelType)) {
                Map<String, Object> data = objectMapper.readValue(json, Map.class);
                String text = String.format("**[SRPDesk CRA Alert]** %s\n%s", eventType, data.get("data"));
                return objectMapper.writeValueAsString(Map.of("text", text));
            }
        } catch (Exception e) {
            log.warn("Failed to format payload for {}, sending raw JSON", channelType);
        }
        return json;
    }

    private String computeHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    private WebhookResponse toResponse(Webhook w) {
        return new WebhookResponse(w.getId(), w.getName(), w.getUrl(), w.getEventTypes(),
                w.getChannelType(), w.isEnabled(), w.getCreatedAt(), w.getUpdatedAt());
    }
}
