package com.lexsecura.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lexsecura.application.dto.*;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SrpSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SrpSubmissionService.class);
    private static final Set<String> VALID_TYPES = Set.of("EARLY_WARNING", "NOTIFICATION", "FINAL_REPORT");

    private final SrpSubmissionRepository submissionRepository;
    private final CraEventRepository eventRepository;
    private final CraEventLinkRepository linkRepository;
    private final ProductRepository productRepository;
    private final ReleaseRepository releaseRepository;
    private final FindingRepository findingRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final ComponentRepository componentRepository;
    private final EvidenceRepository evidenceRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public SrpSubmissionService(SrpSubmissionRepository submissionRepository,
                                CraEventRepository eventRepository,
                                CraEventLinkRepository linkRepository,
                                ProductRepository productRepository,
                                ReleaseRepository releaseRepository,
                                FindingRepository findingRepository,
                                VulnerabilityRepository vulnerabilityRepository,
                                ComponentRepository componentRepository,
                                EvidenceRepository evidenceRepository,
                                AuditService auditService,
                                ObjectMapper objectMapper) {
        this.submissionRepository = submissionRepository;
        this.eventRepository = eventRepository;
        this.linkRepository = linkRepository;
        this.productRepository = productRepository;
        this.releaseRepository = releaseRepository;
        this.findingRepository = findingRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.componentRepository = componentRepository;
        this.evidenceRepository = evidenceRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public SrpSubmissionResponse create(UUID craEventId, SrpSubmissionCreateRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        if (!VALID_TYPES.contains(request.submissionType())) {
            throw new IllegalArgumentException("Invalid submission type: " + request.submissionType());
        }

        CraEvent event = eventRepository.findByIdAndOrgId(craEventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + craEventId));

        String contentJson = autofill(event, request.submissionType());

        SrpSubmission sub = new SrpSubmission();
        sub.setCraEventId(craEventId);
        sub.setSubmissionType(request.submissionType());
        sub.setStatus("DRAFT");
        sub.setContentJson(contentJson);
        sub.setSchemaVersion("1.0");
        sub.setGeneratedBy(userId);
        sub.setGeneratedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        sub.setUpdatedAt(sub.getGeneratedAt());
        sub = submissionRepository.save(sub);

        auditService.record(orgId, "SRP_SUBMISSION", sub.getId(), "CREATE", userId,
                Map.of("craEventId", craEventId.toString(), "type", request.submissionType()));

        return toResponse(sub);
    }

    @Transactional(readOnly = true)
    public List<SrpSubmissionResponse> findAll(UUID craEventId) {
        UUID orgId = TenantContext.getOrgId();
        eventRepository.findByIdAndOrgId(craEventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + craEventId));

        return submissionRepository.findAllByCraEventId(craEventId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SrpSubmissionResponse findById(UUID craEventId, UUID subId) {
        UUID orgId = TenantContext.getOrgId();
        eventRepository.findByIdAndOrgId(craEventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + craEventId));

        SrpSubmission sub = submissionRepository.findByIdAndCraEventId(subId, craEventId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + subId));
        return toResponse(sub);
    }

    public SrpSubmissionResponse validate(UUID craEventId, UUID subId) {
        UUID orgId = TenantContext.getOrgId();
        eventRepository.findByIdAndOrgId(craEventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + craEventId));

        SrpSubmission sub = submissionRepository.findByIdAndCraEventId(subId, craEventId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + subId));

        List<String> errors = validateContent(sub);
        try {
            sub.setValidationErrors(objectMapper.writeValueAsString(errors));
        } catch (Exception e) {
            log.error("Failed to serialize validation errors", e);
        }
        sub.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        sub = submissionRepository.save(sub);

        return toResponse(sub);
    }

    public SrpSubmissionResponse markReady(UUID craEventId, UUID subId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        eventRepository.findByIdAndOrgId(craEventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + craEventId));

        SrpSubmission sub = submissionRepository.findByIdAndCraEventId(subId, craEventId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + subId));

        List<String> errors = validateContent(sub);
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Cannot mark as ready, validation errors: " + String.join("; ", errors));
        }

        sub.setStatus("READY");
        sub.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        sub = submissionRepository.save(sub);

        auditService.record(orgId, "SRP_SUBMISSION", sub.getId(), "MARK_READY", userId,
                Map.of("type", sub.getSubmissionType()));

        return toResponse(sub);
    }

    public SrpSubmissionResponse markSubmitted(UUID craEventId, UUID subId, MarkSubmittedRequest request,
                                               UUID ackEvidenceId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        eventRepository.findByIdAndOrgId(craEventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + craEventId));

        SrpSubmission sub = submissionRepository.findByIdAndCraEventId(subId, craEventId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + subId));

        sub.setStatus("SUBMITTED");
        sub.setSubmittedReference(request.reference());
        sub.setSubmittedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        if (ackEvidenceId != null) {
            sub.setAcknowledgmentEvidenceId(ackEvidenceId);
        }
        sub.setUpdatedAt(sub.getSubmittedAt());
        sub = submissionRepository.save(sub);

        auditService.record(orgId, "SRP_SUBMISSION", sub.getId(), "MARK_SUBMITTED", userId,
                Map.of("type", sub.getSubmissionType(), "reference", request.reference()));

        return toResponse(sub);
    }

    public SrpSubmission getSubmission(UUID craEventId, UUID subId) {
        UUID orgId = TenantContext.getOrgId();
        eventRepository.findByIdAndOrgId(craEventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + craEventId));
        return submissionRepository.findByIdAndCraEventId(subId, craEventId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + subId));
    }

    public void markExported(SrpSubmission sub) {
        if ("DRAFT".equals(sub.getStatus())) {
            return;
        }
        sub.setStatus("EXPORTED");
        sub.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        submissionRepository.save(sub);
    }

    private String autofill(CraEvent event, String submissionType) {
        try {
            ObjectNode content = objectMapper.createObjectNode();

            UUID orgId = event.getOrgId();

            // Product info
            Product product = productRepository.findByIdAndOrgId(event.getProductId(), orgId).orElse(null);
            if (product != null) {
                ObjectNode productNode = content.putObject("product");
                productNode.put("name", product.getName());
                productNode.put("type", product.getType());
                productNode.put("criticality", product.getCriticality());
            }

            // Event info
            ObjectNode eventNode = content.putObject("event");
            eventNode.put("type", event.getEventType());
            eventNode.put("title", event.getTitle());
            if (event.getDescription() != null) eventNode.put("description", event.getDescription());
            eventNode.put("detectedAt", event.getDetectedAt().toString());
            if (event.getStartedAt() != null) eventNode.put("startedAt", event.getStartedAt().toString());

            // Linked releases
            List<CraEventLink> releaseLinks = linkRepository.findAllByCraEventIdAndLinkType(event.getId(), "RELEASE");
            if (!releaseLinks.isEmpty()) {
                ArrayNode releases = content.putArray("affectedReleases");
                for (CraEventLink link : releaseLinks) {
                    releaseRepository.findByIdAndOrgId(link.getTargetId(), orgId).ifPresent(r -> {
                        ObjectNode rn = releases.addObject();
                        rn.put("version", r.getVersion());
                        rn.put("status", r.getStatus().name());
                    });
                }
            }

            // Linked findings
            // Chain of trust: findings are accessed via CraEventLink which belongs to a CraEvent
            // already validated against orgId. The linked finding, its vulnerability, and its
            // component are org-scoped through this validated event chain. Vulnerabilities and
            // components are global reference data without org_id columns, so findById is correct.
            List<CraEventLink> findingLinks = linkRepository.findAllByCraEventIdAndLinkType(event.getId(), "FINDING");
            if (!findingLinks.isEmpty()) {
                ArrayNode findings = content.putArray("vulnerabilities");
                for (CraEventLink link : findingLinks) {
                    findingRepository.findById(link.getTargetId()).ifPresent(f -> {
                        ObjectNode fn = findings.addObject();
                        fn.put("status", f.getStatus());
                        fn.put("detectedAt", f.getDetectedAt().toString());
                        vulnerabilityRepository.findById(f.getVulnerabilityId()).ifPresent(v -> {
                            fn.put("osvId", v.getOsvId());
                            fn.put("severity", v.getSeverity());
                            if (v.getSummary() != null) fn.put("summary", v.getSummary());
                            // EUVD & monitoring enrichment (Phase 2.6-2.7)
                            if (v.getEuvdId() != null) fn.put("euvdId", v.getEuvdId());
                            if (v.getCvssScore() != null) fn.put("cvssScore", v.getCvssScore().doubleValue());
                            if (v.getCvssVector() != null) fn.put("cvssVector", v.getCvssVector());
                            fn.put("activelyExploited", v.isActivelyExploited());
                            if (v.getKevDateAdded() != null) fn.put("kevDateAdded", v.getKevDateAdded().toString());
                        });
                        componentRepository.findById(f.getComponentId()).ifPresent(c -> {
                            fn.put("componentName", c.getName());
                            fn.put("componentPurl", c.getPurl());
                        });
                    });
                }
            }

            // Linked evidences
            List<CraEventLink> evidenceLinks = linkRepository.findAllByCraEventIdAndLinkType(event.getId(), "EVIDENCE");
            if (!evidenceLinks.isEmpty()) {
                ArrayNode evidences = content.putArray("evidences");
                for (CraEventLink link : evidenceLinks) {
                    evidenceRepository.findByIdAndOrgId(link.getTargetId(), orgId).ifPresent(e -> {
                        ObjectNode en = evidences.addObject();
                        en.put("filename", e.getFilename());
                        en.put("type", e.getType().name());
                        en.put("sha256", e.getSha256());
                    });
                }
            }

            // For NOTIFICATION and FINAL_REPORT, add timeline
            if (!"EARLY_WARNING".equals(submissionType)) {
                ObjectNode timeline = content.putObject("timeline");
                timeline.put("detectedAt", event.getDetectedAt().toString());
                if (event.getStartedAt() != null) timeline.put("startedAt", event.getStartedAt().toString());
                if (event.getPatchAvailableAt() != null) timeline.put("patchAvailableAt", event.getPatchAvailableAt().toString());
                if (event.getResolvedAt() != null) timeline.put("resolvedAt", event.getResolvedAt().toString());
            }

            // Contact info
            if (product != null && product.getContacts() != null && !product.getContacts().isEmpty()) {
                ArrayNode contacts = content.putArray("contacts");
                for (Map<String, String> c : product.getContacts()) {
                    ObjectNode cn = contacts.addObject();
                    c.forEach(cn::put);
                }
            }

            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            log.error("Autofill failed for event {}", event.getId(), e);
            return "{}";
        }
    }

    private List<String> validateContent(SrpSubmission sub) {
        List<String> errors = new ArrayList<>();

        try {
            Map<String, Object> content = objectMapper.readValue(sub.getContentJson(),
                    new TypeReference<Map<String, Object>>() {});

            // Load schema
            Map<String, Object> schema = loadSchema(sub.getSubmissionType());
            if (schema == null) {
                errors.add("No validation schema found for type: " + sub.getSubmissionType());
                return errors;
            }

            @SuppressWarnings("unchecked")
            List<String> required = (List<String>) schema.get("required");
            if (required != null) {
                for (String field : required) {
                    if (!content.containsKey(field) || content.get(field) == null) {
                        errors.add("Missing required field: " + field);
                    }
                }
            }

            // Validate nested required fields
            @SuppressWarnings("unchecked")
            Map<String, List<String>> nestedRequired = (Map<String, List<String>>) schema.get("nestedRequired");
            if (nestedRequired != null) {
                for (var entry : nestedRequired.entrySet()) {
                    Object parent = content.get(entry.getKey());
                    if (parent instanceof Map<?, ?> parentMap) {
                        for (String field : entry.getValue()) {
                            if (!parentMap.containsKey(field) || parentMap.get(field) == null) {
                                errors.add("Missing required field: " + entry.getKey() + "." + field);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            errors.add("Failed to parse content: " + e.getMessage());
        }

        return errors;
    }

    private Map<String, Object> loadSchema(String submissionType) {
        String path = "schemas/" + submissionType.toLowerCase() + "_v1.json";
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Could not load schema from {}", path, e);
            return null;
        }
    }

    private SrpSubmissionResponse toResponse(SrpSubmission s) {
        Object content = null;
        Object valErrors = null;
        try {
            if (s.getContentJson() != null) content = objectMapper.readTree(s.getContentJson());
            if (s.getValidationErrors() != null) valErrors = objectMapper.readTree(s.getValidationErrors());
        } catch (Exception e) {
            content = s.getContentJson();
            valErrors = s.getValidationErrors();
        }

        return new SrpSubmissionResponse(
                s.getId(), s.getCraEventId(), s.getSubmissionType(), s.getStatus(),
                content, s.getSchemaVersion(), valErrors,
                s.getSubmittedReference(), s.getSubmittedAt(), s.getAcknowledgmentEvidenceId(),
                s.getGeneratedBy(), s.getGeneratedAt(), s.getUpdatedAt()
        );
    }
}
