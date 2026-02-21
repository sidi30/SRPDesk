package com.lexsecura.application.service;

import com.lexsecura.application.dto.SlaResponse;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.model.OrgSlaSettings;
import com.lexsecura.domain.repository.OrgSlaSettingsRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class SlaService {

    private final OrgSlaSettingsRepository settingsRepository;

    public SlaService(OrgSlaSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public SlaResponse computeSla(CraEvent event) {
        OrgSlaSettings settings = settingsRepository.findByOrgId(event.getOrgId())
                .orElseGet(OrgSlaSettings::new);

        Instant detectedAt = event.getDetectedAt();
        Instant now = Instant.now();

        Instant earlyWarningDue = detectedAt.plus(Duration.ofHours(settings.getEarlyWarningHours()));
        Instant notificationDue = detectedAt.plus(Duration.ofHours(settings.getNotificationHours()));

        Instant finalReportDue = computeFinalReportDue(event, settings);

        return new SlaResponse(
                toDeadline(earlyWarningDue, now),
                toDeadline(notificationDue, now),
                finalReportDue != null ? toDeadline(finalReportDue, now) : null
        );
    }

    private Instant computeFinalReportDue(CraEvent event, OrgSlaSettings settings) {
        if ("EXPLOITED_VULNERABILITY".equals(event.getEventType())) {
            if (event.getPatchAvailableAt() != null) {
                return event.getPatchAvailableAt().plus(Duration.ofDays(settings.getFinalReportDaysAfterPatch()));
            }
            return null;
        }
        if ("SEVERE_INCIDENT".equals(event.getEventType())) {
            if (event.getResolvedAt() != null) {
                return event.getResolvedAt().plus(Duration.ofDays(settings.getFinalReportDaysAfterResolve()));
            }
            return null;
        }
        return null;
    }

    private SlaResponse.SlaDeadline toDeadline(Instant dueAt, Instant now) {
        long remaining = Duration.between(now, dueAt).getSeconds();
        return new SlaResponse.SlaDeadline(dueAt, remaining, remaining < 0);
    }
}
