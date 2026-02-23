package com.lexsecura.application.service;

import com.lexsecura.application.dto.SecurityAdvisoryCreateRequest;
import com.lexsecura.application.dto.SecurityAdvisoryResponse;
import com.lexsecura.application.port.EmailPort;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.model.SecurityAdvisory;
import com.lexsecura.domain.repository.CraEventRepository;
import com.lexsecura.domain.repository.SecurityAdvisoryRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityAdvisoryServiceTest {

    @Mock private SecurityAdvisoryRepository advisoryRepository;
    @Mock private CraEventRepository eventRepository;
    @Mock private AuditService auditService;
    @Mock private WebhookService webhookService;
    @Mock private EmailPort emailPort;

    private SecurityAdvisoryService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SecurityAdvisoryService(advisoryRepository, eventRepository,
                auditService, webhookService, emailPort);
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    @Test
    void create_shouldSaveAdvisory() {
        CraEvent event = buildEvent();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(advisoryRepository.save(any(SecurityAdvisory.class))).thenAnswer(inv -> {
            SecurityAdvisory a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        SecurityAdvisoryCreateRequest request = new SecurityAdvisoryCreateRequest(
                eventId, "CVE-2024-1234", "CRITICAL", "< 2.0", "Critical vuln", "Update to 2.0");

        SecurityAdvisoryResponse response = service.create(request);

        assertNotNull(response.id());
        assertEquals("CVE-2024-1234", response.title());
        assertEquals("CRITICAL", response.severity());
        assertEquals("DRAFT", response.status());
        verify(auditService).record(eq(orgId), eq("SECURITY_ADVISORY"), any(), eq("CREATE"), eq(userId), anyMap());
    }

    @Test
    void create_eventNotFound_shouldThrow() {
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.empty());

        SecurityAdvisoryCreateRequest request = new SecurityAdvisoryCreateRequest(
                eventId, "Title", "HIGH", null, "Desc", null);

        assertThrows(EntityNotFoundException.class, () -> service.create(request));
    }

    @Test
    void publish_shouldSetPublished() {
        SecurityAdvisory advisory = buildAdvisory("DRAFT");
        when(advisoryRepository.findByIdAndOrgId(advisory.getId(), orgId)).thenReturn(Optional.of(advisory));
        when(advisoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SecurityAdvisoryResponse response = service.publish(advisory.getId());

        assertEquals("PUBLISHED", response.status());
        assertNotNull(response.publishedAt());
        verify(webhookService).dispatch(eq(orgId), eq("ADVISORY_PUBLISHED"), anyMap());
    }

    @Test
    void publish_alreadyPublished_shouldThrow() {
        SecurityAdvisory advisory = buildAdvisory("PUBLISHED");
        when(advisoryRepository.findByIdAndOrgId(advisory.getId(), orgId)).thenReturn(Optional.of(advisory));

        assertThrows(IllegalStateException.class, () -> service.publish(advisory.getId()));
    }

    @Test
    void notifyUsers_shouldSendEmailsAndUpdateStatus() {
        SecurityAdvisory advisory = buildAdvisory("PUBLISHED");
        when(advisoryRepository.findByIdAndOrgId(advisory.getId(), orgId)).thenReturn(Optional.of(advisory));
        when(advisoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SecurityAdvisoryResponse response = service.notifyUsers(advisory.getId(),
                List.of("user1@test.com", "user2@test.com"));

        assertEquals("NOTIFIED", response.status());
        assertNotNull(response.notifiedAt());
        verify(emailPort, times(2)).send(anyString(), anyString(), anyString());
        verify(auditService).record(eq(orgId), eq("SECURITY_ADVISORY"), any(), eq("NOTIFY_USERS"), eq(userId), anyMap());
    }

    @Test
    void notifyUsers_notPublished_shouldThrow() {
        SecurityAdvisory advisory = buildAdvisory("DRAFT");
        when(advisoryRepository.findByIdAndOrgId(advisory.getId(), orgId)).thenReturn(Optional.of(advisory));

        assertThrows(IllegalStateException.class,
                () -> service.notifyUsers(advisory.getId(), List.of("test@test.com")));
    }

    private CraEvent buildEvent() {
        CraEvent event = new CraEvent();
        event.setId(eventId);
        event.setOrgId(orgId);
        event.setProductId(productId);
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setTitle("Test event");
        event.setDetectedAt(Instant.now());
        return event;
    }

    private SecurityAdvisory buildAdvisory(String status) {
        SecurityAdvisory a = new SecurityAdvisory(orgId, eventId, productId,
                "Test Advisory", "HIGH", "Description", userId);
        a.setId(UUID.randomUUID());
        a.setStatus(status);
        if ("PUBLISHED".equals(status)) a.setPublishedAt(Instant.now());
        return a;
    }
}
