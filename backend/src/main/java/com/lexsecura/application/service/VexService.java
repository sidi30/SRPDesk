package com.lexsecura.application.service;

import com.lexsecura.application.dto.VexDocumentResponse;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.vex.*;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import com.lexsecura.infrastructure.vex.VexDocumentGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class VexService {

    private static final Logger log = LoggerFactory.getLogger(VexService.class);

    private final VexDocumentRepository vexDocumentRepository;
    private final VexStatementRepository vexStatementRepository;
    private final ReleaseRepository releaseRepository;
    private final ProductRepository productRepository;
    private final FindingRepository findingRepository;
    private final FindingDecisionRepository decisionRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final AuditService auditService;
    private final Map<VexFormat, VexDocumentGenerator> generators;

    public VexService(VexDocumentRepository vexDocumentRepository,
                      VexStatementRepository vexStatementRepository,
                      ReleaseRepository releaseRepository,
                      ProductRepository productRepository,
                      FindingRepository findingRepository,
                      FindingDecisionRepository decisionRepository,
                      VulnerabilityRepository vulnerabilityRepository,
                      AuditService auditService,
                      List<VexDocumentGenerator> generatorList) {
        this.vexDocumentRepository = vexDocumentRepository;
        this.vexStatementRepository = vexStatementRepository;
        this.releaseRepository = releaseRepository;
        this.productRepository = productRepository;
        this.findingRepository = findingRepository;
        this.decisionRepository = decisionRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.auditService = auditService;

        this.generators = new HashMap<>();
        for (VexDocumentGenerator gen : generatorList) {
            generators.put(gen.supportedFormat(), gen);
        }
    }

    public VexDocumentResponse generateVexDocument(UUID releaseId, VexFormat format) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        Release release = releaseRepository.findByIdAndOrgId(releaseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found: " + releaseId));
        Product product = productRepository.findByIdAndOrgId(release.getProductId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        VexDocumentGenerator generator = generators.get(format);
        if (generator == null) {
            throw new IllegalArgumentException("Unsupported VEX format: " + format);
        }

        // Collect findings with decisions for this release
        List<Finding> findings = findingRepository.findAllByReleaseId(releaseId);
        List<VexStatement> statements = new ArrayList<>();

        for (Finding finding : findings) {
            List<FindingDecision> decisions = decisionRepository.findAllByFindingId(finding.getId());
            if (decisions.isEmpty()) continue;

            FindingDecision latestDecision = decisions.get(decisions.size() - 1);
            Vulnerability vuln = vulnerabilityRepository.findById(finding.getVulnerabilityId()).orElse(null);
            String cveId = vuln != null ? vuln.getOsvId() : "UNKNOWN";

            VexStatement stmt = new VexStatement();
            stmt.setFindingId(finding.getId());
            stmt.setDecisionId(latestDecision.getId());
            stmt.setVulnerabilityId(cveId);
            stmt.setProductId(product.getId());
            stmt.setStatus(mapDecisionToVexStatus(latestDecision.getDecisionType()));
            stmt.setJustification(mapDecisionToJustification(latestDecision.getDecisionType()));
            stmt.setImpactStatement(latestDecision.getRationale());

            if ("PATCH_PLANNED".equals(latestDecision.getDecisionType()) && latestDecision.getDueDate() != null) {
                stmt.setActionStatement("Patch planned for " + latestDecision.getDueDate());
            }

            stmt.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
            statements.add(stmt);
        }

        // Generate document JSON
        String documentJson = generator.generate(product, release, statements);
        String sha256 = sha256(documentJson);

        // Save VEX document
        VexDocument vexDoc = new VexDocument(orgId, releaseId, format, documentJson, sha256,
                userId != null ? userId.toString() : "AUTO");
        vexDoc = vexDocumentRepository.save(vexDoc);

        // Save statements
        for (VexStatement stmt : statements) {
            stmt.setVexDocumentId(vexDoc.getId());
            vexStatementRepository.save(stmt);
        }

        auditService.record(orgId, "VEX_DOCUMENT", vexDoc.getId(), "GENERATE", userId,
                Map.of("format", format.name(), "statements", statements.size(),
                        "releaseId", releaseId.toString()));

        log.info("Generated VEX document {} ({}) with {} statements for release {}",
                vexDoc.getId(), format, statements.size(), releaseId);

        return toResponse(vexDoc, statements.size());
    }

    @Transactional(readOnly = true)
    public List<VexDocumentResponse> listByRelease(UUID releaseId) {
        UUID orgId = TenantContext.getOrgId();
        releaseRepository.findByIdAndOrgId(releaseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found: " + releaseId));

        return vexDocumentRepository.findAllByReleaseIdAndOrgId(releaseId, orgId).stream()
                .map(doc -> {
                    int count = vexStatementRepository.findAllByVexDocumentId(doc.getId()).size();
                    return toResponse(doc, count);
                }).toList();
    }

    @Transactional(readOnly = true)
    public VexDocumentResponse getById(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        VexDocument doc = vexDocumentRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("VEX document not found: " + id));
        int count = vexStatementRepository.findAllByVexDocumentId(id).size();
        return toResponse(doc, count);
    }

    @Transactional(readOnly = true)
    public String downloadDocument(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        VexDocument doc = vexDocumentRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("VEX document not found: " + id));
        return doc.getDocumentJson();
    }

    public VexDocumentResponse publish(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        VexDocument doc = vexDocumentRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("VEX document not found: " + id));

        if (!"DRAFT".equals(doc.getStatus())) {
            throw new IllegalStateException("Only DRAFT documents can be published. Current status: " + doc.getStatus());
        }

        doc.setStatus("PUBLISHED");
        doc.setPublishedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        doc.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        doc = vexDocumentRepository.save(doc);

        auditService.record(orgId, "VEX_DOCUMENT", doc.getId(), "PUBLISH", userId,
                Map.of("format", doc.getFormat().name()));

        int count = vexStatementRepository.findAllByVexDocumentId(id).size();
        return toResponse(doc, count);
    }

    public void delete(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        VexDocument doc = vexDocumentRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("VEX document not found: " + id));

        if (!"DRAFT".equals(doc.getStatus())) {
            throw new IllegalStateException("Only DRAFT documents can be deleted. Current status: " + doc.getStatus());
        }

        vexStatementRepository.deleteAllByVexDocumentId(id);
        vexDocumentRepository.deleteById(id);
    }

    private VexStatus mapDecisionToVexStatus(String decisionType) {
        return switch (decisionType) {
            case "NOT_AFFECTED" -> VexStatus.not_affected;
            case "MITIGATED" -> VexStatus.not_affected;
            case "FIXED" -> VexStatus.fixed;
            case "PATCH_PLANNED" -> VexStatus.affected;
            default -> VexStatus.under_investigation;
        };
    }

    private VexJustification mapDecisionToJustification(String decisionType) {
        return switch (decisionType) {
            case "NOT_AFFECTED" -> VexJustification.vulnerable_code_not_reachable;
            case "MITIGATED" -> VexJustification.inline_mitigations_already_exist;
            default -> null;
        };
    }

    private VexDocumentResponse toResponse(VexDocument doc, int statementCount) {
        return new VexDocumentResponse(
                doc.getId(), doc.getReleaseId(), doc.getFormat().name(),
                doc.getVersion(), doc.getStatus(), doc.getSha256Hash(),
                doc.getGeneratedBy(), statementCount,
                doc.getPublishedAt(), doc.getCreatedAt(), doc.getUpdatedAt()
        );
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "error";
        }
    }
}
