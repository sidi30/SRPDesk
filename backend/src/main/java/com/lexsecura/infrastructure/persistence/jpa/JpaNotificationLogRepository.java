package com.lexsecura.infrastructure.persistence.jpa;

import com.lexsecura.infrastructure.persistence.entity.NotificationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaNotificationLogRepository extends JpaRepository<NotificationLogEntity, UUID> {

    List<NotificationLogEntity> findAllByCraEventId(UUID craEventId);

    boolean existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
            UUID craEventId, String deadlineType, String alertLevel, Instant after);
}
