package com.lexsecura.application.service;

import com.lexsecura.application.dto.SlaResponse;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.repository.CraEventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CraNotificationService {

    private static final Logger log = LoggerFactory.getLogger(CraNotificationService.class);

    private final CraEventRepository eventRepository;
    private final SlaService slaService;
    private final Counter overdueAlertCounter;
    private final AtomicInteger openEventsGauge = new AtomicInteger(0);
    private final AtomicInteger overdueEventsGauge = new AtomicInteger(0);

    public CraNotificationService(CraEventRepository eventRepository,
                                  SlaService slaService,
                                  MeterRegistry meterRegistry) {
        this.eventRepository = eventRepository;
        this.slaService = slaService;
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

    /**
     * Check deadlines every 15 minutes.
     * Alerts at J-6h, J-2h, and when overdue.
     */
    @Scheduled(fixedDelayString = "${app.cra.notification-check-ms:900000}")
    public void checkDeadlines() {
        List<CraEvent> openEvents = eventRepository.findAllByStatus("DRAFT");
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

        if (deadline.overdue()) {
            log.warn("[CRA ALERT] OVERDUE: {} deadline for event '{}' (id={}, org={})",
                    deadlineName, event.getTitle(), event.getId(), event.getOrgId());
            overdueAlertCounter.increment();
            return 1;
        }

        long sixHours = 6 * 3600;
        long twoHours = 2 * 3600;

        if (remainingSecs <= twoHours) {
            log.warn("[CRA ALERT] J-2h: {} deadline approaching for event '{}' (id={}, org={})",
                    deadlineName, event.getTitle(), event.getId(), event.getOrgId());
            overdueAlertCounter.increment();
        } else if (remainingSecs <= sixHours) {
            log.info("[CRA ALERT] J-6h: {} deadline approaching for event '{}' (id={}, org={})",
                    deadlineName, event.getTitle(), event.getId(), event.getOrgId());
        }

        return 0;
    }
}
