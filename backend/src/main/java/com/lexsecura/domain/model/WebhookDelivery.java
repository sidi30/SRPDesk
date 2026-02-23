package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class WebhookDelivery {

    private UUID id;
    private UUID webhookId;
    private String eventType;
    private String payload;
    private Integer httpStatus;
    private String responseBody;
    private boolean success;
    private int attempt;
    private Instant deliveredAt;

    public WebhookDelivery() {}

    public WebhookDelivery(UUID webhookId, String eventType, String payload) {
        this.webhookId = webhookId;
        this.eventType = eventType;
        this.payload = payload;
        this.attempt = 1;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWebhookId() { return webhookId; }
    public void setWebhookId(UUID webhookId) { this.webhookId = webhookId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public int getAttempt() { return attempt; }
    public void setAttempt(int attempt) { this.attempt = attempt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
