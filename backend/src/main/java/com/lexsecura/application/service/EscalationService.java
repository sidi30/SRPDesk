package com.lexsecura.application.service;

import com.lexsecura.application.dto.SlaResponse;
import com.lexsecura.application.port.EmailPort;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.model.NotificationLog;
import com.lexsecura.domain.repository.CraEventRepository;
import com.lexsecura.domain.repository.NotificationLogRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class EscalationService {

    private static final Logger log = LoggerFactory.getLogger(EscalationService.class);

    private final CraEventRepository eventRepository;
    private final SlaService slaService;
    private final NotificationLogRepository notificationLogRepository;
    private final WebhookService webhookService;
    private final EmailPort emailPort;
    private final Counter escalationCounter;

    @Value("${app.cra.escalation.alert-email:}")
    private String escalationAlertEmail;

    public EscalationService(CraEventRepository eventRepository,
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
        this.escalationCounter = Counter.builder("cra.escalations.total")
                .description("Total escalation actions taken")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${app.cra.escalation-check-ms:300000}")
    @Transactional
    public void checkAndEscalate() {
        List<CraEvent> openEvents = new java.util.ArrayList<>(eventRepository.findAllByStatus("DRAFT"));
        openEvents.addAll(eventRepository.findAllByStatus("IN_REVIEW"));

        for (CraEvent event : openEvents) {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("Escalation check failed for event {}: {}", event.getId(), e.getMessage());
            }
        }
    }

    private void processEvent(CraEvent event) {
        SlaResponse sla = slaService.computeSla(event);

        // Check early warning deadline (24h)
        checkAndAlert(event, "EARLY_WARNING", sla.earlyWarning());

        // Check notification deadline (72h)
        checkAndAlert(event, "NOTIFICATION", sla.notification());

        // Check final report deadline
        if (sla.finalReport() != null) {
            checkAndAlert(event, "FINAL_REPORT", sla.finalReport());
        }
    }

    private void checkAndAlert(CraEvent event, String deadlineType, SlaResponse.SlaDeadline deadline) {
        if (deadline == null) return;

        long remainingSecs = deadline.remainingSeconds();
        String alertLevel;

        if (deadline.overdue()) {
            alertLevel = "OVERDUE";
            escalateEvent(event, deadlineType);
        } else if (remainingSecs <= 2 * 3600) {
            alertLevel = "CRITICAL";
        } else if (remainingSecs <= 6 * 3600) {
            alertLevel = "WARNING";
        } else {
            return; // No alert needed
        }

        // Only send if we haven't already alerted for this combo in the last hour
        boolean alreadySent = notificationLogRepository
                .existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
                        event.getId(), deadlineType, alertLevel,
                        Instant.now().minus(1, ChronoUnit.HOURS));
        if (alreadySent) return;

        String subject = buildSubject(alertLevel, deadlineType, event);

        // Send email alert
        if (escalationAlertEmail != null && !escalationAlertEmail.isBlank()) {
            String htmlBody = buildAlertEmail(alertLevel, deadlineType, event, deadline);
            emailPort.send(escalationAlertEmail, subject, htmlBody);
        }

        // Dispatch webhook
        webhookService.dispatch(event.getOrgId(), "CRA_DEADLINE_ALERT", Map.of(
                "eventId", event.getId().toString(),
                "eventTitle", event.getTitle(),
                "eventType", event.getEventType(),
                "deadlineType", deadlineType,
                "alertLevel", alertLevel,
                "remainingSeconds", remainingSecs,
                "overdue", deadline.overdue(),
                "dueAt", deadline.dueAt().toString()
        ));

        // Log notification
        NotificationLog notifLog = new NotificationLog(
                event.getOrgId(), event.getId(), "EMAIL+WEBHOOK",
                escalationAlertEmail != null ? escalationAlertEmail : "webhook-only",
                subject, deadlineType, alertLevel);
        notificationLogRepository.save(notifLog);

        log.warn("[CRA ESCALATION] {} alert for {} deadline on event '{}' (id={})",
                alertLevel, deadlineType, event.getTitle(), event.getId());
    }

    private void escalateEvent(CraEvent event, String deadlineType) {
        String currentLevel = event.getEscalationLevel();
        String newLevel;

        if ("NONE".equals(currentLevel)) {
            newLevel = "WARNING";
        } else if ("WARNING".equals(currentLevel)) {
            newLevel = "CRITICAL";
        } else {
            newLevel = "CRITICAL"; // already at max
        }

        if (!newLevel.equals(currentLevel)) {
            event.setEscalationLevel(newLevel);
            event.setEscalatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
            eventRepository.save(event);
            escalationCounter.increment();

            log.warn("[CRA ESCALATION] Event {} escalated to {} (overdue {})",
                    event.getId(), newLevel, deadlineType);
        }
    }

    private String buildSubject(String alertLevel, String deadlineType, CraEvent event) {
        String prefix = switch (alertLevel) {
            case "OVERDUE" -> "[URGENT] OVERDUE";
            case "CRITICAL" -> "[CRITICAL] J-2h";
            case "WARNING" -> "[WARNING] J-6h";
            default -> "[INFO]";
        };
        return prefix + " - " + deadlineType + " deadline - " + event.getTitle();
    }

    private String buildAlertEmail(String alertLevel, String deadlineType,
                                   CraEvent event, SlaResponse.SlaDeadline deadline) {
        String color = switch (alertLevel) {
            case "OVERDUE" -> "#dc2626";
            case "CRITICAL" -> "#ea580c";
            case "WARNING" -> "#ca8a04";
            default -> "#2563eb";
        };

        String timeInfo = deadline.overdue()
                ? "OVERDUE by " + formatDuration(Math.abs(deadline.remainingSeconds()))
                : formatDuration(deadline.remainingSeconds()) + " remaining";

        return """
                <html><body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background: %s; color: white; padding: 16px; border-radius: 8px 8px 0 0;">
                  <h2 style="margin: 0;">CRA Deadline Alert - %s</h2>
                  <p style="margin: 4px 0 0;">%s deadline: %s</p>
                </div>
                <div style="border: 1px solid #e5e7eb; border-top: 0; padding: 24px; border-radius: 0 0 8px 8px;">
                  <table style="width: 100%%; border-collapse: collapse;">
                    <tr><td style="padding: 8px; font-weight: bold;">Event:</td><td style="padding: 8px;">%s</td></tr>
                    <tr><td style="padding: 8px; font-weight: bold;">Type:</td><td style="padding: 8px;">%s</td></tr>
                    <tr><td style="padding: 8px; font-weight: bold;">Deadline:</td><td style="padding: 8px;">%s</td></tr>
                    <tr><td style="padding: 8px; font-weight: bold;">Due at:</td><td style="padding: 8px;">%s</td></tr>
                  </table>
                  <p style="margin-top: 16px; padding: 12px; background: #fef3c7; border-radius: 4px;">
                    <strong>Action Required:</strong> Per Article 14 of the Cyber Resilience Act (EU 2024/2847),
                    you must submit the %s to ENISA within the deadline. Failure to comply may result in penalties
                    up to 15M EUR or 2.5%% of annual global turnover.
                  </p>
                </div>
                </body></html>
                """.formatted(color, alertLevel, deadlineType, timeInfo,
                event.getTitle(), event.getEventType(), deadlineType, deadline.dueAt(),
                deadlineType.toLowerCase().replace("_", " "));
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 24) return (hours / 24) + "d " + (hours % 24) + "h";
        return hours + "h " + minutes + "m";
    }
}
