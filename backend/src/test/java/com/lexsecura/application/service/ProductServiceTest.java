package com.lexsecura.application.service;

import com.lexsecura.application.dto.ProductCreateRequest;
import com.lexsecura.application.dto.ProductResponse;
import com.lexsecura.application.dto.ProductUpdateRequest;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditService auditService;

    private ProductService productService;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
        productService = new ProductService(productRepository, auditService);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_shouldCreateProductWithDefaults() {
        ProductCreateRequest request = new ProductCreateRequest("Test Product", null, null, null);

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProductResponse response = productService.create(request);

        assertNotNull(response);
        assertEquals("Test Product", response.name());
        assertEquals("SOFTWARE", response.type());
        assertEquals("STANDARD", response.criticality());
        assertEquals(orgId, response.orgId());
        verify(productRepository).save(any(Product.class));
        verify(auditService).record(eq(orgId), eq("PRODUCT"), any(UUID.class),
                eq("CREATE"), eq(userId), any());
    }

    @Test
    void create_shouldCreateProductWithCustomFields() {
        List<Map<String, String>> contacts = List.of(Map.of("name", "John", "email", "john@test.com"));
        ProductCreateRequest request = new ProductCreateRequest("My Device", "HARDWARE", "CRITICAL", contacts);

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProductResponse response = productService.create(request);

        assertEquals("My Device", response.name());
        assertEquals("HARDWARE", response.type());
        assertEquals("CRITICAL", response.criticality());
        assertEquals(1, response.contacts().size());
    }

    @Test
    void findById_shouldReturnProduct() {
        UUID productId = UUID.randomUUID();
        Product product = createProduct(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));

        ProductResponse response = productService.findById(productId);

        assertNotNull(response);
        assertEquals(productId, response.id());
    }

    @Test
    void findById_notFound_shouldThrow() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.findById(productId));
    }

    @Test
    void findAll_shouldReturnProductsForOrg() {
        when(productRepository.findAllByOrgId(orgId)).thenReturn(List.of(createProduct(UUID.randomUUID())));

        List<ProductResponse> responses = productService.findAll();

        assertEquals(1, responses.size());
    }

    @Test
    void update_shouldUpdateProduct() {
        UUID productId = UUID.randomUUID();
        Product product = createProduct(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);

        ProductUpdateRequest request = new ProductUpdateRequest("Updated Name", "FIRMWARE", "CRITICAL", null);
        ProductResponse response = productService.update(productId, request);

        assertEquals("Updated Name", response.name());
        assertEquals("FIRMWARE", response.type());
        assertEquals("CRITICAL", response.criticality());
        verify(auditService).record(eq(orgId), eq("PRODUCT"), eq(productId),
                eq("UPDATE"), eq(userId), any());
    }

    @Test
    void delete_shouldDeleteAndAudit() {
        UUID productId = UUID.randomUUID();
        Product product = createProduct(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));

        productService.delete(productId);

        verify(productRepository).deleteByIdAndOrgId(productId, orgId);
        verify(auditService).record(eq(orgId), eq("PRODUCT"), eq(productId),
                eq("DELETE"), eq(userId), any());
    }

    private Product createProduct(UUID id) {
        Product p = new Product(orgId, "Test", "SOFTWARE", "STANDARD", List.of());
        p.setId(id);
        return p;
    }
}
