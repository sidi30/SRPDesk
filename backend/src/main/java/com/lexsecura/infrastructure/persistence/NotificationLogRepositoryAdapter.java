package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.NotificationLog;
import com.lexsecura.domain.repository.NotificationLogRepository;
import com.lexsecura.infrastructure.persistence.entity.NotificationLogEntity;
import com.lexsecura.infrastructure.persistence.jpa.JpaNotificationLogRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class NotificationLogRepositoryAdapter implements NotificationLogRepository {

    private final JpaNotificationLogRepository jpa;

    public NotificationLogRepositoryAdapter(JpaNotificationLogRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public NotificationLog save(NotificationLog n) { return toDomain(jpa.save(toEntity(n))); }

    @Override
    public List<NotificationLog> findAllByCraEventId(UUID craEventId) {
        return jpa.findAllByCraEventId(craEventId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
            UUID craEventId, String deadlineType, String alertLevel, Instant after) {
        return jpa.existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
                craEventId, deadlineType, alertLevel, after);
    }

    private NotificationLog toDomain(NotificationLogEntity e) {
        NotificationLog n = new NotificationLog();
        n.setId(e.getId()); n.setOrgId(e.getOrgId()); n.setCraEventId(e.getCraEventId());
        n.setChannel(e.getChannel()); n.setRecipient(e.getRecipient()); n.setSubject(e.getSubject());
        n.setDeadlineType(e.getDeadlineType()); n.setAlertLevel(e.getAlertLevel()); n.setSentAt(e.getSentAt());
        return n;
    }

    private NotificationLogEntity toEntity(NotificationLog n) {
        NotificationLogEntity e = new NotificationLogEntity();
        e.setId(n.getId()); e.setOrgId(n.getOrgId()); e.setCraEventId(n.getCraEventId());
        e.setChannel(n.getChannel()); e.setRecipient(n.getRecipient()); e.setSubject(n.getSubject());
        e.setDeadlineType(n.getDeadlineType()); e.setAlertLevel(n.getAlertLevel()); e.setSentAt(n.getSentAt());
        return e;
    }
}
