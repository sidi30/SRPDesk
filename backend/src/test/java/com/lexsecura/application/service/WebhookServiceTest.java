package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.WebhookCreateRequest;
import com.lexsecura.application.dto.WebhookResponse;
import com.lexsecura.domain.model.Webhook;
import com.lexsecura.domain.repository.WebhookDeliveryRepository;
import com.lexsecura.domain.repository.WebhookRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock private WebhookRepository webhookRepository;
    @Mock private WebhookDeliveryRepository deliveryRepository;

    private WebhookService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new WebhookService(webhookRepository, deliveryRepository, new ObjectMapper());
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldSaveWebhook() {
        when(webhookRepository.save(any(Webhook.class))).thenAnswer(inv -> {
            Webhook w = inv.getArgument(0);
            w.setId(UUID.randomUUID());
            return w;
        });

        WebhookCreateRequest request = new WebhookCreateRequest(
                "Slack alerts", "https://hooks.slack.com/test", null, "*", "SLACK");

        WebhookResponse response = service.create(request);

        assertNotNull(response.id());
        assertEquals("Slack alerts", response.name());
        assertEquals("SLACK", response.channelType());
        assertEquals("*", response.eventTypes());

        ArgumentCaptor<Webhook> captor = ArgumentCaptor.forClass(Webhook.class);
        verify(webhookRepository).save(captor.capture());
        assertEquals(orgId, captor.getValue().getOrgId());
        assertEquals(userId, captor.getValue().getCreatedBy());
    }

    @Test
    void list_shouldReturnWebhooksForOrg() {
        Webhook w1 = new Webhook(orgId, "Slack", "https://hooks.slack.com", "SLACK", userId);
        w1.setId(UUID.randomUUID());
        Webhook w2 = new Webhook(orgId, "Teams", "https://outlook.office.com", "TEAMS", userId);
        w2.setId(UUID.randomUUID());
        when(webhookRepository.findAllByOrgId(orgId)).thenReturn(List.of(w1, w2));

        List<WebhookResponse> result = service.list();

        assertEquals(2, result.size());
        assertEquals("Slack", result.get(0).name());
        assertEquals("Teams", result.get(1).name());
    }

    @Test
    void delete_existing_shouldCallRepository() {
        UUID id = UUID.randomUUID();
        Webhook w = new Webhook(orgId, "Test", "https://test.com", "GENERIC", userId);
        w.setId(id);
        when(webhookRepository.findByIdAndOrgId(id, orgId)).thenReturn(Optional.of(w));

        service.delete(id);

        verify(webhookRepository).deleteById(id);
    }

    @Test
    void delete_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(webhookRepository.findByIdAndOrgId(id, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.delete(id));
    }

    @Test
    void toggleEnabled_shouldUpdateFlag() {
        UUID id = UUID.randomUUID();
        Webhook w = new Webhook(orgId, "Test", "https://test.com", "GENERIC", userId);
        w.setId(id);
        w.setEnabled(true);
        when(webhookRepository.findByIdAndOrgId(id, orgId)).thenReturn(Optional.of(w));
        when(webhookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.toggleEnabled(id, false);

        assertFalse(w.isEnabled());
        verify(webhookRepository).save(w);
    }
}
