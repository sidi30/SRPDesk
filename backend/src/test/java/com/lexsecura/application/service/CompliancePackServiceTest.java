package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompliancePackServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private FindingRepository findingRepository;

    @Mock
    private FindingDecisionRepository decisionRepository;

    @Mock
    private VulnerabilityRepository vulnerabilityRepository;

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private PdfReportGenerator pdfGenerator;

    private CompliancePackService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        service = new CompliancePackService(
                productRepository,
                releaseRepository,
                evidenceRepository,
                componentRepository,
                findingRepository,
                decisionRepository,
                vulnerabilityRepository,
                auditEventRepository,
                pdfGenerator,
                objectMapper);
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void generatePack_shouldCreateZipWithJsonAndPdf() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        UUID findingId = UUID.randomUUID();
        UUID vulnerabilityId = UUID.randomUUID();
        UUID decisionId = UUID.randomUUID();

        // Setup Product
        Product product = new Product();
        product.setId(productId);
        product.setOrgId(orgId);
        product.setName("Test Product");
        product.setType("SOFTWARE");
        product.setCriticality("STANDARD");
        product.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        product.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        // Setup Release
        Release release = new Release(productId, "1.0.0");
        release.setId(releaseId);
        release.setGitRef("abc123");
        release.setCreatedAt(Instant.parse("2026-01-15T00:00:00Z"));

        // Setup Evidence
        Evidence evidence = new Evidence(releaseId, orgId, EvidenceType.SBOM, "sbom.json",
                "application/json", 1024, "a".repeat(64), "s3://bucket/sbom.json", userId);
        evidence.setId(evidenceId);
        evidence.setCreatedAt(Instant.parse("2026-01-15T01:00:00Z"));

        // Setup Component
        Component component = new Component("pkg:maven/com.example/lib@1.0", "lib", "1.0", "library");
        component.setId(componentId);

        // Setup Finding
        Finding finding = new Finding();
        finding.setId(findingId);
        finding.setReleaseId(releaseId);
        finding.setComponentId(componentId);
        finding.setVulnerabilityId(vulnerabilityId);
        finding.setStatus("OPEN");
        finding.setDetectedAt(Instant.parse("2026-02-01T10:00:00Z"));
        finding.setSource("OSV");

        // Setup Vulnerability
        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setId(vulnerabilityId);
        vulnerability.setOsvId("GHSA-1234-5678");
        vulnerability.setSummary("Test vulnerability");
        vulnerability.setSeverity("HIGH");

        // Setup FindingDecision
        FindingDecision decision = new FindingDecision();
        decision.setId(decisionId);
        decision.setFindingId(findingId);
        decision.setDecisionType("NOT_AFFECTED");
        decision.setRationale("Not used in production");
        decision.setDecidedBy(userId);
        decision.setCreatedAt(Instant.parse("2026-02-02T10:00:00Z"));

        // Setup AuditEvent
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setHash("b".repeat(64));

        // Mock repository calls
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(evidenceRepository.findAllByReleaseIdAndOrgId(releaseId, orgId)).thenReturn(List.of(evidence));
        when(componentRepository.findAllByReleaseId(releaseId)).thenReturn(List.of(component));
        when(findingRepository.findAllByReleaseId(releaseId)).thenReturn(List.of(finding));
        when(decisionRepository.findAllByFindingId(findingId)).thenReturn(List.of(decision));
        when(vulnerabilityRepository.findById(vulnerabilityId)).thenReturn(Optional.of(vulnerability));
        when(auditEventRepository.findTopByOrgIdOrderByCreatedAtDesc(orgId))
                .thenReturn(Optional.of(auditEvent));
        when(pdfGenerator.generate(eq(product), eq(release), eq(List.of(evidence)),
                eq(List.of(component)), any(), eq("b".repeat(64))))
                .thenReturn("PDF-CONTENT-BYTES".getBytes());

        // Execute
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.generatePack(releaseId, out);

        // Verify ZIP contents
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
        Set<String> entryNames = new HashSet<>();
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            entryNames.add(entry.getName());
            if ("report.json".equals(entry.getName())) {
                byte[] jsonBytes = zis.readAllBytes();
                String json = new String(jsonBytes);
                assertTrue(json.contains("Test Product"));
                assertTrue(json.contains("1.0.0"));
                assertTrue(json.contains("b".repeat(64)));
            }
            if ("report.pdf".equals(entry.getName())) {
                byte[] pdfBytes = zis.readAllBytes();
                assertEquals("PDF-CONTENT-BYTES", new String(pdfBytes));
            }
            zis.closeEntry();
        }
        zis.close();

        assertTrue(entryNames.contains("report.json"), "ZIP should contain report.json");
        assertTrue(entryNames.contains("report.pdf"), "ZIP should contain report.pdf");
        assertEquals(2, entryNames.size());
    }

    @Test
    void generatePack_releaseNotFound_shouldThrow() {
        UUID releaseId = UUID.randomUUID();

        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.empty());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.generatePack(releaseId, out));

        assertTrue(ex.getMessage().contains("Release not found"));
    }
}
