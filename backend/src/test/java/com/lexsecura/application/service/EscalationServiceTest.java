package com.lexsecura.application.service;

import com.lexsecura.application.dto.SlaResponse;
import com.lexsecura.application.port.EmailPort;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.repository.CraEventRepository;
import com.lexsecura.domain.repository.NotificationLogRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EscalationServiceTest {

    @Mock private CraEventRepository eventRepository;
    @Mock private SlaService slaService;
    @Mock private NotificationLogRepository notificationLogRepository;
    @Mock private WebhookService webhookService;
    @Mock private EmailPort emailPort;

    private EscalationService service;

    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new EscalationService(eventRepository, slaService, notificationLogRepository,
                webhookService, emailPort, new SimpleMeterRegistry());
    }

    @Test
    void checkAndEscalate_overdueEvent_shouldEscalateToWarning() {
        CraEvent event = buildEvent("NONE");
        when(eventRepository.findAllByStatus("DRAFT")).thenReturn(List.of(event));
        when(eventRepository.findAllByStatus("IN_REVIEW")).thenReturn(List.of());
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant earlyWarningDue = Instant.now().minus(Duration.ofHours(2));
        Instant notificationDue = Instant.now().plus(Duration.ofHours(46));
        SlaResponse sla = new SlaResponse(
                new SlaResponse.SlaDeadline(earlyWarningDue, -7200, true),
                new SlaResponse.SlaDeadline(notificationDue, 165600, false),
                null
        );
        when(slaService.computeSla(event)).thenReturn(sla);
        when(notificationLogRepository.existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
                any(), any(), any(), any())).thenReturn(false);

        service.checkAndEscalate();

        assertEquals("WARNING", event.getEscalationLevel());
        assertNotNull(event.getEscalatedAt());
        verify(eventRepository).save(event);
        verify(webhookService).dispatch(eq(orgId), eq("CRA_DEADLINE_ALERT"), anyMap());
        verify(notificationLogRepository).save(any());
    }

    @Test
    void checkAndEscalate_warningToCompletelyCritical() {
        CraEvent event = buildEvent("WARNING");
        when(eventRepository.findAllByStatus("DRAFT")).thenReturn(List.of(event));
        when(eventRepository.findAllByStatus("IN_REVIEW")).thenReturn(List.of());
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Instant earlyWarningDue = Instant.now().minus(Duration.ofHours(10));
        Instant notificationDue = Instant.now().minus(Duration.ofHours(1));
        SlaResponse sla = new SlaResponse(
                new SlaResponse.SlaDeadline(earlyWarningDue, -36000, true),
                new SlaResponse.SlaDeadline(notificationDue, -3600, true),
                null
        );
        when(slaService.computeSla(event)).thenReturn(sla);
        when(notificationLogRepository.existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
                any(), any(), any(), any())).thenReturn(false);

        service.checkAndEscalate();

        assertEquals("CRITICAL", event.getEscalationLevel());
    }

    @Test
    void checkAndEscalate_noOverdue_shouldNotEscalate() {
        CraEvent event = buildEvent("NONE");
        when(eventRepository.findAllByStatus("DRAFT")).thenReturn(List.of(event));
        when(eventRepository.findAllByStatus("IN_REVIEW")).thenReturn(List.of());

        Instant earlyWarningDue = Instant.now().plus(Duration.ofHours(20));
        Instant notificationDue = Instant.now().plus(Duration.ofHours(68));
        SlaResponse sla = new SlaResponse(
                new SlaResponse.SlaDeadline(earlyWarningDue, 72000, false),
                new SlaResponse.SlaDeadline(notificationDue, 244800, false),
                null
        );
        when(slaService.computeSla(event)).thenReturn(sla);

        service.checkAndEscalate();

        assertEquals("NONE", event.getEscalationLevel());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void checkAndEscalate_alreadySentAlert_shouldSkip() {
        CraEvent event = buildEvent("NONE");
        when(eventRepository.findAllByStatus("DRAFT")).thenReturn(List.of(event));
        when(eventRepository.findAllByStatus("IN_REVIEW")).thenReturn(List.of());

        Instant earlyWarningDue = Instant.now().plus(Duration.ofHours(1));
        SlaResponse sla = new SlaResponse(
                new SlaResponse.SlaDeadline(earlyWarningDue, 3600, false),
                new SlaResponse.SlaDeadline(Instant.now().plus(Duration.ofHours(49)), 176400, false),
                null
        );
        when(slaService.computeSla(event)).thenReturn(sla);
        when(notificationLogRepository.existsByCraEventIdAndDeadlineTypeAndAlertLevelAndSentAtAfter(
                any(), eq("EARLY_WARNING"), eq("CRITICAL"), any())).thenReturn(true);

        service.checkAndEscalate();

        verify(notificationLogRepository, never()).save(any());
    }

    private CraEvent buildEvent(String escalationLevel) {
        CraEvent event = new CraEvent();
        event.setId(UUID.randomUUID());
        event.setOrgId(orgId);
        event.setProductId(UUID.randomUUID());
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setTitle("Test event");
        event.setStatus("DRAFT");
        event.setDetectedAt(Instant.now().minus(Duration.ofHours(26)));
        event.setEscalationLevel(escalationLevel);
        return event;
    }
}
