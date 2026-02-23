package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class NotificationLog {

    private UUID id;
    private UUID orgId;
    private UUID craEventId;
    private String channel;
    private String recipient;
    private String subject;
    private String deadlineType;
    private String alertLevel;
    private Instant sentAt;

    public NotificationLog() {}

    public NotificationLog(UUID orgId, UUID craEventId, String channel, String recipient,
                           String subject, String deadlineType, String alertLevel) {
        this.orgId = orgId;
        this.craEventId = craEventId;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.deadlineType = deadlineType;
        this.alertLevel = alertLevel;
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
