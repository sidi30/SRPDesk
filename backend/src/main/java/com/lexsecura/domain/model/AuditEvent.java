package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class AuditEvent {

    private UUID id;
    private UUID orgId;
    private String entityType;
    private UUID entityId;
    private String action;
    private UUID actor;
    private String payloadJson;
    private Instant createdAt;
    private String prevHash;
    private String hash;

    public AuditEvent() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public UUID getActor() { return actor; }
    public void setActor(UUID actor) { this.actor = actor; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getPrevHash() { return prevHash; }
    public void setPrevHash(String prevHash) { this.prevHash = prevHash; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
}
