package com.lexsecura.application.service;

import com.lexsecura.application.dto.EvidenceResponse;
import com.lexsecura.application.port.StoragePort;
import com.lexsecura.domain.model.Evidence;
import com.lexsecura.domain.model.EvidenceType;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.repository.EvidenceRepository;
import com.lexsecura.domain.repository.ReleaseRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    @Mock private EvidenceRepository evidenceRepository;
    @Mock private ReleaseRepository releaseRepository;
    @Mock private StoragePort storagePort;
    @Mock private Counter evidencesUploadedCounter;
    @Mock private AuditService auditService;

    private EvidenceService evidenceService;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
        evidenceService = new EvidenceService(evidenceRepository, releaseRepository,
                storagePort, evidencesUploadedCounter, auditService);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void upload_shouldComputeSha256AndStore() throws Exception {
        UUID releaseId = UUID.randomUUID();

        Release release = new Release(UUID.randomUUID(), "1.0.0");
        release.setId(releaseId);
        when(releaseRepository.findById(releaseId)).thenReturn(Optional.of(release));

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("sbom.json");
        when(file.getContentType()).thenReturn("application/json");
        when(file.getSize()).thenReturn(2048L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("sbom content".getBytes()));

        doNothing().when(storagePort).upload(anyString(), any(InputStream.class), anyLong(), anyString());
        when(evidenceRepository.save(any())).thenAnswer(inv -> {
            Evidence e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        EvidenceResponse response = evidenceService.upload(releaseId, "SBOM", file);

        assertNotNull(response);
        assertNotNull(response.sha256());
        assertEquals(64, response.sha256().length());
        assertEquals("sbom.json", response.filename());
        assertEquals("SBOM", response.type());
        assertEquals(releaseId, response.releaseId());
        verify(storagePort).upload(anyString(), any(InputStream.class), anyLong(), anyString());
        verify(evidencesUploadedCounter).increment();
        verify(auditService).record(eq(orgId), eq("EVIDENCE"), any(UUID.class),
                eq("CREATE"), eq(userId), any());
    }

    @Test
    void upload_releaseNotFound_shouldThrow() {
        UUID releaseId = UUID.randomUUID();
        when(releaseRepository.findById(releaseId)).thenReturn(Optional.empty());

        MultipartFile file = mock(MultipartFile.class);

        assertThrows(EntityNotFoundException.class, () ->
                evidenceService.upload(releaseId, "SBOM", file));
    }

    @Test
    void delete_shouldRemoveFromStorageAndDbAndAudit() {
        UUID evidenceId = UUID.randomUUID();
        Evidence evidence = new Evidence(UUID.randomUUID(), orgId, EvidenceType.SBOM,
                "file.json", "application/json", 1024, "abc123",
                "key/file.json", userId);
        evidence.setId(evidenceId);

        when(evidenceRepository.findByIdAndOrgId(evidenceId, orgId)).thenReturn(Optional.of(evidence));

        evidenceService.delete(evidenceId);

        verify(storagePort).delete("key/file.json");
        verify(evidenceRepository).deleteById(evidenceId);
        verify(auditService).record(eq(orgId), eq("EVIDENCE"), eq(evidenceId),
                eq("DELETE"), eq(userId), any());
    }

    @Test
    void download_shouldReturnInputStream() {
        UUID evidenceId = UUID.randomUUID();
        Evidence evidence = new Evidence(UUID.randomUUID(), orgId, EvidenceType.TEST_REPORT,
                "report.pdf", "application/pdf", 4096, "def456",
                "org/releases/rel/report.pdf", userId);
        evidence.setId(evidenceId);

        when(evidenceRepository.findByIdAndOrgId(evidenceId, orgId)).thenReturn(Optional.of(evidence));
        InputStream mockStream = new ByteArrayInputStream("pdf content".getBytes());
        when(storagePort.download("org/releases/rel/report.pdf")).thenReturn(mockStream);

        InputStream result = evidenceService.download(evidenceId);

        assertNotNull(result);
        verify(storagePort).download("org/releases/rel/report.pdf");
    }
}
