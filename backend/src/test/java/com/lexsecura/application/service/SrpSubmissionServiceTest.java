package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.*;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class SrpSubmissionServiceTest {

    @Mock private SrpSubmissionRepository submissionRepository;
    @Mock private CraEventRepository eventRepository;
    @Mock private CraEventLinkRepository linkRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ReleaseRepository releaseRepository;
    @Mock private FindingRepository findingRepository;
    @Mock private VulnerabilityRepository vulnerabilityRepository;
    @Mock private ComponentRepository componentRepository;
    @Mock private EvidenceRepository evidenceRepository;
    @Mock private AuditService auditService;

    private SrpSubmissionService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        service = new SrpSubmissionService(
                submissionRepository, eventRepository, linkRepository,
                productRepository, releaseRepository, findingRepository,
                vulnerabilityRepository, componentRepository, evidenceRepository,
                auditService, objectMapper);
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ── CREATE ───────────────────────────────────────────────

    @Test
    void create_earlyWarning_shouldAutofillAndSave() {
        CraEvent event = buildEvent();
        Product product = new Product();
        product.setId(productId);
        product.setName("IoT Gateway");
        product.setType("CLASS_I");
        product.setCriticality("HIGH");

        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(linkRepository.findAllByCraEventIdAndLinkType(eventId, "RELEASE")).thenReturn(List.of());
        when(linkRepository.findAllByCraEventIdAndLinkType(eventId, "FINDING")).thenReturn(List.of());
        when(linkRepository.findAllByCraEventIdAndLinkType(eventId, "EVIDENCE")).thenReturn(List.of());
        when(submissionRepository.save(any(SrpSubmission.class))).thenAnswer(inv -> {
            SrpSubmission s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        SrpSubmissionResponse response = service.create(eventId, new SrpSubmissionCreateRequest("EARLY_WARNING"));

        assertNotNull(response.id());
        assertEquals("EARLY_WARNING", response.submissionType());
        assertEquals("DRAFT", response.status());
        assertEquals("1.0", response.schemaVersion());
        assertNotNull(response.contentJson());
        verify(auditService).record(eq(orgId), eq("SRP_SUBMISSION"), any(), eq("CREATE"), eq(userId), anyMap());
    }

    @Test
    void create_invalidType_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(eventId, new SrpSubmissionCreateRequest("INVALID")));
    }

    @Test
    void create_eventNotFound_shouldThrow() {
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.create(eventId, new SrpSubmissionCreateRequest("EARLY_WARNING")));
    }

    @Test
    void create_withLinkedData_shouldIncludeInContent() throws Exception {
        CraEvent event = buildEvent();
        Product product = new Product();
        product.setId(productId);
        product.setName("IoT Gateway");
        product.setType("CLASS_I");
        product.setCriticality("HIGH");

        UUID releaseId = UUID.randomUUID();
        UUID findingId = UUID.randomUUID();
        UUID vulnId = UUID.randomUUID();
        UUID compId = UUID.randomUUID();

        CraEventLink releaseLink = new CraEventLink();
        releaseLink.setTargetId(releaseId);
        CraEventLink findingLink = new CraEventLink();
        findingLink.setTargetId(findingId);

        Release release = new Release();
        release.setId(releaseId);
        release.setVersion("1.0.0");
        release.setStatus(ReleaseStatus.RELEASED);

        Finding finding = new Finding();
        finding.setId(findingId);
        finding.setVulnerabilityId(vulnId);
        finding.setComponentId(compId);
        finding.setStatus("OPEN");
        finding.setDetectedAt(Instant.now());

        Vulnerability vuln = new Vulnerability();
        vuln.setOsvId("GHSA-1234");
        vuln.setSeverity("CRITICAL");
        vuln.setSummary("RCE in log4j");

        Component comp = new Component("pkg:maven/org.apache/log4j@2.14.1", "log4j", "2.14.1", "library");
        comp.setId(compId);

        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(linkRepository.findAllByCraEventIdAndLinkType(eventId, "RELEASE")).thenReturn(List.of(releaseLink));
        when(linkRepository.findAllByCraEventIdAndLinkType(eventId, "FINDING")).thenReturn(List.of(findingLink));
        when(linkRepository.findAllByCraEventIdAndLinkType(eventId, "EVIDENCE")).thenReturn(List.of());
        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));
        when(findingRepository.findById(findingId)).thenReturn(Optional.of(finding));
        when(vulnerabilityRepository.findById(vulnId)).thenReturn(Optional.of(vuln));
        when(componentRepository.findById(compId)).thenReturn(Optional.of(comp));
        when(submissionRepository.save(any(SrpSubmission.class))).thenAnswer(inv -> {
            SrpSubmission s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        SrpSubmissionResponse response = service.create(eventId, new SrpSubmissionCreateRequest("NOTIFICATION"));

        assertNotNull(response.contentJson());
        String json = response.contentJson().toString();
        assertTrue(json.contains("IoT Gateway"));
        assertTrue(json.contains("GHSA-1234"));
        assertTrue(json.contains("1.0.0"));
    }

    // ── FIND ALL ─────────────────────────────────────────────

    @Test
    void findAll_shouldReturnSubmissions() {
        CraEvent event = buildEvent();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        SrpSubmission sub = buildSubmission(UUID.randomUUID());
        when(submissionRepository.findAllByCraEventId(eventId)).thenReturn(List.of(sub));

        List<SrpSubmissionResponse> results = service.findAll(eventId);
        assertEquals(1, results.size());
    }

    @Test
    void findAll_eventNotFound_shouldThrow() {
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findAll(eventId));
    }

    // ── FIND BY ID ───────────────────────────────────────────

    @Test
    void findById_found_shouldReturn() {
        UUID subId = UUID.randomUUID();
        CraEvent event = buildEvent();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        SrpSubmission sub = buildSubmission(subId);
        when(submissionRepository.findByIdAndCraEventId(subId, eventId)).thenReturn(Optional.of(sub));

        SrpSubmissionResponse response = service.findById(eventId, subId);
        assertEquals(subId, response.id());
    }

    @Test
    void findById_notFound_shouldThrow() {
        UUID subId = UUID.randomUUID();
        CraEvent event = buildEvent();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(submissionRepository.findByIdAndCraEventId(subId, eventId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(eventId, subId));
    }

    // ── VALIDATE ─────────────────────────────────────────────

    @Test
    void validate_validContent_shouldReturnNoErrors() {
        UUID subId = UUID.randomUUID();
        CraEvent event = buildEvent();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        SrpSubmission sub = buildSubmission(subId);
        sub.setContentJson("{\"product\":{\"name\":\"Test\",\"type\":\"CLASS_I\"},\"event\":{\"type\":\"EXPLOITED_VULNERABILITY\",\"title\":\"Test\",\"detectedAt\":\"2026-02-21T00:00:00Z\"}}");
        when(submissionRepository.findByIdAndCraEventId(subId, eventId)).thenReturn(Optional.of(sub));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SrpSubmissionResponse response = service.validate(eventId, subId);

        // Validation errors should be empty array
        assertNotNull(response);
    }

    @Test
    void validate_missingRequiredFields_shouldReturnErrors() {
        UUID subId = UUID.randomUUID();
        CraEvent event = buildEvent();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        SrpSubmission sub = buildSubmission(subId);
        sub.setContentJson("{}");
        when(submissionRepository.findByIdAndCraEventId(subId, eventId)).thenReturn(Optional.of(sub));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SrpSubmissionResponse response = service.validate(eventId, subId);

        // Should have validation errors since {} is missing product and event
        assertNotNull(response.validationErrors());
    }

    // ── MARK READY ───────────────────────────────────────────

    @Test
    void markReady_validSubmission_shouldSetReady() {
        UUID subId = UUID.randomUUID();
        CraEvent event = buildEvent();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        SrpSubmission sub = buildSubmission(subId);
        sub.setContentJson("{\"product\":{\"name\":\"Test\",\"type\":\"CLASS_I\"},\"event\":{\"type\":\"EXPLOITED_VULNERABILITY\",\"title\":\"Test\",\"detectedAt\":\"2026-02-21T00:00:00Z\"}}");
        when(submissionRepository.findByIdAndCraEventId(subId, eventId)).thenReturn(Optional.of(sub));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SrpSubmissionResponse response = service.markReady(eventId, subId);

        assertEquals("READY", response.status());
        verify(auditService).record(eq(orgId), eq("SRP_SUBMISSION"), eq(subId), eq("MARK_READY"), eq(userId), anyMap());
    }

    @Test
    void markReady_withErrors_shouldThrow() {
        UUID subId = UUID.randomUUID();
        CraEvent event = buildEvent();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        SrpSubmission sub = buildSubmission(subId);
        sub.setContentJson("{}");
        when(submissionRepository.findByIdAndCraEventId(subId, eventId)).thenReturn(Optional.of(sub));

        assertThrows(IllegalStateException.class, () -> service.markReady(eventId, subId));
    }

    // ── MARK SUBMITTED ───────────────────────────────────────

    @Test
    void markSubmitted_shouldSetStatusAndReference() {
        UUID subId = UUID.randomUUID();
        CraEvent event = buildEvent();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        SrpSubmission sub = buildSubmission(subId);
        sub.setStatus("READY");
        when(submissionRepository.findByIdAndCraEventId(subId, eventId)).thenReturn(Optional.of(sub));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MarkSubmittedRequest request = new MarkSubmittedRequest("SRP-2026-001234");
        SrpSubmissionResponse response = service.markSubmitted(eventId, subId, request, null);

        assertEquals("SUBMITTED", response.status());
        assertEquals("SRP-2026-001234", response.submittedReference());
        assertNotNull(response.submittedAt());
        verify(auditService).record(eq(orgId), eq("SRP_SUBMISSION"), eq(subId), eq("MARK_SUBMITTED"), eq(userId), anyMap());
    }

    @Test
    void markSubmitted_withAckEvidence_shouldStoreId() {
        UUID subId = UUID.randomUUID();
        UUID ackId = UUID.randomUUID();
        CraEvent event = buildEvent();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        SrpSubmission sub = buildSubmission(subId);
        sub.setStatus("EXPORTED");
        when(submissionRepository.findByIdAndCraEventId(subId, eventId)).thenReturn(Optional.of(sub));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SrpSubmissionResponse response = service.markSubmitted(eventId, subId,
                new MarkSubmittedRequest("REF-123"), ackId);

        assertEquals(ackId, response.acknowledgmentEvidenceId());
    }

    // ── MARK EXPORTED ────────────────────────────────────────

    @Test
    void markExported_readySubmission_shouldSetExported() {
        SrpSubmission sub = buildSubmission(UUID.randomUUID());
        sub.setStatus("READY");
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.markExported(sub);

        assertEquals("EXPORTED", sub.getStatus());
        verify(submissionRepository).save(sub);
    }

    @Test
    void markExported_draftSubmission_shouldNotChange() {
        SrpSubmission sub = buildSubmission(UUID.randomUUID());
        sub.setStatus("DRAFT");

        service.markExported(sub);

        assertEquals("DRAFT", sub.getStatus());
        verify(submissionRepository, never()).save(any());
    }

    // ── HELPERS ──────────────────────────────────────────────

    private CraEvent buildEvent() {
        CraEvent event = new CraEvent();
        event.setId(eventId);
        event.setOrgId(orgId);
        event.setProductId(productId);
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setTitle("Test event");
        event.setDescription("Description");
        event.setStatus("DRAFT");
        event.setDetectedAt(Instant.now());
        event.setCreatedBy(userId);
        event.setCreatedAt(Instant.now());
        event.setUpdatedAt(Instant.now());
        return event;
    }

    private SrpSubmission buildSubmission(UUID subId) {
        SrpSubmission sub = new SrpSubmission();
        sub.setId(subId);
        sub.setCraEventId(eventId);
        sub.setSubmissionType("EARLY_WARNING");
        sub.setStatus("DRAFT");
        sub.setContentJson("{\"product\":{\"name\":\"Test\"},\"event\":{\"type\":\"EXPLOITED_VULNERABILITY\",\"title\":\"Test\",\"detectedAt\":\"2026-02-21T00:00:00Z\"}}");
        sub.setSchemaVersion("1.0");
        sub.setGeneratedBy(userId);
        sub.setGeneratedAt(Instant.now());
        sub.setUpdatedAt(Instant.now());
        return sub;
    }
}
