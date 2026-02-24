package com.lexsecura.infrastructure.persistence;

import com.lexsecura.domain.model.NotificationLog;
import com.lexsecura.domain.repository.NotificationLogRepository;
import com.lexsecura.infrastructure.persistence.jpa.JpaNotificationLogRepository;
import com.lexsecura.infrastructure.persistence.mapper.PersistenceMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class NotificationLogRepositoryAdapter implements NotificationLogRepository {

    private final JpaNotificationLogRepository jpa;
    private final PersistenceMapper mapper;

    public NotificationLogRepositoryAdapter(JpaNotificationLogRepository jpa, PersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public NotificationLog save(NotificationLog n) {
        return mapper.toDomain(jpa.save(mapper.toEntity(n)));
    }

    @Override
    public List<NotificationLog> findAllByCraEventId(UUID craEventId) {
        return jpa.findAllByCraEventId(craEventId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
            UUID craEventId, String deadlineType, String alertLevel, Instant after) {
        return jpa.existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
                craEventId, deadlineType, alertLevel, after);
    }
}
