package com.lexsecura.application.service;

import com.lexsecura.application.dto.*;
import com.lexsecura.domain.model.*;
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
class CraEventServiceTest {

    @Mock private CraEventRepository eventRepository;
    @Mock private CraEventParticipantRepository participantRepository;
    @Mock private CraEventLinkRepository linkRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AuditService auditService;

    private CraEventService service;

    private final UUID orgId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CraEventService(eventRepository, participantRepository,
                linkRepository, productRepository, auditService);
        TenantContext.setOrgId(orgId);
        TenantContext.setUserId(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ── CREATE ───────────────────────────────────────────────

    @Test
    void create_validRequest_shouldCreateEventAndOwnerParticipant() {
        Product product = new Product();
        product.setId(productId);
        product.setName("IoT Gateway");
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.of(product));

        when(eventRepository.save(any(CraEvent.class))).thenAnswer(inv -> {
            CraEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(participantRepository.save(any(CraEventParticipant.class))).thenAnswer(inv -> {
            CraEventParticipant p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(participantRepository.findAllByCraEventId(any())).thenReturn(List.of());
        when(linkRepository.findAllByCraEventId(any())).thenReturn(List.of());

        CraEventCreateRequest request = new CraEventCreateRequest(
                productId, "EXPLOITED_VULNERABILITY", "Log4Shell detected",
                "Critical CVE in Log4j", null, Instant.now());

        CraEventResponse response = service.create(request);

        assertNotNull(response.id());
        assertEquals("DRAFT", response.status());
        assertEquals("EXPLOITED_VULNERABILITY", response.eventType());
        assertEquals("Log4Shell detected", response.title());
        assertEquals(orgId, response.orgId());
        assertEquals(productId, response.productId());

        // Verify owner participant was created
        ArgumentCaptor<CraEventParticipant> participantCaptor = ArgumentCaptor.forClass(CraEventParticipant.class);
        verify(participantRepository).save(participantCaptor.capture());
        assertEquals("OWNER", participantCaptor.getValue().getRole());
        assertEquals(userId, participantCaptor.getValue().getUserId());

        // Verify audit recorded
        verify(auditService).record(eq(orgId), eq("CRA_EVENT"), any(), eq("CREATE"), eq(userId), anyMap());
    }

    @Test
    void create_invalidEventType_shouldThrow() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(new CraEventCreateRequest(
                        productId, "INVALID_TYPE", "Title", null, null, Instant.now())));
        assertTrue(ex.getMessage().contains("INVALID_TYPE"));
    }

    @Test
    void create_productNotFound_shouldThrow() {
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.create(new CraEventCreateRequest(
                        productId, "EXPLOITED_VULNERABILITY", "Title", null, null, Instant.now())));
    }

    // ── FIND ALL ─────────────────────────────────────────────

    @Test
    void findAll_noFilters_shouldReturnAllByOrg() {
        CraEvent event = buildEvent(UUID.randomUUID());
        when(eventRepository.findAllByOrgId(orgId)).thenReturn(List.of(event));
        when(participantRepository.findAllByCraEventId(any())).thenReturn(List.of());
        when(linkRepository.findAllByCraEventId(any())).thenReturn(List.of());
        when(productRepository.findByIdAndOrgId(any(), eq(orgId))).thenReturn(Optional.empty());

        List<CraEventResponse> results = service.findAll(null, null);
        assertEquals(1, results.size());
        verify(eventRepository).findAllByOrgId(orgId);
    }

    @Test
    void findAll_withProductIdFilter() {
        when(eventRepository.findAllByOrgIdAndProductId(orgId, productId)).thenReturn(List.of());

        service.findAll(productId, null);
        verify(eventRepository).findAllByOrgIdAndProductId(orgId, productId);
    }

    @Test
    void findAll_withStatusFilter() {
        when(eventRepository.findAllByOrgIdAndStatus(orgId, "DRAFT")).thenReturn(List.of());

        service.findAll(null, "DRAFT");
        verify(eventRepository).findAllByOrgIdAndStatus(orgId, "DRAFT");
    }

    @Test
    void findAll_withBothFilters() {
        when(eventRepository.findAllByOrgIdAndProductIdAndStatus(orgId, productId, "IN_REVIEW"))
                .thenReturn(List.of());

        service.findAll(productId, "IN_REVIEW");
        verify(eventRepository).findAllByOrgIdAndProductIdAndStatus(orgId, productId, "IN_REVIEW");
    }

    // ── FIND BY ID ───────────────────────────────────────────

    @Test
    void findById_found_shouldReturnResponse() {
        UUID eventId = UUID.randomUUID();
        CraEvent event = buildEvent(eventId);
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(participantRepository.findAllByCraEventId(eventId)).thenReturn(List.of());
        when(linkRepository.findAllByCraEventId(eventId)).thenReturn(List.of());
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());

        CraEventResponse response = service.findById(eventId);
        assertEquals(eventId, response.id());
    }

