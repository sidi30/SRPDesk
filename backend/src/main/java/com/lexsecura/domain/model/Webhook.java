package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Webhook {

    private UUID id;
    private UUID orgId;
    private String name;
    private String url;
    private String secret;
    private String eventTypes;
    private String channelType;
    private boolean enabled;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public Webhook() {}

    public Webhook(UUID orgId, String name, String url, String channelType, UUID createdBy) {
        this.orgId = orgId;
        this.name = name;
        this.url = url;
        this.channelType = channelType;
        this.eventTypes = "*";
        this.enabled = true;
        this.createdBy = createdBy;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getEventTypes() { return eventTypes; }
    public void setEventTypes(String eventTypes) { this.eventTypes = eventTypes; }
    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public boolean matchesEventType(String eventType) {
        if ("*".equals(eventTypes)) return true;
        for (String t : eventTypes.split(",")) {
            if (t.trim().equals(eventType)) return true;
        }
        return false;
    }
}
