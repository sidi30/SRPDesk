package com.lexsecura.application.service;

import com.lexsecura.application.dto.FindingDecisionRequest;
import com.lexsecura.application.dto.FindingDecisionResponse;
import com.lexsecura.application.dto.FindingResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FindingService {

    private final FindingRepository findingRepository;
    private final FindingDecisionRepository decisionRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final ComponentRepository componentRepository;
    private final ReleaseRepository releaseRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public FindingService(FindingRepository findingRepository,
                          FindingDecisionRepository decisionRepository,
                          VulnerabilityRepository vulnerabilityRepository,
                          ComponentRepository componentRepository,
                          ReleaseRepository releaseRepository,
                          AuditService auditService,
                          ObjectMapper objectMapper) {
        this.findingRepository = findingRepository;
        this.decisionRepository = decisionRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.componentRepository = componentRepository;
        this.releaseRepository = releaseRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<FindingResponse> findByProductId(UUID productId, String status) {
        List<Release> releases = releaseRepository.findAllByProductId(productId);
        List<FindingResponse> results = new ArrayList<>();

        for (Release release : releases) {
            List<Finding> findings = (status != null && !status.isBlank())
                    ? findingRepository.findAllByReleaseIdAndStatus(release.getId(), status)
                    : findingRepository.findAllByReleaseId(release.getId());

            for (Finding finding : findings) {
                results.add(toResponse(finding));
            }
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<FindingResponse> findByReleaseId(UUID releaseId, String status) {
        List<Finding> findings = (status != null && !status.isBlank())
                ? findingRepository.findAllByReleaseIdAndStatus(releaseId, status)
                : findingRepository.findAllByReleaseId(releaseId);

        return findings.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public FindingDecisionResponse addDecision(UUID findingId, FindingDecisionRequest request) {
        UUID userId = TenantContext.getUserId();
        UUID orgId = TenantContext.getOrgId();

        Finding finding = findingRepository.findById(findingId)
                .orElseThrow(() -> new EntityNotFoundException("Finding not found: " + findingId));

        Set<String> validTypes = Set.of("NOT_AFFECTED", "PATCH_PLANNED", "MITIGATED", "FIXED");
        if (!validTypes.contains(request.decisionType())) {
            throw new IllegalArgumentException("Invalid decision type: " + request.decisionType()
                    + ". Must be one of: " + validTypes);
        }

        FindingDecision decision = new FindingDecision();
        decision.setFindingId(findingId);
        decision.setDecisionType(request.decisionType());
        decision.setRationale(request.rationale());
        decision.setDueDate(request.dueDate());
        decision.setDecidedBy(userId);
        decision.setFixReleaseId(request.fixReleaseId());
        decision.setCreatedAt(Instant.now());
        decision = decisionRepository.save(decision);

        // Update finding status based on decision
        finding.setStatus(request.decisionType());
        findingRepository.save(finding);

        auditService.record(orgId, "FINDING_DECISION", findingId, "DECIDE", userId,
                Map.of("decisionType", request.decisionType(), "rationale", request.rationale()));

        return toDecisionResponse(decision);
    }

    private FindingResponse toResponse(Finding f) {
        Vulnerability vuln = vulnerabilityRepository.findById(f.getVulnerabilityId()).orElse(null);
        Component comp = componentRepository.findById(f.getComponentId()).orElse(null);
        List<FindingDecision> decisions = decisionRepository.findAllByFindingId(f.getId());

        String details = vuln != null ? vuln.getDetails() : null;
        List<String> aliases = parseAliases(vuln != null ? vuln.getAliases() : null);
        Instant publishedAt = vuln != null ? vuln.getPublished() : null;
        String osvId = vuln != null ? vuln.getOsvId() : null;
        String osvUrl = osvId != null ? "https://osv.dev/vulnerability/" + osvId : null;

        return new FindingResponse(
                f.getId(), f.getReleaseId(), f.getComponentId(),
                comp != null ? comp.getName() : null,
                comp != null ? comp.getPurl() : null,
                f.getVulnerabilityId(),
                osvId,
                vuln != null ? vuln.getSummary() : null,
                details,
                vuln != null ? vuln.getSeverity() : null,
                aliases,
                publishedAt,
                osvUrl,
                f.getStatus(), f.getDetectedAt(), f.getSource(),
                decisions.stream().map(this::toDecisionResponse).collect(Collectors.toList()));
    }

    private List<String> parseAliases(String aliasesJson) {
        if (aliasesJson == null || aliasesJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(aliasesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private FindingDecisionResponse toDecisionResponse(FindingDecision d) {
        return new FindingDecisionResponse(
                d.getId(), d.getDecisionType(), d.getRationale(),
                d.getDueDate(), d.getDecidedBy(), d.getFixReleaseId(), d.getCreatedAt());
    }
}
