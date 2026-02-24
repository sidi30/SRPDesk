package com.lexsecura.extras.requirements.service;

import com.lexsecura.application.dto.CraChecklistItemResponse;
import com.lexsecura.application.dto.CraChecklistSummaryResponse;
import com.lexsecura.application.dto.CraChecklistUpdateRequest;
import com.lexsecura.application.service.AuditService;
import com.lexsecura.application.service.EntityNotFoundException;
import com.lexsecura.domain.model.CraChecklistItem;
import com.lexsecura.domain.repository.CraChecklistRepository;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CraChecklistService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "NOT_ASSESSED", "COMPLIANT", "PARTIALLY_COMPLIANT", "NON_COMPLIANT", "NOT_APPLICABLE"
    );

    private static final List<String[]> ANNEX_I_REQUIREMENTS = List.of(
            // Part I - SECURE_BY_DESIGN (13 items)
            new String[]{"AI-1.1", "SECURE_BY_DESIGN", "Security by design and default",
                    "Products shall be designed, developed and produced to ensure an appropriate level of cybersecurity based on the risks."},
            new String[]{"AI-1.2", "SECURE_BY_DESIGN", "No known exploitable vulnerabilities",
                    "Products shall be delivered without any known exploitable vulnerabilities."},
            new String[]{"AI-1.3", "SECURE_BY_DESIGN", "Secure default configuration",
                    "Products shall be made available with a secure by default configuration, including the possibility to reset to the original state."},
            new String[]{"AI-1.4", "SECURE_BY_DESIGN", "Protection against unauthorized access",
                    "Products shall ensure protection from unauthorized access by appropriate control mechanisms."},
            new String[]{"AI-1.5", "SECURE_BY_DESIGN", "Data confidentiality",
                    "Products shall protect the confidentiality of stored, transmitted or otherwise processed data."},
            new String[]{"AI-1.6", "SECURE_BY_DESIGN", "Data integrity",
                    "Products shall protect the integrity of stored, transmitted or otherwise processed data, commands, programs and configuration."},
            new String[]{"AI-1.7", "SECURE_BY_DESIGN", "Data minimisation",
                    "Products shall process only data that are adequate, relevant and limited to what is necessary (data minimisation)."},
            new String[]{"AI-1.8", "SECURE_BY_DESIGN", "Availability and resilience",
                    "Products shall be designed to ensure availability of essential functions, including resilience and mitigation against denial of service attacks."},
            new String[]{"AI-1.9", "SECURE_BY_DESIGN", "Minimise negative impact",
                    "Products shall minimise their own negative impact on the availability of services provided by other devices or networks."},
            new String[]{"AI-1.10", "SECURE_BY_DESIGN", "Minimise attack surfaces",
                    "Products shall be designed to reduce attack surfaces, including external interfaces."},
            new String[]{"AI-1.11", "SECURE_BY_DESIGN", "Incident impact mitigation",
                    "Products shall be designed to mitigate the impact of security incidents using appropriate exploitation mitigation mechanisms and techniques."},
            new String[]{"AI-1.12", "SECURE_BY_DESIGN", "Security logging",
                    "Products shall provide security-relevant information by recording and/or monitoring relevant internal activity."},
            new String[]{"AI-1.13", "SECURE_BY_DESIGN", "Secure update mechanism",
                    "Products shall ensure that vulnerabilities can be addressed through security updates, including automatic updates where applicable."},

            // Part II - VULNERABILITY_MANAGEMENT (8 items)
            new String[]{"AII-2.1", "VULNERABILITY_MANAGEMENT", "Identify and document vulnerabilities",
                    "Identify and document vulnerabilities and components contained in the product, including an SBOM."},
            new String[]{"AII-2.2", "VULNERABILITY_MANAGEMENT", "Address and remediate vulnerabilities",
                    "Address and remediate vulnerabilities without delay, including by providing security updates."},
            new String[]{"AII-2.3", "VULNERABILITY_MANAGEMENT", "Regular testing and review",
                    "Apply effective and regular tests and reviews of the security of the product."},
            new String[]{"AII-2.4", "VULNERABILITY_MANAGEMENT", "Public disclosure",
                    "Once a security update is available, publicly disclose information about fixed vulnerabilities with a CVE identifier."},
            new String[]{"AII-2.5", "VULNERABILITY_MANAGEMENT", "Coordinated vulnerability disclosure",
                    "Put in place and enforce a policy on coordinated vulnerability disclosure."},
            new String[]{"AII-2.6", "VULNERABILITY_MANAGEMENT", "Vulnerability information sharing",
                    "Take measures to facilitate the sharing of information about potential vulnerabilities."},
            new String[]{"AII-2.7", "VULNERABILITY_MANAGEMENT", "Secure distribution of fixes",
                    "Provide mechanisms to securely distribute fixes and security updates for the product."},
            new String[]{"AII-2.8", "VULNERABILITY_MANAGEMENT", "Free security updates",
                    "Ensure that where security patches or updates are available, they are disseminated without delay and free of charge with advisory messages."}
    );

    private final CraChecklistRepository checklistRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public CraChecklistService(CraChecklistRepository checklistRepository,
                               ProductRepository productRepository,
                               AuditService auditService) {
        this.checklistRepository = checklistRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    public List<CraChecklistItemResponse> initializeChecklist(UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        productRepository.findByIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        if (checklistRepository.countByProductIdAndOrgId(productId, orgId) > 0) {
            throw new IllegalStateException("Checklist already initialized for product: " + productId);
        }

        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        List<CraChecklistItem> items = new ArrayList<>();

        for (String[] req : ANNEX_I_REQUIREMENTS) {
            CraChecklistItem item = new CraChecklistItem();
            item.setOrgId(orgId);
            item.setProductId(productId);
            item.setRequirementRef(req[0]);
            item.setCategory(req[1]);
            item.setTitle(req[2]);
            item.setDescription(req[3]);
            item.setStatus("NOT_ASSESSED");
            item.setEvidenceIds(List.of());
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            items.add(checklistRepository.save(item));
        }

        auditService.record(orgId, "CRA_CHECKLIST", productId, "INITIALIZE", userId,
                Map.of("items", items.size()));

        return items.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CraChecklistItemResponse> findAll(UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        return checklistRepository.findAllByProductIdAndOrgId(productId, orgId)
                .stream().map(this::toResponse).toList();
    }

    public CraChecklistItemResponse update(UUID itemId, CraChecklistUpdateRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        CraChecklistItem item = checklistRepository.findByIdAndOrgId(itemId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Checklist item not found: " + itemId));

        if (request.status() != null) {
            if (!VALID_STATUSES.contains(request.status())) {
                throw new IllegalArgumentException("Invalid status: " + request.status());
            }
            item.setStatus(request.status());
        }
        if (request.notes() != null) {
            item.setNotes(request.notes());
        }
        if (request.evidenceIds() != null) {
            item.setEvidenceIds(request.evidenceIds());
        }

        item.setAssessedBy(userId);
        item.setAssessedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        item.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        item = checklistRepository.save(item);

        auditService.record(orgId, "CRA_CHECKLIST", item.getId(), "UPDATE", userId,
                Map.of("requirementRef", item.getRequirementRef(), "status", item.getStatus()));

        return toResponse(item);
    }

    @Transactional(readOnly = true)
    public CraChecklistSummaryResponse getSummary(UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        List<CraChecklistItem> items = checklistRepository.findAllByProductIdAndOrgId(productId, orgId);

        if (items.isEmpty()) {
            return new CraChecklistSummaryResponse(productId, 0, 0, 0, 0, 0, Map.of());
        }

        Map<String, List<CraChecklistItem>> byCategory = items.stream()
                .collect(Collectors.groupingBy(CraChecklistItem::getCategory));

        Map<String, CraChecklistSummaryResponse.CategorySummary> categories = new LinkedHashMap<>();
        for (var entry : byCategory.entrySet()) {
            categories.put(entry.getKey(), buildCategorySummary(entry.getValue()));
        }

        return new CraChecklistSummaryResponse(
                productId,
                items.size(),
                countByStatus(items, "COMPLIANT"),
                countByStatus(items, "PARTIALLY_COMPLIANT"),
                countByStatus(items, "NON_COMPLIANT"),
                countByStatus(items, "NOT_ASSESSED"),
                categories
        );
    }

    public CraChecklistItemResponse linkEvidence(UUID itemId, UUID evidenceId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        CraChecklistItem item = checklistRepository.findByIdAndOrgId(itemId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Checklist item not found: " + itemId));

        List<UUID> ids = new ArrayList<>(item.getEvidenceIds() != null ? item.getEvidenceIds() : List.of());
        if (!ids.contains(evidenceId)) {
            ids.add(evidenceId);
        }
        item.setEvidenceIds(ids);
        item.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        item = checklistRepository.save(item);

        auditService.record(orgId, "CRA_CHECKLIST", item.getId(), "LINK_EVIDENCE", userId,
                Map.of("evidenceId", evidenceId.toString()));

        return toResponse(item);
    }

    private CraChecklistItemResponse toResponse(CraChecklistItem item) {
        return new CraChecklistItemResponse(
                item.getId(), item.getProductId(), item.getRequirementRef(),
                item.getCategory(), item.getTitle(), item.getDescription(),
                item.getStatus(), item.getEvidenceIds(), item.getNotes(),
                item.getAssessedBy(), item.getAssessedAt(),
                item.getCreatedAt(), item.getUpdatedAt()
        );
    }

    private int countByStatus(List<CraChecklistItem> items, String status) {
        return (int) items.stream().filter(i -> status.equals(i.getStatus())).count();
    }

    private CraChecklistSummaryResponse.CategorySummary buildCategorySummary(List<CraChecklistItem> items) {
        return new CraChecklistSummaryResponse.CategorySummary(
                items.size(),
                countByStatus(items, "COMPLIANT"),
                countByStatus(items, "PARTIALLY_COMPLIANT"),
                countByStatus(items, "NON_COMPLIANT"),
                countByStatus(items, "NOT_ASSESSED")
        );
    }
}
