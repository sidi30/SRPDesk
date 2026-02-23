package com.lexsecura.application.service;

import com.lexsecura.application.dto.ReleaseCreateRequest;
import com.lexsecura.application.dto.ReleaseResponse;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseServiceTest {

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditService auditService;

    private ReleaseService releaseService;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
        releaseService = new ReleaseService(releaseRepository, productRepository, auditService);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldCreateRelease() {
        Product product = new Product(orgId, "Test Product", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));

        when(releaseRepository.save(any(Release.class))).thenAnswer(inv -> {
            Release r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ReleaseCreateRequest request = new ReleaseCreateRequest("1.0.0", "abc123", "build-42", null, null);
        ReleaseResponse response = releaseService.create(productId, request);

        assertNotNull(response);
        assertEquals("1.0.0", response.version());
        assertEquals("abc123", response.gitRef());
        assertEquals("build-42", response.buildId());
        assertEquals("DRAFT", response.status());
        assertEquals(productId, response.productId());
        verify(auditService).record(eq(orgId), eq("RELEASE"), any(UUID.class),
                eq("CREATE"), eq(userId), any());
    }

    @Test
    void create_productNotFound_shouldThrow() {
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());

        ReleaseCreateRequest request = new ReleaseCreateRequest("1.0.0", null, null, null, null);

        assertThrows(EntityNotFoundException.class, () -> releaseService.create(productId, request));
    }

    @Test
    void findAllByProductId_shouldReturnReleases() {
        Product product = new Product(orgId, "Test", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));

        Release release = new Release(productId, "1.0.0");
        release.setId(UUID.randomUUID());
        when(releaseRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(release));

        List<ReleaseResponse> responses = releaseService.findAllByProductId(productId);

        assertEquals(1, responses.size());
        assertEquals("1.0.0", responses.get(0).version());
    }

    @Test
    void findById_shouldReturnRelease() {
        UUID releaseId = UUID.randomUUID();
        Release release = new Release(productId, "2.0.0");
        release.setId(releaseId);
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.of(release));

        ReleaseResponse response = releaseService.findById(releaseId);

        assertEquals(releaseId, response.id());
        assertEquals("2.0.0", response.version());
    }

    @Test
    void findById_notFound_shouldThrow() {
        UUID releaseId = UUID.randomUUID();
        when(releaseRepository.findByIdAndOrgId(releaseId, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> releaseService.findById(releaseId));
    }
}
