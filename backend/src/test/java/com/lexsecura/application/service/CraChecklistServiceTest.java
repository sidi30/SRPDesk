package com.lexsecura.application.service;

import com.lexsecura.application.dto.CraChecklistItemResponse;
import com.lexsecura.application.dto.CraChecklistSummaryResponse;
import com.lexsecura.application.dto.CraChecklistUpdateRequest;
import com.lexsecura.domain.model.CraChecklistItem;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.repository.CraChecklistRepository;
import com.lexsecura.domain.repository.ProductRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CraChecklistServiceTest {

    @Mock
    private CraChecklistRepository checklistRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditService auditService;

    private CraChecklistService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
        service = new CraChecklistService(checklistRepository, productRepository, auditService);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void initializeChecklist_shouldCreate21Items() {
        Product product = new Product(orgId, "Test", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(0L);
        when(checklistRepository.save(any(CraChecklistItem.class))).thenAnswer(inv -> {
            CraChecklistItem item = inv.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });

        List<CraChecklistItemResponse> result = service.initializeChecklist(productId);

        assertEquals(21, result.size());
        verify(checklistRepository, times(21)).save(any(CraChecklistItem.class));
        verify(auditService).record(eq(orgId), eq("CRA_CHECKLIST"), eq(productId),
                eq("INITIALIZE"), eq(userId), any());

        // Verify categories
        long secureByDesign = result.stream().filter(i -> "SECURE_BY_DESIGN".equals(i.category())).count();
        long vulnMgmt = result.stream().filter(i -> "VULNERABILITY_MANAGEMENT".equals(i.category())).count();
        assertEquals(13, secureByDesign);
        assertEquals(8, vulnMgmt);

        // All items should start as NOT_ASSESSED
        assertTrue(result.stream().allMatch(i -> "NOT_ASSESSED".equals(i.status())));
    }

    @Test
    void initializeChecklist_productNotFound_shouldThrow() {
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.initializeChecklist(productId));
    }

    @Test
    void initializeChecklist_alreadyInitialized_shouldThrow() {
        Product product = new Product(orgId, "Test", "SOFTWARE", "STANDARD", List.of());
        product.setId(productId);
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));
        when(checklistRepository.countByProductIdAndOrgId(productId, orgId)).thenReturn(21L);

        assertThrows(IllegalStateException.class, () -> service.initializeChecklist(productId));
    }

    @Test
    void findAll_shouldReturnItemsForProduct() {
        CraChecklistItem item = createItem("AI-1.1", "SECURE_BY_DESIGN", "COMPLIANT");
        when(checklistRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of(item));

        List<CraChecklistItemResponse> result = service.findAll(productId);

        assertEquals(1, result.size());
        assertEquals("AI-1.1", result.get(0).requirementRef());
    }

    @Test
    void update_shouldChangeStatusAndAudit() {
        CraChecklistItem item = createItem("AI-1.1", "SECURE_BY_DESIGN", "NOT_ASSESSED");
        UUID itemId = item.getId();
        when(checklistRepository.findByIdAndOrgId(itemId, orgId)).thenReturn(Optional.of(item));
        when(checklistRepository.save(any(CraChecklistItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CraChecklistUpdateRequest request = new CraChecklistUpdateRequest("COMPLIANT", "Evidence attached", null);
        CraChecklistItemResponse result = service.update(itemId, request);

        assertEquals("COMPLIANT", result.status());
        assertEquals("Evidence attached", result.notes());
        assertEquals(userId, result.assessedBy());
        assertNotNull(result.assessedAt());

        verify(auditService).record(eq(orgId), eq("CRA_CHECKLIST"), eq(itemId),
                eq("UPDATE"), eq(userId), any());
    }

    @Test
    void update_invalidStatus_shouldThrow() {
        CraChecklistItem item = createItem("AI-1.1", "SECURE_BY_DESIGN", "NOT_ASSESSED");
        UUID itemId = item.getId();
        when(checklistRepository.findByIdAndOrgId(itemId, orgId)).thenReturn(Optional.of(item));

        CraChecklistUpdateRequest request = new CraChecklistUpdateRequest("INVALID_STATUS", null, null);
        assertThrows(IllegalArgumentException.class, () -> service.update(itemId, request));
    }

    @Test
    void update_itemNotFound_shouldThrow() {
        UUID itemId = UUID.randomUUID();
        when(checklistRepository.findByIdAndOrgId(itemId, orgId)).thenReturn(Optional.empty());

        CraChecklistUpdateRequest request = new CraChecklistUpdateRequest("COMPLIANT", null, null);
        assertThrows(EntityNotFoundException.class, () -> service.update(itemId, request));
    }

    @Test
    void getSummary_shouldComputeCorrectCounts() {
        List<CraChecklistItem> items = List.of(
                createItem("AI-1.1", "SECURE_BY_DESIGN", "COMPLIANT"),
                createItem("AI-1.2", "SECURE_BY_DESIGN", "NON_COMPLIANT"),
                createItem("AI-1.3", "SECURE_BY_DESIGN", "NOT_ASSESSED"),
                createItem("AII-2.1", "VULNERABILITY_MANAGEMENT", "COMPLIANT"),
                createItem("AII-2.2", "VULNERABILITY_MANAGEMENT", "PARTIALLY_COMPLIANT")
        );
        when(checklistRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(items);

        CraChecklistSummaryResponse summary = service.getSummary(productId);

        assertEquals(productId, summary.productId());
        assertEquals(5, summary.totalItems());
        assertEquals(2, summary.compliant());
        assertEquals(1, summary.partiallyCompliant());
        assertEquals(1, summary.nonCompliant());
        assertEquals(1, summary.notAssessed());

        // Check category breakdown
        assertNotNull(summary.categories().get("SECURE_BY_DESIGN"));
        assertEquals(3, summary.categories().get("SECURE_BY_DESIGN").total());
        assertEquals(1, summary.categories().get("SECURE_BY_DESIGN").compliant());
        assertEquals(1, summary.categories().get("SECURE_BY_DESIGN").nonCompliant());

        assertNotNull(summary.categories().get("VULNERABILITY_MANAGEMENT"));
        assertEquals(2, summary.categories().get("VULNERABILITY_MANAGEMENT").total());
        assertEquals(1, summary.categories().get("VULNERABILITY_MANAGEMENT").compliant());
        assertEquals(1, summary.categories().get("VULNERABILITY_MANAGEMENT").partiallyCompliant());
    }

    @Test
    void getSummary_emptyChecklist_shouldReturnZeros() {
        when(checklistRepository.findAllByProductIdAndOrgId(productId, orgId)).thenReturn(List.of());

        CraChecklistSummaryResponse summary = service.getSummary(productId);

        assertEquals(0, summary.totalItems());
        assertEquals(0, summary.compliant());
    }

    @Test
    void linkEvidence_shouldAddEvidenceId() {
        UUID evidenceId = UUID.randomUUID();
        CraChecklistItem item = createItem("AI-1.1", "SECURE_BY_DESIGN", "COMPLIANT");
        UUID itemId = item.getId();
        when(checklistRepository.findByIdAndOrgId(itemId, orgId)).thenReturn(Optional.of(item));
        when(checklistRepository.save(any(CraChecklistItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CraChecklistItemResponse result = service.linkEvidence(itemId, evidenceId);

        assertTrue(result.evidenceIds().contains(evidenceId));
        verify(auditService).record(eq(orgId), eq("CRA_CHECKLIST"), eq(itemId),
                eq("LINK_EVIDENCE"), eq(userId), any());
    }

    @Test
    void linkEvidence_duplicateEvidence_shouldNotAddTwice() {
        UUID evidenceId = UUID.randomUUID();
        CraChecklistItem item = createItem("AI-1.1", "SECURE_BY_DESIGN", "COMPLIANT");
        item.setEvidenceIds(new java.util.ArrayList<>(List.of(evidenceId)));
        UUID itemId = item.getId();
        when(checklistRepository.findByIdAndOrgId(itemId, orgId)).thenReturn(Optional.of(item));
        when(checklistRepository.save(any(CraChecklistItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CraChecklistItemResponse result = service.linkEvidence(itemId, evidenceId);

        assertEquals(1, result.evidenceIds().size());
    }

    private CraChecklistItem createItem(String ref, String category, String status) {
        CraChecklistItem item = new CraChecklistItem();
        item.setId(UUID.randomUUID());
        item.setOrgId(orgId);
        item.setProductId(productId);
        item.setRequirementRef(ref);
        item.setCategory(category);
        item.setTitle("Test requirement " + ref);
        item.setDescription("Description for " + ref);
        item.setStatus(status);
        item.setEvidenceIds(List.of());
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        return item;
    }
}
