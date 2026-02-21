package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.domain.model.ProductRepoMapping;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.repository.ProcessedWebhookEventRepository;
import com.lexsecura.domain.repository.ProductRepoMappingRepository;
import com.lexsecura.domain.repository.ReleaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitLabWebhookServiceTest {

    @Mock
    private ProductRepoMappingRepository mappingRepository;

    @Mock
    private ProcessedWebhookEventRepository processedEventRepository;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private AuditService auditService;

    private GitLabWebhookService service;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        service = new GitLabWebhookService(
                mappingRepository,
                processedEventRepository,
                releaseRepository,
                auditService,
                objectMapper,
                "test-secret");
    }

    @Test
    void verifySignature_validToken_shouldReturnTrue() {
        assertTrue(service.verifySignature("test-secret"));
    }

    @Test
    void verifySignature_invalidToken_shouldReturnFalse() {
        assertFalse(service.verifySignature("wrong"));
    }

    @Test
    void verifySignature_nullToken_shouldReturnFalse() {
        assertFalse(service.verifySignature(null));
    }

    @Test
    void handleTagPush_withMapping_shouldCreateRelease() {
        UUID productId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        ProductRepoMapping mapping = new ProductRepoMapping();
        mapping.setId(UUID.randomUUID());
        mapping.setOrgId(orgId);
        mapping.setProductId(productId);
        mapping.setForge("GITLAB");
        mapping.setProjectId(123L);

        when(processedEventRepository.existsByForgeAndEventId(eq("GITLAB"), anyString()))
                .thenReturn(false);
        when(mappingRepository.findByForgeAndProjectId("GITLAB", 123L))
                .thenReturn(Optional.of(mapping));
        when(releaseRepository.save(any(Release.class))).thenAnswer(inv -> {
            Release r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        String body = "{\"project_id\": 123, \"ref\": \"refs/tags/v1.0.0\", \"checkout_sha\": \"abc123\"}";
        service.handleWebhook("tag_push", body);

        ArgumentCaptor<Release> captor = ArgumentCaptor.forClass(Release.class);
        verify(releaseRepository).save(captor.capture());
        Release saved = captor.getValue();

        assertEquals(productId, saved.getProductId());
        assertEquals("v1.0.0", saved.getVersion());
        assertEquals("abc123", saved.getGitRef());

        verify(processedEventRepository).save(eq("GITLAB"), eq("tag_push:123:v1.0.0"), eq("tag_push"));
        verify(auditService).record(eq(orgId), eq("RELEASE"), any(UUID.class),
                eq("CREATE_VIA_WEBHOOK"), any(UUID.class), anyMap());
    }

    @Test
    void handleTagPush_alreadyProcessed_shouldSkip() {
        when(processedEventRepository.existsByForgeAndEventId(eq("GITLAB"), eq("tag_push:123:v1.0.0")))
                .thenReturn(true);

        String body = "{\"project_id\": 123, \"ref\": \"refs/tags/v1.0.0\", \"checkout_sha\": \"abc123\"}";
        service.handleWebhook("tag_push", body);

        verify(releaseRepository, never()).save(any());
        verify(auditService, never()).record(any(), any(), any(), any(), any(), anyMap());
    }

    @Test
    void handleTagPush_noMapping_shouldSkip() {
        when(processedEventRepository.existsByForgeAndEventId(eq("GITLAB"), anyString()))
                .thenReturn(false);
        when(mappingRepository.findByForgeAndProjectId("GITLAB", 123L))
                .thenReturn(Optional.empty());

        String body = "{\"project_id\": 123, \"ref\": \"refs/tags/v1.0.0\", \"checkout_sha\": \"abc123\"}";
        service.handleWebhook("tag_push", body);

        verify(releaseRepository, never()).save(any());
        verify(processedEventRepository).save(eq("GITLAB"), eq("tag_push:123:v1.0.0"), eq("tag_push"));
        verify(auditService, never()).record(any(), any(), any(), any(), any(), anyMap());
    }

    @Test
    void handleRelease_createAction_shouldCreateRelease() {
        UUID productId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        ProductRepoMapping mapping = new ProductRepoMapping();
        mapping.setId(UUID.randomUUID());
        mapping.setOrgId(orgId);
        mapping.setProductId(productId);
        mapping.setForge("GITLAB");
        mapping.setProjectId(123L);

        when(processedEventRepository.existsByForgeAndEventId(eq("GITLAB"), anyString()))
                .thenReturn(false);
        when(mappingRepository.findByForgeAndProjectId("GITLAB", 123L))
                .thenReturn(Optional.of(mapping));
        when(releaseRepository.save(any(Release.class))).thenAnswer(inv -> {
            Release r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        String body = "{\"project\": {\"id\": 123}, \"tag\": \"v2.0.0\", \"action\": \"create\", \"commit\": {\"id\": \"def456\"}}";
        service.handleWebhook("release", body);

        ArgumentCaptor<Release> captor = ArgumentCaptor.forClass(Release.class);
        verify(releaseRepository).save(captor.capture());
        Release saved = captor.getValue();

        assertEquals(productId, saved.getProductId());
        assertEquals("v2.0.0", saved.getVersion());
        assertEquals("def456", saved.getGitRef());

        verify(processedEventRepository).save(eq("GITLAB"), eq("release:123:v2.0.0"), eq("release"));
        verify(auditService).record(eq(orgId), eq("RELEASE"), any(UUID.class),
                eq("CREATE_VIA_WEBHOOK"), any(UUID.class), anyMap());
    }
}
