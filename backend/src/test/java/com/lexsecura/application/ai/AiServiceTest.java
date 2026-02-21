package com.lexsecura.application.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.ai.AiRedactor;
import com.lexsecura.infrastructure.ai.AiSchemaValidator;
import com.lexsecura.infrastructure.ai.OllamaClient;
import com.lexsecura.infrastructure.security.TenantContext;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock private OllamaClient ollamaClient;
    @Mock private AiJobRepository aiJobRepository;
    @Mock private AiArtifactRepository aiArtifactRepository;
    @Mock private CraEventRepository craEventRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ReleaseRepository releaseRepository;
    @Mock private FindingRepository findingRepository;
    @Mock private FindingDecisionRepository decisionRepository;
    @Mock private VulnerabilityRepository vulnerabilityRepository;
    @Mock private EvidenceRepository evidenceRepository;
    @Mock private ComponentRepository componentRepository;
    @Mock private AuditEventRepository auditEventRepository;

    private AiService aiService;
    private AiRedactor redactor;
    private AiSchemaValidator validator;
    private ObjectMapper objectMapper;

    private final UUID orgId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        redactor = new AiRedactor();
        validator = new AiSchemaValidator(objectMapper);

        aiService = new AiService(
                ollamaClient, redactor, validator,
                aiJobRepository, aiArtifactRepository,
                craEventRepository, productRepository, releaseRepository,
                findingRepository, decisionRepository, vulnerabilityRepository,
                evidenceRepository, componentRepository, auditEventRepository,
                objectMapper, new SimpleMeterRegistry()
        );

        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ── generateSrpDraft ──────────────────────────────────

    @Test
    void generateSrpDraft_validResponse_completesJob() {
        UUID eventId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CraEvent event = buildEvent(eventId, productId);
        Product product = buildProduct(productId);

        when(craEventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findAllByProductId(productId)).thenReturn(List.of());
        when(ollamaClient.getModel()).thenReturn("phi3.5");
        when(aiJobRepository.save(any(AiJob.class))).thenAnswer(inv -> {
            AiJob j = inv.getArgument(0);
            if (j.getId() == null) j.setId(UUID.randomUUID());
            return j;
        });
        when(aiJobRepository.findById(any())).thenAnswer(inv -> {
            AiJob j = new AiJob();
            j.setId(inv.getArgument(0));
            j.setStatus("COMPLETED");
            return Optional.of(j);
        });

        String validSrpJson = """
                {
                  "summary": "Critical vulnerability in IoT Gateway firmware update",
                  "affected_versions": ["1.0.0"],
                  "impact": "Remote code execution possible",
                  "mitigation": "Disable OTA",
                  "patch_status": "In progress",
                  "timeline": [{"date": "2026-02-01", "event": "Detected"}],
                  "references": [{"type": "product", "id": "%s", "label": "IoT Gateway"}]
                }
                """.formatted(productId);
        when(ollamaClient.generate(anyString(), anyString())).thenReturn(validSrpJson);

        AiJob result = aiService.generateSrpDraft(eventId, "EARLY_WARNING");

        assertNotNull(result);
        verify(ollamaClient, atLeastOnce()).generate(anyString(), anyString());
        verify(aiJobRepository, atLeast(2)).save(any(AiJob.class)); // create + complete
        verify(aiArtifactRepository).save(any(AiArtifact.class));
    }

    @Test
    void generateSrpDraft_invalidResponse_usesFallback() {
        UUID eventId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CraEvent event = buildEvent(eventId, productId);
        Product product = buildProduct(productId);

        when(craEventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findAllByProductId(productId)).thenReturn(List.of());
        when(ollamaClient.getModel()).thenReturn("phi3.5");
        when(aiJobRepository.save(any(AiJob.class))).thenAnswer(inv -> {
            AiJob j = inv.getArgument(0);
            if (j.getId() == null) j.setId(UUID.randomUUID());
            return j;
        });
        when(aiJobRepository.findById(any())).thenAnswer(inv -> {
            AiJob j = new AiJob();
            j.setId(inv.getArgument(0));
            j.setStatus("FAILED");
            return Optional.of(j);
        });

        // Return invalid JSON every time → triggers fallback
        when(ollamaClient.generate(anyString(), anyString())).thenReturn("not valid json at all");

        AiJob result = aiService.generateSrpDraft(eventId, "EARLY_WARNING");

        assertNotNull(result);
        verify(ollamaClient, times(3)).generate(anyString(), anyString()); // 1 + 2 retries
        verify(aiArtifactRepository).save(any(AiArtifact.class)); // fallback artifact saved
    }

    @Test
    void generateSrpDraft_ollamaThrows_usesFallback() {
        UUID eventId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CraEvent event = buildEvent(eventId, productId);
        Product product = buildProduct(productId);

        when(craEventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findAllByProductId(productId)).thenReturn(List.of());
        when(ollamaClient.getModel()).thenReturn("phi3.5");
        when(aiJobRepository.save(any(AiJob.class))).thenAnswer(inv -> {
            AiJob j = inv.getArgument(0);
            if (j.getId() == null) j.setId(UUID.randomUUID());
            return j;
        });
        when(aiJobRepository.findById(any())).thenAnswer(inv -> {
            AiJob j = new AiJob();
            j.setId(inv.getArgument(0));
            j.setStatus("FAILED");
            return Optional.of(j);
        });

        when(ollamaClient.generate(anyString(), anyString()))
                .thenThrow(new OllamaClient.AiGenerationException("Connection refused", new RuntimeException()));

        AiJob result = aiService.generateSrpDraft(eventId, "NOTIFICATION");

        assertNotNull(result);
        verify(ollamaClient, times(3)).generate(anyString(), anyString());
        verify(aiArtifactRepository).save(any(AiArtifact.class));
    }

    // ── generateCommPack ──────────────────────────────────

    @Test
    void generateCommPack_validResponse_completesJob() {
        UUID eventId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CraEvent event = buildEvent(eventId, productId);
        Product product = buildProduct(productId);

        when(craEventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findAllByProductId(productId)).thenReturn(List.of());
        when(ollamaClient.getModel()).thenReturn("phi3.5");
        when(aiJobRepository.save(any(AiJob.class))).thenAnswer(inv -> {
            AiJob j = inv.getArgument(0);
            if (j.getId() == null) j.setId(UUID.randomUUID());
            return j;
        });
        when(aiJobRepository.findById(any())).thenAnswer(inv -> {
            AiJob j = new AiJob();
            j.setId(inv.getArgument(0));
            j.setStatus("COMPLETED");
            return Optional.of(j);
        });

        String validCommJson = """
                {
                  "advisory_markdown": "# Security Advisory\\n\\nCritical vulnerability found.",
                  "email_subject": "Important Security Update",
                  "email_body": "Dear customer, please update your product immediately.",
                  "release_notes_markdown": "## Security Release Notes\\n\\nFixed CVE-2026-0001.",
                  "references": [{"type": "product", "id": "%s", "label": "IoT Gateway"}]
                }
                """.formatted(productId);
        when(ollamaClient.generate(anyString(), anyString())).thenReturn(validCommJson);

        AiJob result = aiService.generateCommPack(eventId);

        assertNotNull(result);
        verify(aiArtifactRepository).save(any(AiArtifact.class));
    }

    // ── fillQuestionnaire ─────────────────────────────────

    @Test
    void fillQuestionnaire_validResponse_completesJob() {
        UUID productId = UUID.randomUUID();

        Product product = buildProduct(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findAllByProductId(productId)).thenReturn(List.of());
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(product));
        when(ollamaClient.getModel()).thenReturn("phi3.5");
        when(aiJobRepository.save(any(AiJob.class))).thenAnswer(inv -> {
            AiJob j = inv.getArgument(0);
            if (j.getId() == null) j.setId(UUID.randomUUID());
            return j;
        });
        when(aiJobRepository.findById(any())).thenAnswer(inv -> {
            AiJob j = new AiJob();
            j.setId(inv.getArgument(0));
            j.setStatus("COMPLETED");
            return Optional.of(j);
        });

        String validQJson = """
                [
                  {
                    "question_id": "Q1",
                    "question": "Do you scan for vulnerabilities?",
                    "answer": "Yes, via OSV API integration.",
                    "confidence": "HIGH",
                    "references": [{"type": "product", "id": "%s"}]
                  }
                ]
                """.formatted(productId);
        when(ollamaClient.generate(anyString(), anyString())).thenReturn(validQJson);

        AiJob result = aiService.fillQuestionnaire("Q1: Do you scan for vulnerabilities?", productId);

        assertNotNull(result);
        verify(aiArtifactRepository).save(any(AiArtifact.class));
    }

    @Test
    void fillQuestionnaire_noProduct_usesOrgContextOnly() {
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(ollamaClient.getModel()).thenReturn("phi3.5");
        when(aiJobRepository.save(any(AiJob.class))).thenAnswer(inv -> {
            AiJob j = inv.getArgument(0);
            if (j.getId() == null) j.setId(UUID.randomUUID());
            return j;
        });
        when(aiJobRepository.findById(any())).thenAnswer(inv -> {
            AiJob j = new AiJob();
            j.setId(inv.getArgument(0));
            j.setStatus("FAILED");
            return Optional.of(j);
        });

        // Return invalid → fallback
        when(ollamaClient.generate(anyString(), anyString())).thenReturn("garbage");

        AiJob result = aiService.fillQuestionnaire("Q1: Security policy?\nQ2: SBOM?", null);

        assertNotNull(result);
        verify(aiArtifactRepository).save(any(AiArtifact.class));
    }

    // ── getJob ────────────────────────────────────────────

    @Test
    void getJob_existingJob_returnsResponse() {
        UUID jobId = UUID.randomUUID();
        AiJob job = new AiJob();
        job.setId(jobId);
        job.setOrgId(orgId);
        job.setStatus("COMPLETED");

        AiArtifact artifact = new AiArtifact();
        artifact.setId(UUID.randomUUID());
        artifact.setAiJobId(jobId);
        artifact.setKind("SRP_DRAFT");
        artifact.setContentJson("{}");

        when(aiJobRepository.findByIdAndOrgId(jobId, orgId)).thenReturn(Optional.of(job));
        when(aiArtifactRepository.findAllByAiJobId(jobId)).thenReturn(List.of(artifact));

        AiService.AiJobResponse response = aiService.getJob(jobId);

        assertNotNull(response);
        assertEquals(job, response.job());
        assertEquals(1, response.artifacts().size());
    }

    @Test
    void getJob_notFound_throws() {
        UUID jobId = UUID.randomUUID();
        when(aiJobRepository.findByIdAndOrgId(jobId, orgId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> aiService.getJob(jobId));
    }

    // ── Prompt contains redacted content ──────────────────

    @Test
    void generateSrpDraft_redactsPII() {
        UUID eventId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CraEvent event = buildEvent(eventId, productId);
        event.setDescription("Contact admin@corp.com at 10.0.0.1 for details");
        Product product = buildProduct(productId);

        when(craEventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findAllByProductId(productId)).thenReturn(List.of());
        when(ollamaClient.getModel()).thenReturn("phi3.5");
        when(aiJobRepository.save(any(AiJob.class))).thenAnswer(inv -> {
            AiJob j = inv.getArgument(0);
            if (j.getId() == null) j.setId(UUID.randomUUID());
            return j;
        });
        when(aiJobRepository.findById(any())).thenAnswer(inv -> {
            AiJob j = new AiJob();
            j.setId(inv.getArgument(0));
            return Optional.of(j);
        });

        when(ollamaClient.generate(anyString(), anyString())).thenReturn("invalid");

        aiService.generateSrpDraft(eventId, "EARLY_WARNING");

        // Verify the prompt sent to Ollama does NOT contain PII
        var promptCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(ollamaClient, atLeastOnce()).generate(anyString(), promptCaptor.capture());
        String sentPrompt = promptCaptor.getValue();
        assertFalse(sentPrompt.contains("admin@corp.com"), "Email should be redacted");
        assertFalse(sentPrompt.contains("10.0.0.1"), "IP should be redacted");
    }

    // ── JSON extraction with markdown wrapper ─────────────

    @Test
    void generateSrpDraft_jsonInMarkdownBlock_isExtracted() {
        UUID eventId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CraEvent event = buildEvent(eventId, productId);
        Product product = buildProduct(productId);

        when(craEventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findAllByProductId(productId)).thenReturn(List.of());
        when(ollamaClient.getModel()).thenReturn("phi3.5");
        when(aiJobRepository.save(any(AiJob.class))).thenAnswer(inv -> {
            AiJob j = inv.getArgument(0);
            if (j.getId() == null) j.setId(UUID.randomUUID());
            return j;
        });
        when(aiJobRepository.findById(any())).thenAnswer(inv -> {
            AiJob j = new AiJob();
            j.setId(inv.getArgument(0));
            j.setStatus("COMPLETED");
            return Optional.of(j);
        });

        // LLM returns JSON wrapped in markdown code block
        String wrappedResponse = """
                Here is the SRP draft:
                ```json
                {
                  "summary": "Critical vulnerability in IoT Gateway firmware",
                  "affected_versions": ["1.0.0"],
                  "impact": "Remote code execution",
                  "mitigation": "Disable OTA updates",
                  "patch_status": "In progress",
                  "timeline": [{"date": "2026-02-01", "event": "Detected"}],
                  "references": [{"type": "product", "id": "%s", "label": "Test"}]
                }
                ```
                """.formatted(productId);
        when(ollamaClient.generate(anyString(), anyString())).thenReturn(wrappedResponse);

        AiJob result = aiService.generateSrpDraft(eventId, "EARLY_WARNING");

        assertNotNull(result);
        // Should have extracted and validated successfully — only 1 call needed
        verify(ollamaClient, times(1)).generate(anyString(), anyString());
    }

    // ── Helpers ───────────────────────────────────────────

    private CraEvent buildEvent(UUID id, UUID productId) {
        CraEvent event = new CraEvent();
        event.setId(id);
        event.setOrgId(orgId);
        event.setProductId(productId);
        event.setTitle("Exploited vulnerability in firmware OTA");
        event.setEventType("ACTIVELY_EXPLOITED_VULNERABILITY");
        event.setStatus("OPEN");
        event.setDetectedAt(Instant.parse("2026-02-01T10:00:00Z"));
        event.setCreatedBy(userId);
        return event;
    }

    private Product buildProduct(UUID id) {
        Product product = new Product();
        product.setId(id);
        product.setOrgId(orgId);
        product.setName("IoT Gateway");
        product.setType("CLASS_I");
        product.setCriticality("HIGH");
        return product;
    }
}