    @Test
    void findById_notFound_shouldThrow() {
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.findById(eventId));
    }

    // ── UPDATE ───────────────────────────────────────────────

    @Test
    void update_validFields_shouldUpdateAndAudit() {
        UUID eventId = UUID.randomUUID();
        CraEvent event = buildEvent(eventId);
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantRepository.findAllByCraEventId(eventId)).thenReturn(List.of());
        when(linkRepository.findAllByCraEventId(eventId)).thenReturn(List.of());
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());

        CraEventUpdateRequest request = new CraEventUpdateRequest(
                "Updated title", "New desc", "IN_REVIEW", null, null, null, null);

        CraEventResponse response = service.update(eventId, request);

        assertEquals("Updated title", response.title());
        assertEquals("IN_REVIEW", response.status());
        verify(auditService).record(eq(orgId), eq("CRA_EVENT"), eq(eventId), eq("UPDATE"), eq(userId), anyMap());
    }

    @Test
    void update_closedEvent_shouldThrow() {
        UUID eventId = UUID.randomUUID();
        CraEvent event = buildEvent(eventId);
        event.setStatus("CLOSED");
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        CraEventUpdateRequest request = new CraEventUpdateRequest("New title", null, null, null, null, null, null);

        assertThrows(IllegalStateException.class, () -> service.update(eventId, request));
    }

    @Test
    void update_invalidStatus_shouldThrow() {
        UUID eventId = UUID.randomUUID();
        CraEvent event = buildEvent(eventId);
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        CraEventUpdateRequest request = new CraEventUpdateRequest(null, null, "INVALID", null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> service.update(eventId, request));
    }

    // ── CLOSE ────────────────────────────────────────────────

    @Test
    void close_shouldSetStatusAndAudit() {
        UUID eventId = UUID.randomUUID();
        CraEvent event = buildEvent(eventId);
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantRepository.findAllByCraEventId(eventId)).thenReturn(List.of());
        when(linkRepository.findAllByCraEventId(eventId)).thenReturn(List.of());
        when(productRepository.findByIdAndOrgId(productId, orgId)).thenReturn(Optional.empty());

        CraEventResponse response = service.close(eventId);

        assertEquals("CLOSED", response.status());
        verify(auditService).record(eq(orgId), eq("CRA_EVENT"), eq(eventId), eq("CLOSE"), eq(userId), anyMap());
    }

    // ── ADD LINKS ────────────────────────────────────────────

    @Test
    void addLinks_shouldSaveLinksAndAudit() {
        UUID eventId = UUID.randomUUID();
        CraEvent event = buildEvent(eventId);
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(linkRepository.save(any(CraEventLink.class))).thenAnswer(inv -> {
            CraEventLink l = inv.getArgument(0);
            l.setId(UUID.randomUUID());
            return l;
        });

        UUID releaseId = UUID.randomUUID();
        UUID findingId = UUID.randomUUID();
        CraEventLinkRequest request = new CraEventLinkRequest(
                List.of(releaseId), List.of(findingId), List.of());

        service.addLinks(eventId, request);

        verify(linkRepository, times(2)).save(any(CraEventLink.class));
        verify(auditService).record(eq(orgId), eq("CRA_EVENT"), eq(eventId), eq("ADD_LINKS"), eq(userId), anyMap());
    }

    // ── ADD PARTICIPANT ──────────────────────────────────────

    @Test
    void addParticipant_newUser_shouldCreate() {
        UUID eventId = UUID.randomUUID();
        UUID participantUserId = UUID.randomUUID();
        CraEvent event = buildEvent(eventId);
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));
        when(participantRepository.findByCraEventIdAndUserId(eventId, participantUserId))
                .thenReturn(Optional.empty());
        when(participantRepository.save(any(CraEventParticipant.class))).thenAnswer(inv -> {
            CraEventParticipant p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        CraEventParticipantRequest request = new CraEventParticipantRequest(participantUserId, "APPROVER");
        CraEventParticipantResponse response = service.addParticipant(eventId, request);

        assertNotNull(response.id());
        assertEquals("APPROVER", response.role());
        assertEquals(participantUserId, response.userId());
    }

    @Test
    void addParticipant_existingUser_shouldUpdateRole() {
        UUID eventId = UUID.randomUUID();
        UUID participantUserId = UUID.randomUUID();
        CraEvent event = buildEvent(eventId);
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        CraEventParticipant existing = new CraEventParticipant();
        existing.setId(UUID.randomUUID());
        existing.setCraEventId(eventId);
        existing.setUserId(participantUserId);
        existing.setRole("VIEWER");
        existing.setCreatedAt(Instant.now());
        when(participantRepository.findByCraEventIdAndUserId(eventId, participantUserId))
                .thenReturn(Optional.of(existing));
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CraEventParticipantRequest request = new CraEventParticipantRequest(participantUserId, "OWNER");
        CraEventParticipantResponse response = service.addParticipant(eventId, request);

        assertEquals("OWNER", response.role());
    }

    @Test
    void addParticipant_invalidRole_shouldThrow() {
        UUID eventId = UUID.randomUUID();
        CraEvent event = buildEvent(eventId);
        when(eventRepository.findByIdAndOrgId(eventId, orgId)).thenReturn(Optional.of(event));

        CraEventParticipantRequest request = new CraEventParticipantRequest(UUID.randomUUID(), "SUPERADMIN");

        assertThrows(IllegalArgumentException.class, () -> service.addParticipant(eventId, request));
    }

    // ── HELPER ───────────────────────────────────────────────

    private CraEvent buildEvent(UUID id) {
        CraEvent event = new CraEvent();
        event.setId(id);
        event.setOrgId(orgId);
        event.setProductId(productId);
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setTitle("Test event");
        event.setDescription("Test description");
        event.setStatus("DRAFT");
        event.setDetectedAt(Instant.now());
        event.setCreatedBy(userId);
        event.setCreatedAt(Instant.now());
        event.setUpdatedAt(Instant.now());
        return event;
    }
}
