package com.lexsecura.application.service;

import com.lexsecura.application.dto.CvdPolicyRequest;
import com.lexsecura.application.dto.CvdPolicyResponse;
import com.lexsecura.domain.model.CvdPolicy;
import com.lexsecura.application.service.EntityNotFoundException;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.repository.CvdPolicyRepository;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CvdPolicyServiceTest {

    @Mock private CvdPolicyRepository cvdPolicyRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AuditService auditService;

    private CvdPolicyService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CvdPolicyService(cvdPolicyRepository, productRepository, auditService);
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createOrUpdate_newPolicy_shouldCreate() {
        Product product = new Product();
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(cvdPolicyRepository.findByProductIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());
        when(cvdPolicyRepository.save(any(CvdPolicy.class))).thenAnswer(inv -> {
            CvdPolicy p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        CvdPolicyRequest request = new CvdPolicyRequest(
                "security@example.com", "https://example.com/security", null,
                "https://example.com/cvd-policy", 90, true, null, "en,fr", "All products"
        );

        CvdPolicyResponse response = service.createOrUpdate(productId, request);

        assertNotNull(response.id());
        assertEquals("security@example.com", response.contactEmail());
        assertEquals(90, response.disclosureTimelineDays());
        assertTrue(response.acceptsAnonymous());
        assertEquals("DRAFT", response.status());
        verify(auditService).record(eq(orgId), eq("CVD_POLICY"), any(), eq("CREATE"), eq(userId), anyMap());
    }

    @Test
    void createOrUpdate_existingPolicy_shouldUpdate() {
        Product product = new Product();
        product.setId(productId);
        CvdPolicy existing = new CvdPolicy();
        existing.setId(UUID.randomUUID());
        existing.setOrgId(orgId);
        existing.setProductId(productId);
        existing.setContactEmail("old@example.com");

        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(cvdPolicyRepository.findByProductIdAndOrgId(productId, orgId)).thenReturn(Optional.of(existing));
        when(cvdPolicyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CvdPolicyRequest request = new CvdPolicyRequest(
                "new@example.com", null, null, null, 60, false, null, "en", null
        );

        CvdPolicyResponse response = service.createOrUpdate(productId, request);

        assertEquals("new@example.com", response.contactEmail());
        assertEquals(60, response.disclosureTimelineDays());
        assertFalse(response.acceptsAnonymous());
        verify(auditService).record(eq(orgId), eq("CVD_POLICY"), any(), eq("UPDATE"), eq(userId), anyMap());
    }

    @Test
    void createOrUpdate_productNotFound_shouldThrow() {
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());

        CvdPolicyRequest request = new CvdPolicyRequest("a@b.com", null, null, null, 90, true, null, "en", null);

        assertThrows(EntityNotFoundException.class, () -> service.createOrUpdate(productId, request));
    }

    @Test
    void publish_shouldSetPublished() {
        CvdPolicy policy = new CvdPolicy();
        policy.setId(UUID.randomUUID());
        policy.setOrgId(orgId);
        policy.setProductId(productId);
        policy.setStatus("DRAFT");

        when(cvdPolicyRepository.findByProductIdAndOrgId(productId, orgId)).thenReturn(Optional.of(policy));
        when(cvdPolicyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CvdPolicyResponse response = service.publish(productId);

        assertEquals("PUBLISHED", response.status());
        assertNotNull(response.publishedAt());
    }

    @Test
    void findByProductId_notFound_shouldThrow() {
        when(cvdPolicyRepository.findByProductIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findByProductId(productId));
    }
}
