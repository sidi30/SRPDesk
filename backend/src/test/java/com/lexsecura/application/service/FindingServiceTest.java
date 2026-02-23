package com.lexsecura.application.service;

import com.lexsecura.application.dto.FindingDecisionRequest;
import com.lexsecura.application.dto.FindingDecisionResponse;
import com.lexsecura.application.dto.FindingResponse;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class FindingServiceTest {

    @Mock
    private FindingRepository findingRepository;

    @Mock
    private FindingDecisionRepository decisionRepository;

    @Mock
    private VulnerabilityRepository vulnerabilityRepository;

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private AuditService auditService;

    private FindingService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new FindingService(
                findingRepository,
                decisionRepository,
                vulnerabilityRepository,
                componentRepository,
                releaseRepository,
                auditService,
                new com.fasterxml.jackson.databind.ObjectMapper());
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void findByReleaseId_shouldReturnFindings() {
        UUID releaseId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        UUID vulnerabilityId = UUID.randomUUID();
        UUID findingId = UUID.randomUUID();

        Finding finding = new Finding();
        finding.setId(findingId);
        finding.setReleaseId(releaseId);
        finding.setComponentId(componentId);
        finding.setVulnerabilityId(vulnerabilityId);
        finding.setStatus("OPEN");
        finding.setDetectedAt(Instant.parse("2026-02-01T10:00:00Z"));
        finding.setSource("OSV");

        Component component = new Component("pkg:maven/com.example/lib@1.0", "lib", "1.0", "library");
        component.setId(componentId);

        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setId(vulnerabilityId);
        vulnerability.setOsvId("GHSA-1234-5678");
        vulnerability.setSummary("Test vulnerability");
        vulnerability.setSeverity("HIGH");

        when(findingRepository.findAllByReleaseId(releaseId)).thenReturn(List.of(finding));
        when(vulnerabilityRepository.findById(vulnerabilityId)).thenReturn(Optional.of(vulnerability));
        when(componentRepository.findById(componentId)).thenReturn(Optional.of(component));
        when(decisionRepository.findAllByFindingId(findingId)).thenReturn(List.of());

        List<FindingResponse> results = service.findByReleaseId(releaseId, null);

        assertEquals(1, results.size());
        FindingResponse response = results.get(0);
        assertEquals(findingId, response.id());
        assertEquals(releaseId, response.releaseId());
        assertEquals(componentId, response.componentId());
        assertEquals("lib", response.componentName());
        assertEquals("pkg:maven/com.example/lib@1.0", response.componentPurl());
        assertEquals(vulnerabilityId, response.vulnerabilityId());
        assertEquals("GHSA-1234-5678", response.osvId());
        assertEquals("Test vulnerability", response.summary());
        assertEquals("HIGH", response.severity());
        assertEquals("OPEN", response.status());
        assertEquals("OSV", response.source());
        assertTrue(response.decisions().isEmpty());
    }

    @Test
    void findByReleaseId_withStatusFilter_shouldFilter() {
        UUID releaseId = UUID.randomUUID();
        UUID findingId = UUID.randomUUID();
        UUID componentId = UUID.randomUUID();
        UUID vulnerabilityId = UUID.randomUUID();

        Finding finding = new Finding();
        finding.setId(findingId);
        finding.setReleaseId(releaseId);
        finding.setComponentId(componentId);
        finding.setVulnerabilityId(vulnerabilityId);
        finding.setStatus("NOT_AFFECTED");
        finding.setDetectedAt(Instant.parse("2026-02-01T10:00:00Z"));
        finding.setSource("OSV");

        when(findingRepository.findAllByReleaseIdAndStatus(releaseId, "NOT_AFFECTED"))
                .thenReturn(List.of(finding));
        when(vulnerabilityRepository.findById(vulnerabilityId)).thenReturn(Optional.empty());
        when(componentRepository.findById(componentId)).thenReturn(Optional.empty());
        when(decisionRepository.findAllByFindingId(findingId)).thenReturn(List.of());

        List<FindingResponse> results = service.findByReleaseId(releaseId, "NOT_AFFECTED");

        assertEquals(1, results.size());
        assertEquals("NOT_AFFECTED", results.get(0).status());

        verify(findingRepository).findAllByReleaseIdAndStatus(releaseId, "NOT_AFFECTED");
        verify(findingRepository, never()).findAllByReleaseId(any());
    }

    @Test
    void addDecision_validType_shouldSave() {
        UUID findingId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();

        Finding finding = new Finding();
        finding.setId(findingId);
        finding.setReleaseId(releaseId);
        finding.setComponentId(UUID.randomUUID());
        finding.setVulnerabilityId(UUID.randomUUID());
        finding.setStatus("OPEN");
        finding.setDetectedAt(Instant.now());
        finding.setSource("OSV");

        Release release = new Release(UUID.randomUUID(), "1.0");
        release.setId(releaseId);

        when(findingRepository.findById(findingId)).thenReturn(Optional.of(finding));
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));
        when(decisionRepository.save(any(FindingDecision.class))).thenAnswer(inv -> {
            FindingDecision d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });
        when(findingRepository.save(any(Finding.class))).thenAnswer(inv -> inv.getArgument(0));

        FindingDecisionRequest request = new FindingDecisionRequest(
                "NOT_AFFECTED", "Component not used in production", null, null);

        FindingDecisionResponse response = service.addDecision(findingId, request);

        assertNotNull(response);
        assertEquals("NOT_AFFECTED", response.decisionType());
        assertEquals("Component not used in production", response.rationale());
        assertEquals(userId, response.decidedBy());

        // Verify finding status was updated
        ArgumentCaptor<Finding> findingCaptor = ArgumentCaptor.forClass(Finding.class);
        verify(findingRepository).save(findingCaptor.capture());
        assertEquals("NOT_AFFECTED", findingCaptor.getValue().getStatus());

        // Verify audit was recorded
        verify(auditService).record(eq(orgId), eq("FINDING_DECISION"), eq(findingId),
                eq("DECIDE"), eq(userId), anyMap());
    }

    @Test
    void addDecision_invalidType_shouldThrow() {
        UUID findingId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();

        Finding finding = new Finding();
        finding.setId(findingId);
        finding.setReleaseId(releaseId);
        finding.setStatus("OPEN");

        Release release = new Release(UUID.randomUUID(), "1.0");
        release.setId(releaseId);

        when(findingRepository.findById(findingId)).thenReturn(Optional.of(finding));
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));

        FindingDecisionRequest request = new FindingDecisionRequest(
                "INVALID", "Some rationale", null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.addDecision(findingId, request));

        assertTrue(ex.getMessage().contains("INVALID"));
        verify(decisionRepository, never()).save(any());
    }

    @Test
    void addDecision_findingNotFound_shouldThrow() {
        UUID findingId = UUID.randomUUID();

        when(findingRepository.findById(findingId)).thenReturn(Optional.empty());

        FindingDecisionRequest request = new FindingDecisionRequest(
                "NOT_AFFECTED", "Some rationale", null, null);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.addDecision(findingId, request));

        assertTrue(ex.getMessage().contains(findingId.toString()));
        verify(decisionRepository, never()).save(any());
    }
}
