package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_log")
public class NotificationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "cra_event_id", nullable = false)
    private UUID craEventId;

    @Column(length = 30, nullable = false)
    private String channel;

    @Column(length = 500, nullable = false)
    private String recipient;

    @Column(length = 500, nullable = false)
    private String subject;

    @Column(name = "deadline_type", length = 50, nullable = false)
    private String deadlineType;

    @Column(name = "alert_level", length = 20, nullable = false)
    private String alertLevel;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @PrePersist
    void prePersist() {
        if (sentAt == null) sentAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public UUID getCraEventId() { return craEventId; }
    public void setCraEventId(UUID craEventId) { this.craEventId = craEventId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDeadlineType() { return deadlineType; }
    public void setDeadlineType(String deadlineType) { this.deadlineType = deadlineType; }
    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}
