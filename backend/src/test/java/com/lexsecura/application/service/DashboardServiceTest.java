package com.lexsecura.application.service;

import com.lexsecura.application.dto.DashboardResponse;
import com.lexsecura.domain.model.*;
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
                vulnerabilityRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getDashboard_emptyOrg_shouldReturnZeros() {
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of());
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of());

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

        // 2 findings: 1 open critical, 1 fixed
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

        // CRA events
        CraEvent activeEvent = new CraEvent();
        activeEvent.setId(UUID.randomUUID());
        activeEvent.setStatus("IN_REVIEW");
        CraEvent closedEvent = new CraEvent();
        closedEvent.setId(UUID.randomUUID());
        closedEvent.setStatus("CLOSED");
        when(craEventRepository.findAllByOrgId(orgId)).thenReturn(List.of(activeEvent, closedEvent));

        // Readiness + checklist
        when(readinessService.computeScore(productId)).thenThrow(new RuntimeException("test"));
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(21L);
        when(checklistRepository.countByProductIdAndOrgIdAndStatus(productId, orgId, "COMPLIANT")).thenReturn(10L);

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

        // Readiness: P1=60, P2=80
        when(readinessService.computeScore(p1.getId()))
                .thenReturn(new com.lexsecura.application.dto.ReadinessScoreResponse(p1.getId(), 60, List.of(), List.of()));
        when(readinessService.computeScore(p2.getId()))
                .thenReturn(new com.lexsecura.application.dto.ReadinessScoreResponse(p2.getId(), 80, List.of(), List.of()));

        DashboardResponse dashboard = service.getDashboard();

        assertEquals(2, dashboard.totalProducts());
        assertEquals(70.0, dashboard.averageReadinessScore());
        assertEquals(2, dashboard.productReadiness().size());
    }
}
