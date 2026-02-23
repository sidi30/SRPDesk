package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lexsecura.application.dto.ReadinessScoreResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadinessServiceTest {

    @Mock private CraChecklistRepository checklistRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ReleaseRepository releaseRepository;
    @Mock private FindingRepository findingRepository;
    @Mock private EvidenceRepository evidenceRepository;
    @Mock private CraEventRepository craEventRepository;
    @Mock private SrpSubmissionRepository srpSubmissionRepository;
    @Mock private ReadinessSnapshotRepository snapshotRepository;
    @Mock private VulnerabilityRepository vulnerabilityRepository;

    private ReadinessService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        service = new ReadinessService(
                checklistRepository, productRepository, releaseRepository,
                findingRepository, evidenceRepository, craEventRepository,
                srpSubmissionRepository, snapshotRepository, vulnerabilityRepository,
                objectMapper);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void computeScore_noChecklist_noReleases_shouldReturnLowScore() {
        Product product = new Product(orgId, "Test", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(checklistRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(craEventRepository.findAllByOrgIdAndProductId(orgId, productId)).thenReturn(List.of());

        ReadinessScoreResponse score = service.computeScore(productId);

        assertNotNull(score);
        assertEquals(productId, score.productId());
        assertTrue(score.overallScore() >= 0 && score.overallScore() <= 100);
        assertEquals(5, score.categories().size());
        assertFalse(score.actionItems().isEmpty(), "Should have action items for missing checklist/releases");
    }

    @Test
    void computeScore_allCompliant_shouldReturnHighScore() {
        Product product = new Product(orgId, "Test", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));

        // All 21 checklist items compliant
        List<CraChecklistItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < 13; i++) {
            items.add(createChecklistItem("AI-1." + (i + 1), "SECURE_BY_DESIGN", "COMPLIANT"));
        }
        for (int i = 0; i < 8; i++) {
            items.add(createChecklistItem("AII-2." + (i + 1), "VULNERABILITY_MANAGEMENT", "COMPLIANT"));
        }
        when(checklistRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(items);

        // Release with SBOM + conformity evidence + design doc
        UUID releaseId = UUID.randomUUID();
        Release release = new Release(productId, "1.0.0");
        release.setId(releaseId);
        release.setOrgId(orgId);
        release.setStatus(ReleaseStatus.RELEASED);
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(release));

        Evidence sbomEvidence = new Evidence(releaseId, orgId, EvidenceType.SBOM, "sbom.json",
                "application/json", 1000, "abc123", "uri", userId);
        Evidence conformEvidence = new Evidence(releaseId, orgId, EvidenceType.CONFORMITY_DECLARATION,
                "conformity.pdf", "application/pdf", 2000, "def456", "uri2", userId);
        Evidence designEvidence = new Evidence(releaseId, orgId, EvidenceType.DESIGN_DOC,
                "design.pdf", "application/pdf", 3000, "ghi789", "uri3", userId);
        when(evidenceRepository.findAllByReleaseIdAndOrgId(releaseId, orgId))
                .thenReturn(List.of(sbomEvidence, conformEvidence, designEvidence));

        // Finding with decision (not open)
        Finding finding = new Finding();
        finding.setId(UUID.randomUUID());
        finding.setReleaseId(releaseId);
        finding.setVulnerabilityId(UUID.randomUUID());
        finding.setStatus("FIXED");
        finding.setSource("SCAN");
        finding.setDetectedAt(Instant.now());
        when(findingRepository.findAllByReleaseId(releaseId)).thenReturn(List.of(finding));

        // CRA event with submission
        CraEvent event = new CraEvent();
        event.setId(UUID.randomUUID());
        event.setOrgId(orgId);
        event.setProductId(productId);
        event.setStatus("CLOSED");
        when(craEventRepository.findAllByOrgIdAndProductId(orgId, productId)).thenReturn(List.of(event));

        SrpSubmission submission = new SrpSubmission();
        submission.setId(UUID.randomUUID());
        when(srpSubmissionRepository.findAllByCraEventId(event.getId())).thenReturn(List.of(submission));

        ReadinessScoreResponse score = service.computeScore(productId);

        assertTrue(score.overallScore() >= 70, "With all compliant checklist + evidences, score should be high. Got: " + score.overallScore());
    }

    @Test
    void computeScore_productNotFound_shouldThrow() {
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.computeScore(productId));
    }

    @Test
    void computeScore_categoriesShouldMatchExpectedNames() {
        Product product = new Product(orgId, "Test", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(checklistRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(craEventRepository.findAllByOrgIdAndProductId(orgId, productId)).thenReturn(List.of());

        ReadinessScoreResponse score = service.computeScore(productId);

        List<String> categoryNames = score.categories().stream()
                .map(ReadinessScoreResponse.CategoryScore::name).toList();
        assertTrue(categoryNames.contains("SECURE_BY_DESIGN"));
        assertTrue(categoryNames.contains("VULNERABILITY_MANAGEMENT"));
        assertTrue(categoryNames.contains("SBOM_MANAGEMENT"));
        assertTrue(categoryNames.contains("INCIDENT_REPORTING"));
        assertTrue(categoryNames.contains("DOCUMENTATION"));
    }

    @Test
    void computeScore_maxScoreShouldSum100() {
        Product product = new Product(orgId, "Test", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(checklistRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(craEventRepository.findAllByOrgIdAndProductId(orgId, productId)).thenReturn(List.of());

        ReadinessScoreResponse score = service.computeScore(productId);

        int totalMaxScore = score.categories().stream()
                .mapToInt(ReadinessScoreResponse.CategoryScore::maxScore).sum();
        assertEquals(100, totalMaxScore);
    }

    @Test
    void snapshotScore_shouldSaveSnapshot() {
        Product product = new Product(orgId, "Test", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(checklistRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());
        when(craEventRepository.findAllByOrgIdAndProductId(orgId, productId)).thenReturn(List.of());
        when(snapshotRepository.save(any(ReadinessSnapshot.class))).thenAnswer(inv -> {
            ReadinessSnapshot s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        ReadinessScoreResponse result = service.snapshotScore(productId);

        assertNotNull(result);
        verify(snapshotRepository).save(any(ReadinessSnapshot.class));
    }

    @Test
    void computeScore_criticalFindingsAffectScore() {
        Product product = new Product(orgId, "Test", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(checklistRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());

        UUID releaseId = UUID.randomUUID();
        Release release = new Release(productId, "1.0.0");
        release.setId(releaseId);
        release.setOrgId(orgId);
        release.setStatus(ReleaseStatus.RELEASED);
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(release));
        when(evidenceRepository.findAllByReleaseIdAndOrgId(releaseId, orgId)).thenReturn(List.of());
        when(craEventRepository.findAllByOrgIdAndProductId(orgId, productId)).thenReturn(List.of());

        // 4 open critical findings
        UUID vulnId = UUID.randomUUID();
        Vulnerability vuln = new Vulnerability();
        vuln.setId(vulnId);
        vuln.setSeverity("CRITICAL");
        when(vulnerabilityRepository.findById(vulnId)).thenReturn(Optional.of(vuln));

        List<Finding> findings = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Finding f = new Finding();
            f.setId(UUID.randomUUID());
            f.setReleaseId(releaseId);
            f.setVulnerabilityId(vulnId);
            f.setStatus("OPEN");
            f.setSource("SCAN");
            f.setDetectedAt(Instant.now());
            findings.add(f);
        }
        when(findingRepository.findAllByReleaseId(releaseId)).thenReturn(findings);

        ReadinessScoreResponse score = service.computeScore(productId);

        // Should have action item about critical findings
        assertTrue(score.actionItems().stream()
                .anyMatch(a -> a.contains("critiques") || a.contains("critical")),
                "Should recommend reducing critical findings");
    }

    private CraChecklistItem createChecklistItem(String ref, String category, String status) {
        CraChecklistItem item = new CraChecklistItem();
        item.setId(UUID.randomUUID());
        item.setOrgId(orgId);
        item.setProductId(productId);
        item.setRequirementRef(ref);
        item.setCategory(category);
        item.setTitle("Requirement " + ref);
        item.setStatus(status);
        item.setEvidenceIds(List.of());
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        return item;
    }
}
