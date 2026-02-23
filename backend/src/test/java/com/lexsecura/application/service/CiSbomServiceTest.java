package com.lexsecura.application.service;

import com.lexsecura.application.dto.CiSbomUploadResponse;
import com.lexsecura.application.dto.SbomUploadResponse;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.domain.repository.ReleaseRepository;
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

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private SbomService sbomService;

    @Mock
    private MultipartFile file;

    private CiSbomService ciSbomService;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
        ciSbomService = new CiSbomService(productRepository, releaseRepository, sbomService);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void uploadFromCi_existingProductAndRelease_shouldUploadSbom() {
        UUID productId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();

        Product product = new Product(orgId, "my-app", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        Release release = new Release(productId, "1.0.0");
        release.setId(releaseId);
        release.setOrgId(orgId);

        when(productRepository.findByNameAndOrgId("my-app", orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findByProductIdAndVersionAndOrgId(productId, "1.0.0", orgId))
                .thenReturn(Optional.of(release));
        when(sbomService.ingest(releaseId, file))
                .thenReturn(new SbomUploadResponse(evidenceId, 42, "abc123"));

        CiSbomUploadResponse response = ciSbomService.uploadFromCi("my-app", "1.0.0", "abc", file);

        assertEquals(releaseId, response.releaseId());
        assertEquals(evidenceId, response.evidenceId());
        assertEquals(42, response.componentCount());
        assertEquals("abc123", response.sha256());
        assertFalse(response.releaseCreated());
        verify(releaseRepository, never()).save(any());
    }

    @Test
    void uploadFromCi_existingProductNewRelease_shouldCreateRelease() {
        UUID productId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();
        UUID evidenceId = UUID.randomUUID();

        Product product = new Product(orgId, "my-app", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);

        when(productRepository.findByNameAndOrgId("my-app", orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findByProductIdAndVersionAndOrgId(productId, "2.0.0", orgId))
                .thenReturn(Optional.empty());
        when(releaseRepository.save(any(Release.class))).thenAnswer(inv -> {
            Release r = inv.getArgument(0);
            r.setId(releaseId);
            return r;
        });
        when(sbomService.ingest(releaseId, file))
                .thenReturn(new SbomUploadResponse(evidenceId, 15, "def456"));

        CiSbomUploadResponse response = ciSbomService.uploadFromCi("my-app", "2.0.0", "sha123", file);

        assertTrue(response.releaseCreated());
        assertEquals(releaseId, response.releaseId());
        assertEquals(15, response.componentCount());
        verify(releaseRepository).save(any(Release.class));
    }

    @Test
    void uploadFromCi_unknownProduct_shouldThrow() {
        when(productRepository.findByNameAndOrgId("unknown", orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> ciSbomService.uploadFromCi("unknown", "1.0.0", null, file));
    }

    @Test
    void uploadFromCi_newReleaseWithGitRef_shouldSetGitRef() {
        UUID productId = UUID.randomUUID();
        UUID releaseId = UUID.randomUUID();

        Product product = new Product(orgId, "app", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);

        when(productRepository.findByNameAndOrgId("app", orgId)).thenReturn(Optional.of(product));
        when(releaseRepository.findByProductIdAndVersionAndOrgId(productId, "1.0.0", orgId))
                .thenReturn(Optional.empty());
        when(releaseRepository.save(any(Release.class))).thenAnswer(inv -> {
            Release r = inv.getArgument(0);
            r.setId(releaseId);
            return r;
        });
        when(sbomService.ingest(eq(releaseId), eq(file)))
                .thenReturn(new SbomUploadResponse(UUID.randomUUID(), 5, "hash"));

        ciSbomService.uploadFromCi("app", "1.0.0", "abc123def", file);

        verify(releaseRepository).save(argThat(r -> "abc123def".equals(r.getGitRef())));
    }
}
