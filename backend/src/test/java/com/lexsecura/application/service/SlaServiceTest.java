package com.lexsecura.application.service;

import com.lexsecura.application.dto.SlaResponse;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.model.OrgSlaSettings;
import com.lexsecura.domain.repository.OrgSlaSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaServiceTest {

    @Mock
    private OrgSlaSettingsRepository settingsRepository;

    private SlaService slaService;

    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        slaService = new SlaService(settingsRepository);
    }

    @Test
    void computeSla_defaultSettings_earlyWarning24h() {
        when(settingsRepository.findByOrgId(orgId)).thenReturn(Optional.empty());

        CraEvent event = new CraEvent();
        event.setOrgId(orgId);
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setDetectedAt(Instant.now().minus(Duration.ofHours(12)));

        SlaResponse sla = slaService.computeSla(event);

        assertNotNull(sla.earlyWarning());
        assertFalse(sla.earlyWarning().overdue());
        assertTrue(sla.earlyWarning().remainingSeconds() > 0);
        assertTrue(sla.earlyWarning().remainingSeconds() <= 12 * 3600 + 10);
    }

    @Test
    void computeSla_overdueEarlyWarning() {
        when(settingsRepository.findByOrgId(orgId)).thenReturn(Optional.empty());

        CraEvent event = new CraEvent();
        event.setOrgId(orgId);
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setDetectedAt(Instant.now().minus(Duration.ofHours(30)));

        SlaResponse sla = slaService.computeSla(event);

        assertTrue(sla.earlyWarning().overdue());
        assertTrue(sla.earlyWarning().remainingSeconds() < 0);
    }

    @Test
    void computeSla_customOrgSettings() {
        OrgSlaSettings custom = new OrgSlaSettings();
        custom.setOrgId(orgId);
        custom.setEarlyWarningHours(12);
        custom.setNotificationHours(48);
        custom.setFinalReportDaysAfterPatch(7);
        custom.setFinalReportDaysAfterResolve(14);
        when(settingsRepository.findByOrgId(orgId)).thenReturn(Optional.of(custom));

        CraEvent event = new CraEvent();
        event.setOrgId(orgId);
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setDetectedAt(Instant.now().minus(Duration.ofHours(10)));

        SlaResponse sla = slaService.computeSla(event);

        // With 12h early warning and 10h elapsed, ~2h remaining
        assertFalse(sla.earlyWarning().overdue());
        assertTrue(sla.earlyWarning().remainingSeconds() < 2 * 3600 + 60);
    }

    @Test
    void computeSla_exploitedVuln_noPatch_noFinalReport() {
        when(settingsRepository.findByOrgId(orgId)).thenReturn(Optional.empty());

        CraEvent event = new CraEvent();
        event.setOrgId(orgId);
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setDetectedAt(Instant.now());
        event.setPatchAvailableAt(null);

        SlaResponse sla = slaService.computeSla(event);

        assertNull(sla.finalReport());
    }

    @Test
    void computeSla_exploitedVuln_withPatch_finalReportDue() {
        when(settingsRepository.findByOrgId(orgId)).thenReturn(Optional.empty());

        CraEvent event = new CraEvent();
        event.setOrgId(orgId);
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setDetectedAt(Instant.now().minus(Duration.ofDays(5)));
        event.setPatchAvailableAt(Instant.now().minus(Duration.ofDays(1)));

        SlaResponse sla = slaService.computeSla(event);

        assertNotNull(sla.finalReport());
        assertFalse(sla.finalReport().overdue());
        // Patch was 1 day ago, 14 days deadline, so ~13 days remaining
        assertTrue(sla.finalReport().remainingSeconds() > 12 * 86400);
    }

    @Test
    void computeSla_severeIncident_noResolved_noFinalReport() {
        when(settingsRepository.findByOrgId(orgId)).thenReturn(Optional.empty());

        CraEvent event = new CraEvent();
        event.setOrgId(orgId);
        event.setEventType("SEVERE_INCIDENT");
        event.setDetectedAt(Instant.now());
        event.setResolvedAt(null);

        SlaResponse sla = slaService.computeSla(event);

        assertNull(sla.finalReport());
    }

    @Test
    void computeSla_severeIncident_resolved_finalReportDue() {
        when(settingsRepository.findByOrgId(orgId)).thenReturn(Optional.empty());

        CraEvent event = new CraEvent();
        event.setOrgId(orgId);
        event.setEventType("SEVERE_INCIDENT");
        event.setDetectedAt(Instant.now().minus(Duration.ofDays(10)));
        event.setResolvedAt(Instant.now().minus(Duration.ofDays(2)));

        SlaResponse sla = slaService.computeSla(event);

        assertNotNull(sla.finalReport());
        assertFalse(sla.finalReport().overdue());
        // Resolved 2 days ago, 30 days deadline, so ~28 days remaining
        assertTrue(sla.finalReport().remainingSeconds() > 27 * 86400);
    }
}
