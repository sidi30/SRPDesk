package com.lexsecura.domain.repository;

import com.lexsecura.domain.model.NotificationLog;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository {

    NotificationLog save(NotificationLog log);

    List<NotificationLog> findAllByCraEventId(UUID craEventId);

    boolean existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
            UUID craEventId, String deadlineType, String alertLevel, Instant after);
}
