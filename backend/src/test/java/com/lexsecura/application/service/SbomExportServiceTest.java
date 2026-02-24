package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.model.ReleaseStatus;
import com.lexsecura.domain.repository.ComponentRepository;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.domain.repository.ReleaseRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SbomExportServiceTest {

    @Mock private ReleaseRepository releaseRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ComponentRepository componentRepository;

    private SbomExportService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID orgId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID releaseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SbomExportService(releaseRepository, productRepository, componentRepository, objectMapper);
        TenantContext.setOrgId(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void export_spdx_shouldGenerateValidSpdx23() throws Exception {
        setupMocks();

        String json = service.export(releaseId, "spdx");

        assertNotNull(json);
        JsonNode doc = objectMapper.readTree(json);
        assertEquals("SPDX-2.3", doc.get("spdxVersion").asText());
        assertEquals("CC0-1.0", doc.get("dataLicense").asText());
        assertEquals("SPDXRef-DOCUMENT", doc.get("SPDXID").asText());
        assertTrue(doc.get("name").asText().contains("TestProduct"));
        assertTrue(doc.has("packages"));
        assertTrue(doc.has("relationships"));
        assertTrue(doc.has("creationInfo"));

        // Should have root package + 2 component packages = 3
        assertEquals(3, doc.get("packages").size());

        // Should have purl in externalRefs
        JsonNode secondPkg = doc.get("packages").get(1);
        assertTrue(secondPkg.has("externalRefs"));
        assertEquals("purl", secondPkg.get("externalRefs").get(0).get("referenceType").asText());
    }

    @Test
    void export_cyclonedx_shouldGenerateValidCycloneDx() throws Exception {
        setupMocks();

        String json = service.export(releaseId, "cyclonedx");

        assertNotNull(json);
        JsonNode doc = objectMapper.readTree(json);
        assertEquals("CycloneDX", doc.get("bomFormat").asText());
        assertEquals("1.6", doc.get("specVersion").asText());
        assertTrue(doc.has("components"));
        assertTrue(doc.has("dependencies"));
        assertTrue(doc.has("metadata"));
        assertEquals(2, doc.get("components").size());

        // Check purl on first component
        JsonNode comp = doc.get("components").get(0);
        assertEquals("pkg:npm/lodash@4.17.21", comp.get("purl").asText());
    }

    @Test
    void export_unsupportedFormat_shouldThrow() {
        setupMocks();

        assertThrows(IllegalArgumentException.class, () -> service.export(releaseId, "xml"));
    }

    @Test
    void export_spdx_emptyComponents_shouldGenerateMinimal() throws Exception {
        Release release = buildRelease();
        Product product = buildProduct();
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(componentRepository.findAllByReleaseId(releaseId)).thenReturn(List.of());

        String json = service.export(releaseId, "spdx");
        JsonNode doc = objectMapper.readTree(json);

        // Only root package
        assertEquals(1, doc.get("packages").size());
        // Only DESCRIBES relationship
        assertEquals(1, doc.get("relationships").size());
    }

    private void setupMocks() {
        Release release = buildRelease();
        Product product = buildProduct();

        Component c1 = new Component("pkg:npm/lodash@4.17.21", "lodash", "4.17.21", "library");
        c1.setId(UUID.randomUUID());
        Component c2 = new Component("pkg:npm/express@4.18.2", "express", "4.18.2", "library");
        c2.setId(UUID.randomUUID());

        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(componentRepository.findAllByReleaseId(releaseId)).thenReturn(List.of(c1, c2));
    }

    private Release buildRelease() {
        Release r = new Release();
        r.setId(releaseId);
        r.setProductId(productId);
        r.setOrgId(orgId);
        r.setVersion("2.0.0");
        r.setStatus(ReleaseStatus.RELEASED);
        return r;
    }

    private Product buildProduct() {
        Product p = new Product();
        p.setId(productId);
        p.setOrgId(orgId);
        p.setName("TestProduct");
        p.setType("CLASS_I");
        return p;
    }
}
