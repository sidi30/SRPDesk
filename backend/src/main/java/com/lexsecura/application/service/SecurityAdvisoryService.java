package com.lexsecura.application.service;

import com.lexsecura.application.dto.SecurityAdvisoryCreateRequest;
import com.lexsecura.application.dto.SecurityAdvisoryResponse;
import com.lexsecura.application.port.EmailPort;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.model.SecurityAdvisory;
import com.lexsecura.domain.repository.CraEventRepository;
import com.lexsecura.domain.repository.SecurityAdvisoryRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SecurityAdvisoryService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAdvisoryService.class);

    private final SecurityAdvisoryRepository advisoryRepository;
    private final CraEventRepository eventRepository;
    private final AuditService auditService;
    private final WebhookService webhookService;
    private final EmailPort emailPort;

    public SecurityAdvisoryService(SecurityAdvisoryRepository advisoryRepository,
                                   CraEventRepository eventRepository,
                                   AuditService auditService,
                                   WebhookService webhookService,
                                   EmailPort emailPort) {
        this.advisoryRepository = advisoryRepository;
        this.eventRepository = eventRepository;
        this.auditService = auditService;
        this.webhookService = webhookService;
        this.emailPort = emailPort;
    }

    public SecurityAdvisoryResponse create(SecurityAdvisoryCreateRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        CraEvent event = eventRepository.findByIdAndOrgId(request.craEventId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + request.craEventId()));

        SecurityAdvisory advisory = new SecurityAdvisory(
                orgId, event.getId(), event.getProductId(),
                request.title(), request.severity(), request.description(), userId);
        advisory.setAffectedVersions(request.affectedVersions());
        advisory.setRemediation(request.remediation());
        advisory = advisoryRepository.save(advisory);

        auditService.record(orgId, "SECURITY_ADVISORY", advisory.getId(), "CREATE", userId,
                Map.of("craEventId", event.getId().toString(), "title", request.title()));

        log.info("Security advisory created: id={}, event={}", advisory.getId(), event.getId());
        return toResponse(advisory);
    }

    @Transactional(readOnly = true)
    public List<SecurityAdvisoryResponse> list() {
        UUID orgId = TenantContext.getOrgId();
        return advisoryRepository.findAllByOrgId(orgId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SecurityAdvisoryResponse findById(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        SecurityAdvisory advisory = advisoryRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Advisory not found: " + id));
        return toResponse(advisory);
    }

    public SecurityAdvisoryResponse publish(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        SecurityAdvisory advisory = advisoryRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Advisory not found: " + id));

        if ("PUBLISHED".equals(advisory.getStatus())) {
            throw new IllegalStateException("Advisory already published");
        }

        advisory.setStatus("PUBLISHED");
        advisory.setPublishedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        advisory = advisoryRepository.save(advisory);

        // Dispatch webhook event for published advisory
        webhookService.dispatch(orgId, "ADVISORY_PUBLISHED", Map.of(
                "advisoryId", advisory.getId().toString(),
                "title", advisory.getTitle(),
                "severity", advisory.getSeverity(),
                "productId", advisory.getProductId().toString()
        ));

        auditService.record(orgId, "SECURITY_ADVISORY", advisory.getId(), "PUBLISH", userId, Map.of());

        log.info("Security advisory published: id={}", advisory.getId());
        return toResponse(advisory);
    }

    public SecurityAdvisoryResponse notifyUsers(UUID id, List<String> recipients) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        SecurityAdvisory advisory = advisoryRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Advisory not found: " + id));

        if (!"PUBLISHED".equals(advisory.getStatus())) {
            throw new IllegalStateException("Advisory must be published before notifying users");
        }

        String subject = "[CRA Security Advisory] " + advisory.getTitle();
        String htmlBody = buildAdvisoryEmail(advisory);

        int sent = 0;
        for (String recipient : recipients) {
            try {
                emailPort.send(recipient, subject, htmlBody);
                sent++;
            } catch (Exception e) {
                log.error("Failed to notify {}: {}", recipient, e.getMessage());
            }
        }

        advisory.setNotifiedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        advisory.setStatus("NOTIFIED");
        advisory = advisoryRepository.save(advisory);

        auditService.record(orgId, "SECURITY_ADVISORY", advisory.getId(), "NOTIFY_USERS", userId,
                Map.of("recipients", sent, "total", recipients.size()));

        log.info("Advisory {} notified to {}/{} users", advisory.getId(), sent, recipients.size());
        return toResponse(advisory);
    }

    private String buildAdvisoryEmail(SecurityAdvisory advisory) {
        return """
                <html><body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background: #dc2626; color: white; padding: 16px; border-radius: 8px 8px 0 0;">
                  <h2 style="margin: 0;">Security Advisory</h2>
                  <p style="margin: 4px 0 0;">Severity: %s</p>
                </div>
                <div style="border: 1px solid #e5e7eb; border-top: 0; padding: 24px; border-radius: 0 0 8px 8px;">
                  <h3>%s</h3>
                  <p><strong>Affected Versions:</strong> %s</p>
                  <h4>Description</h4>
                  <p>%s</p>
                  %s
                  <hr>
                  <p style="color: #6b7280; font-size: 12px;">
                    This advisory is issued under Article 14.3 of the EU Cyber Resilience Act (2024/2847).
                    Please update your systems as soon as possible.
                  </p>
                </div>
                </body></html>
                """.formatted(
                advisory.getSeverity(),
                advisory.getTitle(),
                advisory.getAffectedVersions() != null ? advisory.getAffectedVersions() : "All versions",
                advisory.getDescription(),
                advisory.getRemediation() != null
                        ? "<h4>Remediation</h4><p>" + advisory.getRemediation() + "</p>"
                        : ""
        );
    }

    private SecurityAdvisoryResponse toResponse(SecurityAdvisory a) {
        return new SecurityAdvisoryResponse(
                a.getId(), a.getCraEventId(), a.getProductId(), a.getTitle(), a.getSeverity(),
                a.getAffectedVersions(), a.getDescription(), a.getRemediation(), a.getAdvisoryUrl(),
                a.getStatus(), a.getPublishedAt(), a.getNotifiedAt(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
