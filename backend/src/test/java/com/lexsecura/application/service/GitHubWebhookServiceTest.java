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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubWebhookServiceTest {

    @Mock private ProductRepoMappingRepository mappingRepository;
    @Mock private ProcessedWebhookEventRepository processedEventRepository;
    @Mock private ReleaseRepository releaseRepository;
    @Mock private AuditService auditService;

    private GitHubWebhookService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new GitHubWebhookService(
                mappingRepository, processedEventRepository, releaseRepository,
                auditService, objectMapper, "test-secret");
    }

    @Test
    void handleEvent_releasePublished_shouldCreateRelease() {
        UUID productId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        ProductRepoMapping mapping = new ProductRepoMapping();
        mapping.setProductId(productId);
        mapping.setOrgId(orgId);

        String payload = """
                {
                  "action": "published",
                  "release": {"tag_name": "v1.2.3", "target_commitish": "abc123"},
                  "repository": {"full_name": "owner/repo"}
                }""";

        when(processedEventRepository.existsByForgeAndEventId("GITHUB", "release:owner/repo:v1.2.3"))
                .thenReturn(false);
        when(mappingRepository.findByForgeAndRepoFullName("GITHUB", "owner/repo"))
                .thenReturn(Optional.of(mapping));
        when(releaseRepository.save(any(Release.class))).thenAnswer(inv -> {
            Release r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        Map<String, String> result = service.handleEvent("release", payload);

        assertEquals("created", result.get("status"));
        assertEquals("1.2.3", result.get("version")); // v stripped
        verify(releaseRepository).save(argThat(r -> "1.2.3".equals(r.getVersion())));
        verify(processedEventRepository).save("GITHUB", "release:owner/repo:v1.2.3", "release");
        verify(auditService).record(eq(orgId), eq("RELEASE"), any(), eq("CREATE_VIA_WEBHOOK"), any(), any());
    }

    @Test
    void handleEvent_releaseDeleted_shouldIgnore() {
        String payload = """
                {
                  "action": "deleted",
                  "release": {"tag_name": "v1.0.0"},
                  "repository": {"full_name": "owner/repo"}
                }""";

        Map<String, String> result = service.handleEvent("release", payload);

        assertEquals("ignored", result.get("status"));
        verify(releaseRepository, never()).save(any());
    }

    @Test
    void handleEvent_releaseAlreadyProcessed_shouldSkip() {
        String payload = """
                {
                  "action": "published",
                  "release": {"tag_name": "v1.0.0", "target_commitish": "sha"},
                  "repository": {"full_name": "owner/repo"}
                }""";

        when(processedEventRepository.existsByForgeAndEventId("GITHUB", "release:owner/repo:v1.0.0"))
                .thenReturn(true);

        Map<String, String> result = service.handleEvent("release", payload);

        assertEquals("skipped", result.get("status"));
        verify(releaseRepository, never()).save(any());
    }

    @Test
    void handleEvent_noMapping_shouldSkip() {
        String payload = """
                {
                  "action": "published",
                  "release": {"tag_name": "v1.0.0", "target_commitish": "sha"},
                  "repository": {"full_name": "unknown/repo"}
                }""";

        when(processedEventRepository.existsByForgeAndEventId("GITHUB", "release:unknown/repo:v1.0.0"))
                .thenReturn(false);
        when(mappingRepository.findByForgeAndRepoFullName("GITHUB", "unknown/repo"))
                .thenReturn(Optional.empty());

        Map<String, String> result = service.handleEvent("release", payload);

        assertEquals("skipped", result.get("status"));
        verify(releaseRepository, never()).save(any());
    }

    @Test
    void handleEvent_ping_shouldPong() {
        Map<String, String> result = service.handleEvent("ping", "{}");
        assertEquals("pong", result.get("status"));
    }

    @Test
    void verifySignature_validSignature_shouldReturnTrue() {
        // Pre-computed HMAC-SHA256 of "test" with key "test-secret"
        String payload = "test";
        javax.crypto.Mac mac;
        try {
            mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(
                    "test-secret".getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String signature = "sha256=" + java.util.HexFormat.of().formatHex(hash);

            assertTrue(service.verifySignature(payload, signature));
        } catch (Exception e) {
            fail("HMAC computation failed: " + e.getMessage());
        }
    }

    @Test
    void verifySignature_invalidSignature_shouldReturnFalse() {
        assertFalse(service.verifySignature("payload", "sha256=invalid"));
    }
}
