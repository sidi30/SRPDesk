package com.lexsecura.application.service;

import com.lexsecura.application.dto.*;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CraEventService {

    private static final Set<String> VALID_EVENT_TYPES = Set.of("EXPLOITED_VULNERABILITY", "SEVERE_INCIDENT");
    private static final Set<String> VALID_STATUSES = Set.of("DRAFT", "IN_REVIEW", "SUBMITTED", "CLOSED");
    private static final Set<String> VALID_PARTICIPANT_ROLES = Set.of("OWNER", "APPROVER", "VIEWER");

    private final CraEventRepository eventRepository;
    private final CraEventParticipantRepository participantRepository;
    private final CraEventLinkRepository linkRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public CraEventService(CraEventRepository eventRepository,
                           CraEventParticipantRepository participantRepository,
                           CraEventLinkRepository linkRepository,
                           ProductRepository productRepository,
                           AuditService auditService) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.linkRepository = linkRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    public CraEventResponse create(CraEventCreateRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        if (!VALID_EVENT_TYPES.contains(request.eventType())) {
            throw new IllegalArgumentException("Invalid event type: " + request.eventType());
        }

        productRepository.findByIdAndOrgId(request.productId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.productId()));

        CraEvent event = new CraEvent();
        event.setOrgId(orgId);
        event.setProductId(request.productId());
        event.setEventType(request.eventType());
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setStatus("DRAFT");
        event.setStartedAt(request.startedAt());
        event.setDetectedAt(request.detectedAt());
        event.setCreatedBy(userId);
        event.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        event.setUpdatedAt(event.getCreatedAt());
        event = eventRepository.save(event);

        // Auto-add creator as OWNER
        CraEventParticipant owner = new CraEventParticipant();
        owner.setCraEventId(event.getId());
        owner.setUserId(userId);
        owner.setRole("OWNER");
        owner.setCreatedAt(event.getCreatedAt());
        participantRepository.save(owner);

        auditService.record(orgId, "CRA_EVENT", event.getId(), "CREATE", userId,
                Map.of("title", event.getTitle(), "eventType", event.getEventType()));

        return toResponse(event);
    }

    @Transactional(readOnly = true)
    public List<CraEventResponse> findAll(UUID productId, String status) {
        UUID orgId = TenantContext.getOrgId();

        List<CraEvent> events;
        if (productId != null && status != null && !status.isBlank()) {
            events = eventRepository.findAllByOrgIdAndProductIdAndStatus(orgId, productId, status);
        } else if (productId != null) {
            events = eventRepository.findAllByOrgIdAndProductId(orgId, productId);
        } else if (status != null && !status.isBlank()) {
            events = eventRepository.findAllByOrgIdAndStatus(orgId, status);
        } else {
            events = eventRepository.findAllByOrgId(orgId);
        }

        return events.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CraEventResponse findById(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        CraEvent event = eventRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + id));
        return toResponse(event);
    }

    public CraEventResponse update(UUID id, CraEventUpdateRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        CraEvent event = eventRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + id));

        if ("CLOSED".equals(event.getStatus())) {
            throw new IllegalStateException("Cannot update a closed event");
        }

        if (request.title() != null) event.setTitle(request.title());
        if (request.description() != null) event.setDescription(request.description());
        if (request.startedAt() != null) event.setStartedAt(request.startedAt());
        if (request.detectedAt() != null) event.setDetectedAt(request.detectedAt());
        if (request.patchAvailableAt() != null) event.setPatchAvailableAt(request.patchAvailableAt());
        if (request.resolvedAt() != null) event.setResolvedAt(request.resolvedAt());

        if (request.status() != null && !request.status().isBlank()) {
            if (!VALID_STATUSES.contains(request.status())) {
                throw new IllegalArgumentException("Invalid status: " + request.status());
            }
            event.setStatus(request.status());
        }

        event.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        event = eventRepository.save(event);

        auditService.record(orgId, "CRA_EVENT", event.getId(), "UPDATE", userId,
                Map.of("title", event.getTitle(), "status", event.getStatus()));

        return toResponse(event);
    }

    public CraEventResponse close(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        CraEvent event = eventRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + id));

        event.setStatus("CLOSED");
        event.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        event = eventRepository.save(event);

        auditService.record(orgId, "CRA_EVENT", event.getId(), "CLOSE", userId,
                Map.of("title", event.getTitle()));

        return toResponse(event);
    }

    public void addLinks(UUID eventId, CraEventLinkRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        CraEvent event = eventRepository.findByIdAndOrgId(eventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + eventId));

        List<String> addedTypes = new ArrayList<>();

        if (request.releaseIds() != null) {
            for (UUID targetId : request.releaseIds()) {
                saveLink(event.getId(), "RELEASE", targetId);
            }
            if (!request.releaseIds().isEmpty()) addedTypes.add("RELEASE");
        }
        if (request.findingIds() != null) {
            for (UUID targetId : request.findingIds()) {
                saveLink(event.getId(), "FINDING", targetId);
            }
            if (!request.findingIds().isEmpty()) addedTypes.add("FINDING");
        }
        if (request.evidenceIds() != null) {
            for (UUID targetId : request.evidenceIds()) {
                saveLink(event.getId(), "EVIDENCE", targetId);
            }
            if (!request.evidenceIds().isEmpty()) addedTypes.add("EVIDENCE");
        }

        auditService.record(orgId, "CRA_EVENT", eventId, "ADD_LINKS", userId,
                Map.of("linkTypes", String.join(",", addedTypes)));
    }

    public CraEventParticipantResponse addParticipant(UUID eventId, CraEventParticipantRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        CraEvent event = eventRepository.findByIdAndOrgId(eventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + eventId));

        if (!VALID_PARTICIPANT_ROLES.contains(request.role())) {
            throw new IllegalArgumentException("Invalid role: " + request.role() +
                    ". Must be one of: " + VALID_PARTICIPANT_ROLES);
        }

        // Check if participant already exists and update role
        Optional<CraEventParticipant> existing = participantRepository.findByCraEventIdAndUserId(eventId, request.userId());
        CraEventParticipant participant;
        if (existing.isPresent()) {
            participant = existing.get();
            participant.setRole(request.role());
        } else {
            participant = new CraEventParticipant();
            participant.setCraEventId(eventId);
            participant.setUserId(request.userId());
            participant.setRole(request.role());
            participant.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        }
        participant = participantRepository.save(participant);

        auditService.record(orgId, "CRA_EVENT", eventId, "ADD_PARTICIPANT", userId,
                Map.of("participantUserId", request.userId().toString(), "role", request.role()));

        return new CraEventParticipantResponse(participant.getId(), participant.getUserId(),
                participant.getRole(), participant.getCreatedAt());
    }

    public CraEvent getEvent(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        return eventRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + id));
    }

    private void saveLink(UUID eventId, String linkType, UUID targetId) {
        CraEventLink link = new CraEventLink();
        link.setCraEventId(eventId);
        link.setLinkType(linkType);
        link.setTargetId(targetId);
        link.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        try {
            linkRepository.save(link);
        } catch (Exception e) {
            // Ignore duplicate link (unique constraint)
        }
    }

    private CraEventResponse toResponse(CraEvent e) {
        List<CraEventParticipant> participants = participantRepository.findAllByCraEventId(e.getId());
        List<CraEventLink> links = linkRepository.findAllByCraEventId(e.getId());

        String productName = productRepository.findByIdAndOrgId(e.getProductId(), e.getOrgId())
                .map(Product::getName)
                .orElse(null);

        return new CraEventResponse(
                e.getId(), e.getOrgId(), e.getProductId(), productName,
                e.getEventType(), e.getTitle(), e.getDescription(), e.getStatus(),
                e.getStartedAt(), e.getDetectedAt(), e.getPatchAvailableAt(), e.getResolvedAt(),
                e.getCreatedBy(), e.getCreatedAt(), e.getUpdatedAt(),
                participants.stream().map(p -> new CraEventParticipantResponse(
                        p.getId(), p.getUserId(), p.getRole(), p.getCreatedAt()
                )).collect(Collectors.toList()),
                links.stream().map(l -> new CraEventLinkResponse(
                        l.getId(), l.getLinkType(), l.getTargetId(), l.getCreatedAt()
                )).collect(Collectors.toList())
        );
    }
}
