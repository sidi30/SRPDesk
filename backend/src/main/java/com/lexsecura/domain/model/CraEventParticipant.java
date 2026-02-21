package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class CraEventParticipant {

    private UUID id;
    private UUID craEventId;
    private UUID userId;
    private String role;
    private Instant createdAt;

    public CraEventParticipant() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCraEventId() { return craEventId; }
    public void setCraEventId(UUID craEventId) { this.craEventId = craEventId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
