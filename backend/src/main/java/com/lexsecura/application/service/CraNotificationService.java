package com.lexsecura.application.service;

import com.lexsecura.application.dto.SlaResponse;
import com.lexsecura.application.port.EmailPort;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.model.NotificationLog;
import com.lexsecura.domain.repository.CraEventRepository;
import com.lexsecura.domain.repository.NotificationLogRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CraNotificationService {

    private static final Logger log = LoggerFactory.getLogger(CraNotificationService.class);

    private final CraEventRepository eventRepository;
    private final SlaService slaService;
    private final NotificationLogRepository notificationLogRepository;
    private final WebhookService webhookService;
    private final EmailPort emailPort;
    private final Counter overdueAlertCounter;
    private final AtomicInteger openEventsGauge = new AtomicInteger(0);
    private final AtomicInteger overdueEventsGauge = new AtomicInteger(0);

    @Value("${app.cra.alert-email:}")
    private String alertEmail;

    public CraNotificationService(CraEventRepository eventRepository,
                                  SlaService slaService,
                                  NotificationLogRepository notificationLogRepository,
                                  WebhookService webhookService,
                                  EmailPort emailPort,
                                  MeterRegistry meterRegistry) {
        this.eventRepository = eventRepository;
        this.slaService = slaService;
        this.notificationLogRepository = notificationLogRepository;
        this.webhookService = webhookService;
        this.emailPort = emailPort;
        this.overdueAlertCounter = Counter.builder("cra.notifications.overdue_alerts")
                .description("Number of overdue deadline alerts sent")
                .register(meterRegistry);
        Gauge.builder("cra.events.open", openEventsGauge, AtomicInteger::get)
                .description("Number of open CRA events")
                .register(meterRegistry);
        Gauge.builder("cra.events.overdue", overdueEventsGauge, AtomicInteger::get)
                .description("Number of CRA events with overdue deadlines")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${app.cra.notification-check-ms:900000}")
    public void checkDeadlines() {
        List<CraEvent> openEvents = new java.util.ArrayList<>(eventRepository.findAllByStatus("DRAFT"));
        openEvents.addAll(eventRepository.findAllByStatus("IN_REVIEW"));

        openEventsGauge.set(openEvents.size());
        int overdueCount = 0;

        for (CraEvent event : openEvents) {
            try {
                SlaResponse sla = slaService.computeSla(event);

                overdueCount += checkDeadline(event, "Early Warning", sla.earlyWarning());
                overdueCount += checkDeadline(event, "Notification", sla.notification());
                if (sla.finalReport() != null) {
                    overdueCount += checkDeadline(event, "Final Report", sla.finalReport());
                }
            } catch (Exception e) {
                log.error("Error checking deadlines for event {}", event.getId(), e);
            }
        }

        overdueEventsGauge.set(overdueCount);
    }

    private int checkDeadline(CraEvent event, String deadlineName, SlaResponse.SlaDeadline deadline) {
        if (deadline == null) return 0;

        long remainingSecs = deadline.remainingSeconds();
        String alertLevel;

        if (deadline.overdue()) {
            alertLevel = "OVERDUE";
        } else if (remainingSecs <= 2 * 3600) {
            alertLevel = "CRITICAL";
        } else if (remainingSecs <= 6 * 3600) {
            alertLevel = "WARNING";
        } else {
            return 0;
        }

        // Deduplicate: don't send same alert within 1 hour
        boolean alreadySent = notificationLogRepository
                .existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
                        event.getId(), deadlineName, alertLevel,
                        Instant.now().minus(1, ChronoUnit.HOURS));
        if (alreadySent) return deadline.overdue() ? 1 : 0;

        String subject = String.format("[CRA %s] %s deadline for '%s'",
                alertLevel, deadlineName, event.getTitle());

        // Log alert
        log.warn("[CRA ALERT] {}: {} deadline for event '{}' (id={}, org={})",
                alertLevel, deadlineName, event.getTitle(), event.getId(), event.getOrgId());

        // Send email
        if (alertEmail != null && !alertEmail.isBlank()) {
            emailPort.send(alertEmail, subject,
                    "<p><strong>" + alertLevel + "</strong>: " + deadlineName +
                            " deadline for CRA event '" + event.getTitle() + "'</p>" +
                            "<p>Due: " + deadline.dueAt() + "</p>");
        }

        // Dispatch webhook
        webhookService.dispatch(event.getOrgId(), "CRA_SLA_ALERT", Map.of(
                "eventId", event.getId().toString(),
                "eventTitle", event.getTitle(),
                "deadline", deadlineName,
                "alertLevel", alertLevel,
                "remainingSeconds", remainingSecs,
                "dueAt", deadline.dueAt().toString()
        ));

        // Record notification
        NotificationLog notifLog = new NotificationLog(
                event.getOrgId(), event.getId(), "EMAIL+WEBHOOK",
                alertEmail != null ? alertEmail : "log-only",
                subject, deadlineName, alertLevel);
        notificationLogRepository.save(notifLog);

        overdueAlertCounter.increment();

        return deadline.overdue() ? 1 : 0;
    }
}
