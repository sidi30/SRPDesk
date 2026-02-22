package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional(readOnly = true)
public class CompliancePackService {

    private final ProductRepository productRepository;
    private final ReleaseRepository releaseRepository;
    private final EvidenceRepository evidenceRepository;
    private final ComponentRepository componentRepository;
    private final FindingRepository findingRepository;
    private final FindingDecisionRepository decisionRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final AuditEventRepository auditEventRepository;
    private final PdfReportGenerator pdfGenerator;
    private final ObjectMapper objectMapper;

    public CompliancePackService(
            ProductRepository productRepository,
            ReleaseRepository releaseRepository,
            EvidenceRepository evidenceRepository,
            ComponentRepository componentRepository,
            FindingRepository findingRepository,
            FindingDecisionRepository decisionRepository,
            VulnerabilityRepository vulnerabilityRepository,
            AuditEventRepository auditEventRepository,
            PdfReportGenerator pdfGenerator,
            ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.releaseRepository = releaseRepository;
        this.evidenceRepository = evidenceRepository;
        this.componentRepository = componentRepository;
        this.findingRepository = findingRepository;
        this.decisionRepository = decisionRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.auditEventRepository = auditEventRepository;
        this.pdfGenerator = pdfGenerator;
        this.objectMapper = objectMapper.copy()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void generatePack(UUID releaseId, OutputStream outputStream) throws IOException {
        UUID orgId = TenantContext.getOrgId();

        Release release = releaseRepository.findByIdAndOrgId(releaseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found: " + releaseId));

        Product product = productRepository.findByIdAndOrgId(release.getProductId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        List<Evidence> evidences = evidenceRepository.findAllByReleaseIdAndOrgId(releaseId, orgId);
        // Chain of trust: components and findings are scoped to a releaseId that was already
        // validated against orgId above (findByIdAndOrgId). Since components and findings belong
        // to a release, and the release ownership is verified, these are implicitly tenant-safe.
        List<Component> components = componentRepository.findAllByReleaseId(releaseId);
        List<Finding> findings = findingRepository.findAllByReleaseId(releaseId);

        // Enrich findings with vulnerability data + decisions
        List<PdfReportGenerator.EnrichedFinding> enrichedFindings = findings.stream().map(f -> {
            Vulnerability vuln = vulnerabilityRepository.findById(f.getVulnerabilityId()).orElse(null);
            List<FindingDecision> decisions = decisionRepository.findAllByFindingId(f.getId());
            return new PdfReportGenerator.EnrichedFinding(
                    f,
                    vuln != null ? vuln.getOsvId() : null,
                    vuln != null ? vuln.getSummary() : null,
                    vuln != null ? vuln.getSeverity() : null,
                    decisions
            );
        }).toList();

        Map<UUID, List<FindingDecision>> decisionsByFinding = enrichedFindings.stream()
                .collect(Collectors.toMap(
                        ef -> ef.finding().getId(),
                        PdfReportGenerator.EnrichedFinding::decisions
                ));

        String auditHashHead = auditEventRepository.findTopByOrgIdOrderByCreatedAtDesc(orgId)
                .map(AuditEvent::getHash).orElse(null);

        // Build report data
        Map<String, Object> reportData = buildReportData(product, release, evidences,
                components, enrichedFindings, auditHashHead);

        // Stream ZIP
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            // report.json
            zip.putNextEntry(new ZipEntry("report.json"));
            zip.write(objectMapper.writeValueAsBytes(reportData));
            zip.closeEntry();

            // report.pdf
            zip.putNextEntry(new ZipEntry("report.pdf"));
            byte[] pdfBytes = pdfGenerator.generate(product, release, evidences,
                    components, enrichedFindings, auditHashHead);
            zip.write(pdfBytes);
            zip.closeEntry();
        }
    }

    private Map<String, Object> buildReportData(
            Product product, Release release, List<Evidence> evidences,
            List<Component> components, List<PdfReportGenerator.EnrichedFinding> enrichedFindings,
            String auditHashHead) {

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("generatedAt", Instant.now().toString());
        data.put("auditHashHead", auditHashHead);

        // Product
        data.put("product", Map.of(
                "id", product.getId().toString(),
                "name", product.getName(),
                "type", product.getType(),
                "criticality", product.getCriticality()));

        // Release
        Map<String, Object> releaseMap = new LinkedHashMap<>();
        releaseMap.put("id", release.getId().toString());
        releaseMap.put("version", release.getVersion());
        releaseMap.put("status", release.getStatus().name());
        releaseMap.put("gitRef", release.getGitRef());
        releaseMap.put("createdAt", release.getCreatedAt().toString());
        data.put("release", releaseMap);

        // Evidences
        data.put("evidences", evidences.stream().map(e -> Map.of(
                "id", e.getId().toString(),
                "type", e.getType().name(),
                "filename", e.getFilename(),
                "sha256", e.getSha256(),
                "createdAt", e.getCreatedAt().toString()
        )).collect(Collectors.toList()));

        // Components
        data.put("components", components.stream().map(c -> Map.of(
                "purl", c.getPurl(),
                "name", c.getName(),
                "version", c.getVersion() != null ? c.getVersion() : "",
                "type", c.getType()
        )).collect(Collectors.toList()));

        // Findings
        data.put("findings", enrichedFindings.stream().map(ef -> {
            Finding f = ef.finding();
            Map<String, Object> fm = new LinkedHashMap<>();
            fm.put("id", f.getId().toString());
            fm.put("status", f.getStatus());
            fm.put("source", f.getSource());
            fm.put("detectedAt", f.getDetectedAt().toString());
            if (ef.osvId() != null) fm.put("osvId", ef.osvId());
            if (ef.summary() != null) fm.put("summary", ef.summary());
            if (ef.severity() != null) fm.put("severity", ef.severity());

            fm.put("decisions", ef.decisions().stream().map(d -> Map.of(
                    "type", d.getDecisionType(),
                    "rationale", d.getRationale(),
                    "decidedBy", d.getDecidedBy().toString(),
                    "createdAt", d.getCreatedAt().toString()
            )).collect(Collectors.toList()));

            return fm;
        }).collect(Collectors.toList()));

        return data;
    }
}
