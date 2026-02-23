package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.SbomUploadResponse;
import com.lexsecura.application.port.StoragePort;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.model.Evidence;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.model.ReleaseComponent;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SbomServiceTest {

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private ComponentRepository componentRepository;

    @Mock
    private ReleaseComponentRepository releaseComponentRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private StoragePort storagePort;

    @Mock
    private AuditService auditService;

    @Mock
    private MultipartFile multipartFile;

    private SbomService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID releaseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        service = new SbomService(
                releaseRepository,
                componentRepository,
                releaseComponentRepository,
                evidenceRepository,
                storagePort,
                auditService,
                objectMapper);
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void ingest_validCycloneDX_shouldParse() throws Exception {
        String sbomJson = "{\"bomFormat\":\"CycloneDX\",\"specVersion\":\"1.4\",\"components\":["
                + "{\"name\":\"spring-boot\",\"version\":\"3.3.0\",\"type\":\"library\",\"purl\":\"pkg:maven/org.springframework.boot/spring-boot@3.3.0\"},"
                + "{\"name\":\"jackson-core\",\"version\":\"2.17.0\",\"type\":\"library\",\"purl\":\"pkg:maven/com.fasterxml.jackson.core/jackson-core@2.17.0\"}"
                + "]}";
        byte[] content = sbomJson.getBytes();

        when(multipartFile.getBytes()).thenReturn(content);
        when(multipartFile.getSize()).thenReturn((long) content.length);
        when(multipartFile.getOriginalFilename()).thenReturn("sbom.json");

        Release release = new Release(UUID.randomUUID(), "1.0.0");
        release.setId(releaseId);
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));

        UUID comp1Id = UUID.randomUUID();
        UUID comp2Id = UUID.randomUUID();

        when(componentRepository.findByPurl("pkg:maven/org.springframework.boot/spring-boot@3.3.0"))
                .thenReturn(Optional.empty());
        when(componentRepository.findByPurl("pkg:maven/com.fasterxml.jackson.core/jackson-core@2.17.0"))
                .thenReturn(Optional.empty());
        when(componentRepository.save(any(Component.class))).thenAnswer(inv -> {
            Component c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(c.getName().equals("spring-boot") ? comp1Id : comp2Id);
            }
            return c;
        });

        when(releaseComponentRepository.existsByReleaseIdAndComponentId(any(), any()))
                .thenReturn(false);
        when(releaseComponentRepository.save(any(ReleaseComponent.class))).thenAnswer(inv -> inv.getArgument(0));

        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(inv -> {
            Evidence e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        SbomUploadResponse response = service.ingest(releaseId, multipartFile);

        assertNotNull(response);
        assertEquals(2, response.componentCount());
        assertNotNull(response.sha256());
        assertEquals(64, response.sha256().length());
        assertNotNull(response.evidenceId());

        verify(componentRepository).findByPurl("pkg:maven/org.springframework.boot/spring-boot@3.3.0");
        verify(componentRepository).findByPurl("pkg:maven/com.fasterxml.jackson.core/jackson-core@2.17.0");
        verify(componentRepository, times(2)).save(any(Component.class));
        verify(releaseComponentRepository, times(2)).save(any(ReleaseComponent.class));
        verify(evidenceRepository).save(any(Evidence.class));
        verify(storagePort).upload(anyString(), any(), anyLong(), eq("application/json"));
        verify(auditService).record(eq(orgId), eq("SBOM"), eq(releaseId), eq("INGEST"), eq(userId), anyMap());
    }

    @Test
    void ingest_invalidFormat_shouldThrow() throws Exception {
        // Neither CycloneDX nor SPDX format
        String sbomJson = "{\"format\":\"unknown\",\"data\":[]}";
        byte[] content = sbomJson.getBytes();

        when(multipartFile.getBytes()).thenReturn(content);
        when(multipartFile.getSize()).thenReturn((long) content.length);

        Release release = new Release(UUID.randomUUID(), "1.0.0");
        release.setId(releaseId);
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.ingest(releaseId, multipartFile));

        assertTrue(ex.getMessage().contains("Unsupported SBOM format"));
        verify(componentRepository, never()).save(any());
    }

    @Test
    void ingest_validSpdx_shouldParse() throws Exception {
        String spdxJson = "{\"spdxVersion\":\"SPDX-2.3\",\"packages\":["
                + "{\"name\":\"lodash\",\"versionInfo\":\"4.17.21\",\"externalRefs\":["
                + "{\"referenceCategory\":\"PACKAGE_MANAGER\",\"referenceType\":\"purl\",\"referenceLocator\":\"pkg:npm/lodash@4.17.21\"}"
                + "]},"
                + "{\"name\":\"express\",\"versionInfo\":\"4.18.2\"}"
                + "]}";
        byte[] content = spdxJson.getBytes();

        when(multipartFile.getBytes()).thenReturn(content);
        when(multipartFile.getSize()).thenReturn((long) content.length);
        when(multipartFile.getOriginalFilename()).thenReturn("spdx-sbom.json");

        Release release = new Release(UUID.randomUUID(), "2.0.0");
        release.setId(releaseId);
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));

        when(componentRepository.findByPurl(anyString())).thenReturn(Optional.empty());
        when(componentRepository.save(any(Component.class))).thenAnswer(inv -> {
            Component c = inv.getArgument(0);
            if (c.getId() == null) c.setId(UUID.randomUUID());
            return c;
        });
        when(releaseComponentRepository.existsByReleaseIdAndComponentId(any(), any())).thenReturn(false);
        when(releaseComponentRepository.save(any(ReleaseComponent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(inv -> {
            Evidence e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        SbomUploadResponse response = service.ingest(releaseId, multipartFile);

        assertEquals(2, response.componentCount());
        assertNotNull(response.sha256());

        // Verify lodash used purl from externalRefs
        verify(componentRepository).findByPurl("pkg:npm/lodash@4.17.21");
        // Verify express got synthetic purl
        verify(componentRepository).findByPurl("pkg:generic/express@4.18.2");
    }

    @Test
    void ingest_exceedsMaxSize_shouldThrow() {
        when(multipartFile.getSize()).thenReturn(11L * 1024 * 1024); // 11MB

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.ingest(releaseId, multipartFile));

        assertTrue(ex.getMessage().contains("10MB"));
        verify(releaseRepository, never()).findById(any());
    }

    @Test
    void ingest_missingComponents_shouldThrow() throws Exception {
        String sbomJson = "{\"bomFormat\":\"CycloneDX\",\"specVersion\":\"1.4\"}";
        byte[] content = sbomJson.getBytes();

        when(multipartFile.getBytes()).thenReturn(content);
        when(multipartFile.getSize()).thenReturn((long) content.length);

        Release release = new Release(UUID.randomUUID(), "1.0.0");
        release.setId(releaseId);
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.ingest(releaseId, multipartFile));

        assertTrue(ex.getMessage().contains("components"));
        verify(componentRepository, never()).save(any());
    }

    @Test
    void ingest_componentWithoutPurl_shouldGenerateSyntheticPurl() throws Exception {
        String sbomJson = "{\"bomFormat\":\"CycloneDX\",\"specVersion\":\"1.4\",\"components\":["
                + "{\"name\":\"my-lib\",\"version\":\"1.0\",\"type\":\"library\"}"
                + "]}";
        byte[] content = sbomJson.getBytes();

        when(multipartFile.getBytes()).thenReturn(content);
        when(multipartFile.getSize()).thenReturn((long) content.length);
        when(multipartFile.getOriginalFilename()).thenReturn("sbom.json");

        Release release = new Release(UUID.randomUUID(), "1.0.0");
        release.setId(releaseId);
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));

        when(componentRepository.findByPurl("pkg:generic/my-lib@1.0")).thenReturn(Optional.empty());
        when(componentRepository.save(any(Component.class))).thenAnswer(inv -> {
            Component c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        when(releaseComponentRepository.existsByReleaseIdAndComponentId(any(), any()))
                .thenReturn(false);
        when(releaseComponentRepository.save(any(ReleaseComponent.class))).thenAnswer(inv -> inv.getArgument(0));

        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(inv -> {
            Evidence e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        SbomUploadResponse response = service.ingest(releaseId, multipartFile);

        assertEquals(1, response.componentCount());

        verify(componentRepository).findByPurl("pkg:generic/my-lib@1.0");

        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);
        verify(componentRepository).save(captor.capture());
        Component saved = captor.getValue();
        assertEquals("pkg:generic/my-lib@1.0", saved.getPurl());
        assertEquals("my-lib", saved.getName());
        assertEquals("1.0", saved.getVersion());
    }
}
