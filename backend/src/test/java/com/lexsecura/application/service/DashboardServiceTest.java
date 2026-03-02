package com.lexsecura.application.service;

import com.lexsecura.application.dto.DashboardResponse;
import com.lexsecura.application.dto.SlaResponse;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ReleaseRepository releaseRepository;
    @Mock private FindingRepository findingRepository;
    @Mock private CraEventRepository craEventRepository;
    @Mock private CraChecklistRepository checklistRepository;
    @Mock private ReadinessService readinessService;
    @Mock private VulnerabilityRepository vulnerabilityRepository;
    @Mock private CiUploadEventRepository ciUploadEventRepository;
    @Mock private ConformityAssessmentRepository conformityAssessmentRepository;
    @Mock private RiskAssessmentRepository riskAssessmentRepository;
    @Mock private EuDocRepository euDocRepository;
    @Mock private SlaService slaService;

    private DashboardService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
        service = new DashboardService(
                productRepository, releaseRepository, findingRepository,
                craEventRepository, checklistRepository, readinessService,
                vulnerabilityRepository, ciUploadEventRepository,
                conformityAssessmentRepository, riskAssessmentRepository,
                euDocRepository, slaService);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getDashboard_emptyOrg_shouldReturnZeros() {
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());

        DashboardResponse dashboard = service.getDashboard();

        assertEquals(0, dashboard.totalProducts());
        assertEquals(0, dashboard.totalReleases());
        assertEquals(0, dashboard.totalFindings());
        assertEquals(0, dashboard.openFindings());
        assertEquals(0, dashboard.criticalHighFindings());
        assertEquals(0, dashboard.totalCraEvents());
        assertEquals(0, dashboard.activeCraEvents());
        assertEquals(0.0, dashboard.averageReadinessScore());
        assertTrue(dashboard.productReadiness().isEmpty());
        assertTrue(dashboard.alerts().isEmpty());
        assertEquals(0, dashboard.alertsCritical());
        assertEquals(0, dashboard.alertsHigh());
        assertEquals(0, dashboard.alertsMedium());
        assertEquals(0, dashboard.totalVulnerabilities());
        assertEquals(0, dashboard.productsWithEuDoc());
        assertEquals(0, dashboard.productsFullyCompliant());
        assertEquals(0, dashboard.automationScore());
    }

    @Test
    void getDashboard_withProducts_shouldComputeMetrics() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(orgId, "My Product", "CLASS_I", "HIGH", List.of());
        product.setId(productId);
        product.setConformityPath("HARMONISED_STANDARD_OR_THIRD_PARTY");
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(product));

        UUID releaseId = UUID.randomUUID();
        Release release = new Release(productId, "1.0.0");
        release.setId(releaseId);
        release.setOrgId(orgId);
        release.setStatus(ReleaseStatus.RELEASED);
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(release));

        UUID vulnId = UUID.randomUUID();
        Vulnerability vuln = new Vulnerability();
        vuln.setId(vulnId);
        vuln.setSeverity("CRITICAL");

        Finding openFinding = new Finding();
        openFinding.setId(UUID.randomUUID());
        openFinding.setReleaseId(releaseId);
        openFinding.setVulnerabilityId(vulnId);
        openFinding.setStatus("OPEN");
        openFinding.setDetectedAt(Instant.now());

        Finding fixedFinding = new Finding();
        fixedFinding.setId(UUID.randomUUID());
        fixedFinding.setReleaseId(releaseId);
        fixedFinding.setVulnerabilityId(UUID.randomUUID());
        fixedFinding.setStatus("FIXED");
        fixedFinding.setDetectedAt(Instant.now());

        when(findingRepository.findAllByReleaseId(releaseId)).thenReturn(List.of(openFinding, fixedFinding));
        when(vulnerabilityRepository.findById(vulnId)).thenReturn(Optional.of(vuln));

        CraEvent activeEvent = new CraEvent();
        activeEvent.setId(UUID.randomUUID());
        activeEvent.setOrgId(orgId);
        activeEvent.setProductId(productId);
        activeEvent.setDetectedAt(Instant.now());
        activeEvent.setTitle("Test Event");
        activeEvent.setStatus("IN_REVIEW");
        CraEvent closedEvent = new CraEvent();
        closedEvent.setId(UUID.randomUUID());
        closedEvent.setStatus("CLOSED");
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of(activeEvent, closedEvent));

        // SLA not overdue
        SlaResponse slaResponse = new SlaResponse(
                new SlaResponse.SlaDeadline(Instant.now().plusSeconds(86400), 86400, false),
                new SlaResponse.SlaDeadline(Instant.now().plusSeconds(172800), 172800, false),
                null);
        when(slaService.computeSla(activeEvent)).thenReturn(slaResponse);

        when(readinessService.computeScore(productId)).thenThrow(new RuntimeException("test"));
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(21L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(productId, orgId, "COMPLIANT")).thenReturn(10L);
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(conformityAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(riskAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(euDocRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());

        DashboardResponse dashboard = service.getDashboard();

        assertEquals(1, dashboard.totalProducts());
        assertEquals(1, dashboard.totalReleases());
        assertEquals(2, dashboard.totalFindings());
        assertEquals(1, dashboard.openFindings());
        assertEquals(1, dashboard.criticalHighFindings());
        assertEquals(2, dashboard.totalCraEvents());
        assertEquals(1, dashboard.activeCraEvents());

        assertEquals(1, dashboard.productReadiness().size());
        DashboardResponse.ProductReadiness pr = dashboard.productReadiness().get(0);
        assertEquals("My Product", pr.productName());
        assertEquals("CLASS_I", pr.type());
        assertEquals("HARMONISED_STANDARD_OR_THIRD_PARTY", pr.conformityPath());
        assertEquals(21, pr.checklistTotal());
        assertEquals(10, pr.checklistCompliant());
        assertEquals("NONE", pr.sbomFreshness());
        assertNull(pr.lastCiUploadAt());
        // Enriched fields
        assertEquals(1, pr.openFindingsCount());
        assertEquals(1, pr.criticalFindingsCount());
        assertNull(pr.conformityStatus());
        assertNull(pr.riskLevel());
        assertNull(pr.euDocStatus());
        assertEquals(1, pr.releaseCount());
        assertEquals("1.0.0", pr.latestVersion());
    }

    @Test
    void getDashboard_multipleProducts_shouldComputeAverageReadiness() {
        Product p1 = new Product(orgId, "P1", "DEFAULT", "LOW", List.of());
        p1.setId(UUID.randomUUID());
        Product p2 = new Product(orgId, "P2", "CLASS_II", "HIGH", List.of());
        p2.setId(UUID.randomUUID());

        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(p1, p2));
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(any(), eq(orgId))).thenReturn(List.of());
        when(checklistRepository.countByProductIdAndOrgId(any(), eq(orgId))).thenReturn(0L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(any(), eq(orgId), eq("COMPLIANT"))).thenReturn(0L);
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(conformityAssessmentRepository.findAllByProductIdAndOrgId(any(), eq(orgId))).thenReturn(List.of());
        when(riskAssessmentRepository.findAllByProductIdAndOrgId(any(), eq(orgId))).thenReturn(List.of());
        when(euDocRepository.findAllByProductIdAndOrgId(any(), eq(orgId))).thenReturn(List.of());

        when(readinessService.computeScore(p1.getId()))
                .thenReturn(new com.lexsecura.application.dto.ReadinessScoreResponse(p1.getId(), 60, List.of(), List.of()));
        when(readinessService.computeScore(p2.getId()))
                .thenReturn(new com.lexsecura.application.dto.ReadinessScoreResponse(p2.getId(), 80, List.of(), List.of()));

        DashboardResponse dashboard = service.getDashboard();

        assertEquals(2, dashboard.totalProducts());
        assertEquals(70.0, dashboard.averageReadinessScore());
        assertEquals(2, dashboard.productReadiness().size());
    }

    @Test
    void getDashboard_withCiUploadEvent_shouldShowFreshness() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(orgId, "CI Product", "DEFAULT", "LOW", List.of());
        product.setId(productId);

        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(product));
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(0L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(productId, orgId, "COMPLIANT")).thenReturn(0L);
        when(readinessService.computeScore(productId)).thenThrow(new RuntimeException("test"));
        when(conformityAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(riskAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(euDocRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());

        CiUploadEvent ciEvent = new CiUploadEvent();
        ciEvent.setProductId(productId);
        ciEvent.setQualityScore(85);
        ciEvent.setPolicyResult("PASS");
        ciEvent.setCreatedAt(Instant.now().minusSeconds(3600)); // 1 hour ago
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of(ciEvent));

        DashboardResponse dashboard = service.getDashboard();

        DashboardResponse.ProductReadiness pr = dashboard.productReadiness().get(0);
        assertEquals("FRESH", pr.sbomFreshness());
        assertEquals(85, pr.lastQualityScore());
        assertEquals("PASS", pr.lastPolicyResult());
        assertNotNull(pr.lastCiUploadAt());
    }

    // ── New tests ──────────────────────────────────────────

    @Test
    void getDashboard_withCriticalVuln_shouldGenerateAlert() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(orgId, "Vuln Product", "DEFAULT", "HIGH", List.of());
        product.setId(productId);
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(product));
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());

        UUID releaseId = UUID.randomUUID();
        Release release = new Release(productId, "2.0.0");
        release.setId(releaseId);
        release.setOrgId(orgId);
        release.setStatus(ReleaseStatus.RELEASED);
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(release));

        UUID vulnId = UUID.randomUUID();
        Vulnerability vuln = new Vulnerability();
        vuln.setId(vulnId);
        vuln.setSeverity("CRITICAL");
        when(vulnerabilityRepository.findById(vulnId)).thenReturn(Optional.of(vuln));

        Finding finding = new Finding();
        finding.setId(UUID.randomUUID());
        finding.setReleaseId(releaseId);
        finding.setVulnerabilityId(vulnId);
        finding.setStatus("OPEN");
        finding.setDetectedAt(Instant.now());
        when(findingRepository.findAllByReleaseId(releaseId)).thenReturn(List.of(finding));

        when(readinessService.computeScore(productId)).thenThrow(new RuntimeException("test"));
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(0L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(productId, orgId, "COMPLIANT")).thenReturn(0L);
        when(conformityAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(riskAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(euDocRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());

        DashboardResponse dashboard = service.getDashboard();

        assertTrue(dashboard.alerts().stream().anyMatch(a -> "CRITICAL_VULN".equals(a.type())));
        assertEquals(1, dashboard.alertsCritical());
    }

    @Test
    void getDashboard_withSlaOverdue_shouldGenerateAlert() {
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());

        UUID productId = UUID.randomUUID();
        CraEvent event = new CraEvent();
        event.setId(UUID.randomUUID());
        event.setOrgId(orgId);
        event.setProductId(productId);
        event.setTitle("Overdue Event");
        event.setStatus("IN_REVIEW");
        event.setDetectedAt(Instant.now().minus(Duration.ofDays(5)));
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of(event));

        SlaResponse slaResponse = new SlaResponse(
                new SlaResponse.SlaDeadline(Instant.now().minusSeconds(3600), -3600, true),
                new SlaResponse.SlaDeadline(Instant.now().plusSeconds(86400), 86400, false),
                null);
        when(slaService.computeSla(event)).thenReturn(slaResponse);

        DashboardResponse dashboard = service.getDashboard();

        assertTrue(dashboard.alerts().stream().anyMatch(a -> "SLA_OVERDUE".equals(a.type())));
        assertTrue(dashboard.alertsHigh() >= 1);
    }

    @Test
    void getDashboard_withEolImminent_shouldGenerateAlert() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(orgId, "EOL Product", "DEFAULT", "LOW", List.of());
        product.setId(productId);
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(product));
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());

        Release release = new Release(productId, "3.0.0");
        release.setId(UUID.randomUUID());
        release.setOrgId(orgId);
        release.setStatus(ReleaseStatus.RELEASED);
        release.setSupportedUntil(Instant.now().plus(Duration.ofDays(30)));
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(release));
        when(findingRepository.findAllByReleaseId(release.getId())).thenReturn(List.of());

        when(readinessService.computeScore(productId)).thenThrow(new RuntimeException("test"));
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(0L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(productId, orgId, "COMPLIANT")).thenReturn(0L);
        when(conformityAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(riskAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(euDocRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());

        DashboardResponse dashboard = service.getDashboard();

        assertTrue(dashboard.alerts().stream().anyMatch(a -> "EOL_IMMINENT".equals(a.type())));
        DashboardResponse.ProductReadiness pr = dashboard.productReadiness().get(0);
        assertNotNull(pr.supportedUntil());
        assertEquals("3.0.0", pr.latestVersion());
    }

    @Test
    void getDashboard_withConformityAssessment_shouldEnrichProductReadiness() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(orgId, "Conformity Product", "CLASS_I", "HIGH", List.of());
        product.setId(productId);
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(product));
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(readinessService.computeScore(productId)).thenThrow(new RuntimeException("test"));
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(0L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(productId, orgId, "COMPLIANT")).thenReturn(0L);

        ConformityAssessment ca = new ConformityAssessment();
        ca.setId(UUID.randomUUID());
        ca.setProductId(productId);
        ca.setOrgId(orgId);
        ca.setModule("MODULE_A");
        ca.setStatus("IN_PROGRESS");
        ca.setCurrentStep(5);
        ca.setTotalSteps(8);
        when(conformityAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(ca));
        when(riskAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(euDocRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());

        DashboardResponse dashboard = service.getDashboard();

        DashboardResponse.ProductReadiness pr = dashboard.productReadiness().get(0);
        assertEquals("IN_PROGRESS", pr.conformityStatus());
        assertEquals(63, pr.conformityProgress()); // 5/8 = 62.5 → 63
    }

    @Test
    void getDashboard_withEuDocStatus_shouldEnrichAndCount() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(orgId, "EU Doc Product", "CLASS_I", "HIGH", List.of());
        product.setId(productId);
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(product));
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(readinessService.computeScore(productId)).thenThrow(new RuntimeException("test"));
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(0L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(productId, orgId, "COMPLIANT")).thenReturn(0L);
        when(conformityAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(riskAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());

        EuDeclarationOfConformity euDoc = new EuDeclarationOfConformity();
        euDoc.setId(UUID.randomUUID());
        euDoc.setProductId(productId);
        euDoc.setOrgId(orgId);
        euDoc.setStatus("SIGNED");
        when(euDocRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(euDoc));

        DashboardResponse dashboard = service.getDashboard();

        DashboardResponse.ProductReadiness pr = dashboard.productReadiness().get(0);
        assertEquals("SIGNED", pr.euDocStatus());
        assertEquals(1, dashboard.productsWithEuDoc());
    }

    @Test
    void getDashboard_withRiskLevel_shouldEnrichProductReadiness() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(orgId, "Risk Product", "DEFAULT", "HIGH", List.of());
        product.setId(productId);
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(product));
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(readinessService.computeScore(productId)).thenThrow(new RuntimeException("test"));
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(0L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(productId, orgId, "COMPLIANT")).thenReturn(0L);
        when(conformityAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(euDocRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());

        RiskAssessment risk = new RiskAssessment();
        risk.setId(UUID.randomUUID());
        risk.setProductId(productId);
        risk.setOrgId(orgId);
        risk.setStatus("APPROVED");
        risk.setOverallRiskLevel("HIGH");
        when(riskAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(risk));

        DashboardResponse dashboard = service.getDashboard();

        DashboardResponse.ProductReadiness pr = dashboard.productReadiness().get(0);
        assertEquals("HIGH", pr.riskLevel());
        assertEquals("APPROVED", pr.riskStatus());
    }

    @Test
    void getDashboard_automationScore_shouldComputeCorrectly() {
        Product p1 = new Product(orgId, "Automated", "DEFAULT", "LOW", List.of());
        p1.setId(UUID.randomUUID());
        Product p2 = new Product(orgId, "Manual", "DEFAULT", "LOW", List.of());
        p2.setId(UUID.randomUUID());

        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(p1, p2));
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(any(), eq(orgId))).thenReturn(List.of());
        when(checklistRepository.countByProductIdAndOrgId(any(), eq(orgId))).thenReturn(0L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(any(), eq(orgId), eq("COMPLIANT"))).thenReturn(0L);
        when(readinessService.computeScore(any())).thenThrow(new RuntimeException("test"));
        when(conformityAssessmentRepository.findAllByProductIdAndOrgId(any(), eq(orgId))).thenReturn(List.of());
        when(riskAssessmentRepository.findAllByProductIdAndOrgId(any(), eq(orgId))).thenReturn(List.of());
        when(euDocRepository.findAllByProductIdAndOrgId(any(), eq(orgId))).thenReturn(List.of());

        CiUploadEvent ciEvent = new CiUploadEvent();
        ciEvent.setProductId(p1.getId());
        ciEvent.setQualityScore(90);
        ciEvent.setPolicyResult("PASS");
        ciEvent.setCreatedAt(Instant.now().minusSeconds(3600));
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of(ciEvent));

        DashboardResponse dashboard = service.getDashboard();

        // 1 of 2 products automated = 50%
        assertEquals(50, dashboard.automationScore());
    }

    @Test
    void getDashboard_fullyCompliant_shouldCount() {
        UUID productId = UUID.randomUUID();
        Product product = new Product(orgId, "Full Compliance", "CLASS_I", "HIGH", List.of());
        product.setId(productId);
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(product));
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(ciUploadEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(readinessService.computeScore(productId)).thenThrow(new RuntimeException("test"));
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(0L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(productId, orgId, "COMPLIANT")).thenReturn(0L);

        ConformityAssessment ca = new ConformityAssessment();
        ca.setStatus("APPROVED");
        ca.setTotalSteps(8);
        ca.setCurrentStep(8);
        when(conformityAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(ca));

        RiskAssessment risk = new RiskAssessment();
        risk.setStatus("APPROVED");
        risk.setOverallRiskLevel("LOW");
        when(riskAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(risk));

        EuDeclarationOfConformity euDoc = new EuDeclarationOfConformity();
        euDoc.setStatus("PUBLISHED");
        when(euDocRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(euDoc));

        DashboardResponse dashboard = service.getDashboard();

        assertEquals(1, dashboard.productsFullyCompliant());
        assertEquals(1, dashboard.productsWithEuDoc());
        DashboardResponse.ProductReadiness pr = dashboard.productReadiness().get(0);
        assertEquals("APPROVED", pr.conformityStatus());
        assertEquals(100, pr.conformityProgress());
        assertEquals("APPROVED", pr.riskStatus());
        assertEquals("LOW", pr.riskLevel());
        assertEquals("PUBLISHED", pr.euDocStatus());
    }
}
