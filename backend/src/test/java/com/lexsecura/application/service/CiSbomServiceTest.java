package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.CiSbomEnrichedResponse;
import com.lexsecura.application.dto.CiScanResponse;
import com.lexsecura.application.dto.SbomQualityScoreResponse;
import com.lexsecura.application.dto.SbomUploadResponse;
import com.lexsecura.domain.model.CiPolicy;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CiSbomServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ReleaseRepository releaseRepository;
    @Mock private ComponentRepository componentRepository;
    @Mock private SbomService sbomService;
    @Mock private SbomQualityService sbomQualityService;
    @Mock private VulnerabilityScanService vulnerabilityScanService;
    @Mock private CiPolicyRepository ciPolicyRepository;
    @Mock private CiUploadEventRepository ciUploadEventRepository;
    @Mock private MultipartFile file;

    private CiSbomService ciSbomService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
        ciSbomService = new CiSbomService(
                productRepository, releaseRepository, componentRepository,
                sbomService, sbomQualityService, vulnerabilityScanService,
                ciPolicyRepository, ciUploadEventRepository, objectMapper);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void uploadFromCi_existingProductAndRelease_shouldReturnEnrichedResponse() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();

        Product product = new Product(orgId, "my-app", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        Release release = new Release(productId, "1.0.0");
        release.setId(releaseId);
        release.setOrgId(orgId);

        when(productRepository.findByNameAndOrgId("my-app", orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findByProductIdAndVersionAndOrgId(productId, "1.0.0", orgId))
                .thenReturn(Optional.of(release));
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(release));
        when(componentRepository.findAllByReleaseId(releaseId)).thenReturn(List.of());
        when(sbomService.ingest(releaseId, file))
                .thenReturn(new SbomUploadResponse(UUID.randomUUID(), 42, "abc123"));

        String sbomJson = """
                {"bomFormat":"CycloneDX","components":[{"name":"test","version":"1.0"}]}""";
        when(file.getBytes()).thenReturn(sbomJson.getBytes());

        when(sbomQualityService.evaluate(any())).thenReturn(
                new SbomQualityScoreResponse(82, "B", 42, List.of()));
        when(vulnerabilityScanService.scanComponents(any()))
                .thenReturn(new VulnerabilityScanService.VulnScanResult(0, 2, 5, 12, 19));
        when(vulnerabilityScanService.scanRelease(releaseId)).thenReturn(2);

        // Use custom policy with high tolerance so vulns trigger WARN not FAIL
        CiPolicy policy = new CiPolicy();
        policy.setMaxCritical(0);
        policy.setMaxHigh(5);
        policy.setMinQualityScore(50);
        when(ciPolicyRepository.findByOrgId(orgId)).thenReturn(Optional.of(policy));
        when(ciUploadEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CiSbomEnrichedResponse response = ciSbomService.uploadFromCi("my-app", "1.0.0", "abc", file);

        assertEquals(releaseId, response.releaseId());
        assertEquals(42, response.componentCount());
        assertEquals(82, response.qualityScore());
        assertEquals("B", response.qualityGrade());
        assertEquals(0, response.vulnerabilities().critical());
        assertEquals(2, response.vulnerabilities().high());
        assertEquals(19, response.vulnerabilities().total());
        assertEquals(2, response.newVulnerabilities());
        assertEquals("WARN", response.policyResult()); // high > 0 but within max => WARN
        assertEquals("abc123", response.sha256());
        verify(ciUploadEventRepository).save(any());
    }

    @Test
    void uploadFromCi_newRelease_shouldCreateRelease() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();

        Product product = new Product(orgId, "my-app", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);

        when(productRepository.findByNameAndOrgId("my-app", orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findByProductIdAndVersionAndOrgId(productId, "2.0.0", orgId))
                .thenReturn(Optional.empty());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(releaseRepository.save(any(Release.class))).thenAnswer(inv -> {
            Release r = inv.getArgument(0);
            r.setId(releaseId);
            return r;
        });
        when(componentRepository.findAllByReleaseId(releaseId)).thenReturn(List.of());
        when(sbomService.ingest(releaseId, file))
                .thenReturn(new SbomUploadResponse(UUID.randomUUID(), 15, "def456"));

        String sbomJson = """
                {"bomFormat":"CycloneDX","components":[]}""";
        when(file.getBytes()).thenReturn(sbomJson.getBytes());

        when(sbomQualityService.evaluate(any())).thenReturn(
                new SbomQualityScoreResponse(60, "C", 15, List.of()));
        when(vulnerabilityScanService.scanComponents(any()))
                .thenReturn(new VulnerabilityScanService.VulnScanResult(0, 0, 0, 0, 0));
        when(vulnerabilityScanService.scanRelease(releaseId)).thenReturn(0);
        when(ciPolicyRepository.findByOrgId(orgId)).thenReturn(Optional.empty());
        when(ciUploadEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CiSbomEnrichedResponse response = ciSbomService.uploadFromCi("my-app", "2.0.0", "sha123", file);

        assertEquals(releaseId, response.releaseId());
        assertEquals(15, response.componentCount());
        assertEquals("PASS", response.policyResult());
        verify(releaseRepository).save(any(Release.class));
    }

    @Test
    void uploadFromCi_unknownProduct_shouldThrow() {
        when(productRepository.findByNameAndOrgId("unknown", orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> ciSbomService.uploadFromCi("unknown", "1.0.0", null, file));
    }

    @Test
    void uploadFromCi_withCustomPolicy_shouldEvaluatePolicy() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();

        Product product = new Product(orgId, "my-app", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        Release release = new Release(productId, "1.0.0");
        release.setId(releaseId);
        release.setOrgId(orgId);

        CiPolicy policy = new CiPolicy();
        policy.setMaxCritical(0);
        policy.setMaxHigh(0);  // strict: no high vulns allowed
        policy.setMinQualityScore(80);

        when(productRepository.findByNameAndOrgId("my-app", orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findByProductIdAndVersionAndOrgId(productId, "1.0.0", orgId))
                .thenReturn(Optional.of(release));
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(release));
        when(componentRepository.findAllByReleaseId(releaseId)).thenReturn(List.of());
        when(sbomService.ingest(releaseId, file))
                .thenReturn(new SbomUploadResponse(UUID.randomUUID(), 10, "hash"));

        String sbomJson = """
                {"bomFormat":"CycloneDX","components":[]}""";
        when(file.getBytes()).thenReturn(sbomJson.getBytes());

        when(sbomQualityService.evaluate(any())).thenReturn(
                new SbomQualityScoreResponse(85, "B", 10, List.of()));
        when(vulnerabilityScanService.scanComponents(any()))
                .thenReturn(new VulnerabilityScanService.VulnScanResult(0, 1, 0, 0, 1));
        when(vulnerabilityScanService.scanRelease(releaseId)).thenReturn(1);
        when(ciPolicyRepository.findByOrgId(orgId)).thenReturn(Optional.of(policy));
        when(ciUploadEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CiSbomEnrichedResponse response = ciSbomService.uploadFromCi("my-app", "1.0.0", null, file);

        assertEquals("FAIL", response.policyResult()); // 1 high > maxHigh=0
    }

    @Test
    void uploadFromCi_policyPass_shouldReturnPass() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();

        Product product = new Product(orgId, "my-app", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        Release release = new Release(productId, "1.0.0");
        release.setId(releaseId);
        release.setOrgId(orgId);

        when(productRepository.findByNameAndOrgId("my-app", orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findByProductIdAndVersionAndOrgId(productId, "1.0.0", orgId))
                .thenReturn(Optional.of(release));
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(release));
        when(componentRepository.findAllByReleaseId(releaseId)).thenReturn(List.of());
        when(sbomService.ingest(releaseId, file))
                .thenReturn(new SbomUploadResponse(UUID.randomUUID(), 10, "hash"));

        String sbomJson = """
                {"bomFormat":"CycloneDX","components":[]}""";
        when(file.getBytes()).thenReturn(sbomJson.getBytes());

        when(sbomQualityService.evaluate(any())).thenReturn(
                new SbomQualityScoreResponse(90, "A", 10, List.of()));
        when(vulnerabilityScanService.scanComponents(any()))
                .thenReturn(new VulnerabilityScanService.VulnScanResult(0, 0, 0, 0, 0));
        when(vulnerabilityScanService.scanRelease(releaseId)).thenReturn(0);
        when(ciPolicyRepository.findByOrgId(orgId)).thenReturn(Optional.empty());
        when(ciUploadEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CiSbomEnrichedResponse response = ciSbomService.uploadFromCi("my-app", "1.0.0", null, file);

        assertEquals("PASS", response.policyResult());
        assertEquals(0, response.vulnerabilities().total());
    }

    @Test
    void uploadFromCi_defaultPolicyFail_criticalExceeds() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();

        Product product = new Product(orgId, "my-app", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        Release release = new Release(productId, "1.0.0");
        release.setId(releaseId);
        release.setOrgId(orgId);

        when(productRepository.findByNameAndOrgId("my-app", orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findByProductIdAndVersionAndOrgId(productId, "1.0.0", orgId))
                .thenReturn(Optional.of(release));
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(release));
        when(componentRepository.findAllByReleaseId(releaseId)).thenReturn(List.of());
        when(sbomService.ingest(releaseId, file))
                .thenReturn(new SbomUploadResponse(UUID.randomUUID(), 10, "hash"));

        String sbomJson = """
                {"bomFormat":"CycloneDX","components":[]}""";
        when(file.getBytes()).thenReturn(sbomJson.getBytes());

        when(sbomQualityService.evaluate(any())).thenReturn(
                new SbomQualityScoreResponse(70, "C", 10, List.of()));
        when(vulnerabilityScanService.scanComponents(any()))
                .thenReturn(new VulnerabilityScanService.VulnScanResult(3, 0, 0, 0, 3));
        when(vulnerabilityScanService.scanRelease(releaseId)).thenReturn(3);
        when(ciPolicyRepository.findByOrgId(orgId)).thenReturn(Optional.empty());
        when(ciUploadEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CiSbomEnrichedResponse response = ciSbomService.uploadFromCi("my-app", "1.0.0", null, file);

        assertEquals("FAIL", response.policyResult()); // 3 critical > default max 0
    }

    @Test
    void scanFromCi_shouldReturnResponseWithoutRelease() throws Exception {
        UUID productId = UUID.randomUUID();

        Product product = new Product(orgId, "my-app", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);

        when(productRepository.findByNameAndOrgId("my-app", orgId)).thenReturn(Optional.of(product));

        String sbomJson = """
                {"bomFormat":"CycloneDX","components":[
                  {"name":"lodash","version":"4.17.21","purl":"pkg:npm/lodash@4.17.21"},
                  {"name":"express","version":"4.18.2","purl":"pkg:npm/express@4.18.2"}
                ]}""";
        when(file.getBytes()).thenReturn(sbomJson.getBytes());

        when(sbomQualityService.evaluate(any())).thenReturn(
                new SbomQualityScoreResponse(75, "B", 2, List.of()));
        when(vulnerabilityScanService.scanComponents(any()))
                .thenReturn(new VulnerabilityScanService.VulnScanResult(0, 1, 2, 0, 3));

        // Use custom policy with tolerance so high vulns => WARN not FAIL
        CiPolicy policy = new CiPolicy();
        policy.setMaxCritical(0);
        policy.setMaxHigh(5);
        policy.setMinQualityScore(50);
        when(ciPolicyRepository.findByOrgId(orgId)).thenReturn(Optional.of(policy));

        CiScanResponse response = ciSbomService.scanFromCi("my-app", file);

        assertEquals(2, response.componentCount());
        assertEquals(75, response.qualityScore());
        assertEquals("B", response.qualityGrade());
        assertEquals(3, response.vulnerabilities().total());
        assertEquals("WARN", response.policyResult()); // high > 0 but within max => WARN
        assertNotNull(response.sha256());

        // Should NOT create release or ingest SBOM
        verify(releaseRepository, never()).save(any());
        verify(sbomService, never()).ingest(any(), any());
    }

    @Test
    void scanFromCi_unknownProduct_shouldThrow() {
        when(productRepository.findByNameAndOrgId("unknown", orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> ciSbomService.scanFromCi("unknown", file));
    }

    @Test
    void scanFromCi_cleanSbom_shouldReturnPass() throws Exception {
        UUID productId = UUID.randomUUID();

        Product product = new Product(orgId, "my-app", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);

        when(productRepository.findByNameAndOrgId("my-app", orgId)).thenReturn(Optional.of(product));

        String sbomJson = """
                {"bomFormat":"CycloneDX","components":[{"name":"safe","version":"1.0","purl":"pkg:npm/safe@1.0"}]}""";
        when(file.getBytes()).thenReturn(sbomJson.getBytes());

        when(sbomQualityService.evaluate(any())).thenReturn(
                new SbomQualityScoreResponse(95, "A", 1, List.of()));
        when(vulnerabilityScanService.scanComponents(any()))
                .thenReturn(new VulnerabilityScanService.VulnScanResult(0, 0, 0, 0, 0));
        when(ciPolicyRepository.findByOrgId(orgId)).thenReturn(Optional.empty());

        CiScanResponse response = ciSbomService.scanFromCi("my-app", file);

        assertEquals("PASS", response.policyResult());
        assertEquals(0, response.vulnerabilities().total());
    }
}
