package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.ReadinessScoreResponse;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class ReadinessService {

    private final CraChecklistRepository checklistRepository;
    private final ProductRepository productRepository;
    private final ReleaseRepository releaseRepository;
    private final FindingRepository findingRepository;
    private final EvidenceRepository evidenceRepository;
    private final CraEventRepository craEventRepository;
    private final SrpSubmissionRepository srpSubmissionRepository;
    private final ReadinessSnapshotRepository snapshotRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final ObjectMapper objectMapper;

    public ReadinessService(CraChecklistRepository checklistRepository,
                            ProductRepository productRepository,
                            ReleaseRepository releaseRepository,
                            FindingRepository findingRepository,
                            EvidenceRepository evidenceRepository,
                            CraEventRepository craEventRepository,
                            SrpSubmissionRepository srpSubmissionRepository,
                            ReadinessSnapshotRepository snapshotRepository,
                            VulnerabilityRepository vulnerabilityRepository,
                            ObjectMapper objectMapper) {
        this.checklistRepository = checklistRepository;
        this.productRepository = productRepository;
        this.releaseRepository = releaseRepository;
        this.findingRepository = findingRepository;
        this.evidenceRepository = evidenceRepository;
        this.craEventRepository = craEventRepository;
        this.srpSubmissionRepository = srpSubmissionRepository;
        this.snapshotRepository = snapshotRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ReadinessScoreResponse computeScore(UUID productId) {
        UUID orgId = TenantContext.getOrgId();

        productRepository.findByIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        List<CraChecklistItem> checklist = checklistRepository.findAllByProductIdAndOrgId(productId, orgId);
        List<Release> releases = releaseRepository.findAllByProductIdAndOrgId(productId, orgId);
        List<CraEvent> events = craEventRepository.findAllByOrgIdAndProductId(orgId, productId);

        // Gather all findings across releases
        List<Finding> allFindings = new ArrayList<>();
        boolean hasSbomEvidence = false;
        boolean hasConformityEvidence = false;
        boolean hasDesignDoc = false;

        for (Release release : releases) {
            allFindings.addAll(findingRepository.findAllByReleaseId(release.getId()));
            List<Evidence> evidences = evidenceRepository.findAllByReleaseIdAndOrgId(release.getId(), orgId);
            for (Evidence ev : evidences) {
                if (ev.getType() == EvidenceType.SBOM) hasSbomEvidence = true;
                if (ev.getType() == EvidenceType.CONFORMITY_DECLARATION) hasConformityEvidence = true;
                if (ev.getType() == EvidenceType.DESIGN_DOC) hasDesignDoc = true;
            }
        }

        // Gather submissions
        List<SrpSubmission> submissions = new ArrayList<>();
        for (CraEvent event : events) {
            submissions.addAll(srpSubmissionRepository.findAllByCraEventId(event.getId()));
        }

        // Compute categories
        List<ReadinessScoreResponse.CategoryScore> categories = new ArrayList<>();
        List<String> actionItems = new ArrayList<>();
        int totalScore = 0;

        // 1. SECURE_BY_DESIGN (25 pts)
        int secureByDesignScore = computeChecklistCategoryScore(checklist, "SECURE_BY_DESIGN", 25, actionItems);
        categories.add(new ReadinessScoreResponse.CategoryScore("SECURE_BY_DESIGN", secureByDesignScore, 25, "Secure by Design"));
        totalScore += secureByDesignScore;

        // 2. VULNERABILITY_MANAGEMENT (25 pts)
        int vulnMgmtScore = 0;
        if (hasSbomEvidence) vulnMgmtScore += 5;
        else actionItems.add("Importer un SBOM pour au moins une release");

        boolean hasScan = allFindings.stream().anyMatch(f -> "SCAN".equals(f.getSource()));
        if (hasScan) vulnMgmtScore += 5;
        else actionItems.add("Effectuer un scan de vulnérabilités");

        long criticalFindings = allFindings.stream()
                .filter(f -> "OPEN".equals(f.getStatus()))
                .filter(f -> {
                    Vulnerability v = vulnerabilityRepository.findById(f.getVulnerabilityId()).orElse(null);
                    return v != null && ("CRITICAL".equals(v.getSeverity()) || "HIGH".equals(v.getSeverity()));
                })
                .count();
        if (criticalFindings < 3) vulnMgmtScore += 5;
        else actionItems.add("Réduire les vulnérabilités critiques/hautes ouvertes (actuellement " + criticalFindings + ")");

        long decidedFindings = allFindings.stream()
                .filter(f -> !"OPEN".equals(f.getStatus()))
                .count();
        if (!allFindings.isEmpty() && decidedFindings > 0) vulnMgmtScore += 5;
        else if (!allFindings.isEmpty()) actionItems.add("Prendre des décisions sur les vulnérabilités détectées");

        vulnMgmtScore += computeChecklistCategoryScore(checklist, "VULNERABILITY_MANAGEMENT", 5, actionItems);
        categories.add(new ReadinessScoreResponse.CategoryScore("VULNERABILITY_MANAGEMENT", vulnMgmtScore, 25, "Vulnerability Management"));
        totalScore += vulnMgmtScore;

        // 3. SBOM_MANAGEMENT (15 pts)
        int sbomScore = 0;
        if (!releases.isEmpty()) {
            if (hasSbomEvidence) sbomScore += 5;
            else actionItems.add("Uploader un SBOM CycloneDX ou SPDX");

            boolean hasReleasedWithSbom = releases.stream().anyMatch(r -> "RELEASED".equals(r.getStatus().name()));
            if (hasReleasedWithSbom && hasSbomEvidence) sbomScore += 5;
            else actionItems.add("Publier une release avec un SBOM attaché");
        } else {
            actionItems.add("Créer au moins une release pour le produit");
        }
        sbomScore += computeChecklistCategoryScore(checklist, "VULNERABILITY_MANAGEMENT", 5, null);
        if (sbomScore > 15) sbomScore = 15;
        categories.add(new ReadinessScoreResponse.CategoryScore("SBOM_MANAGEMENT", sbomScore, 15, "SBOM Management"));
        totalScore += sbomScore;

        // 4. INCIDENT_REPORTING (20 pts)
        int incidentScore = 0;
        boolean hasEvents = !events.isEmpty();
        boolean hasOverdue = events.stream().anyMatch(e -> "DRAFT".equals(e.getStatus()));
        boolean hasSubmissions = !submissions.isEmpty();

        incidentScore += 5; // SLA assumed configured if war room is present
        if (!hasOverdue || !hasEvents) incidentScore += 5;
        else actionItems.add("Résoudre les événements CRA en statut DRAFT");
        if (hasSubmissions) incidentScore += 5;
        else if (hasEvents) actionItems.add("Créer des soumissions SRP pour les événements CRA");
        incidentScore += computeChecklistCategoryScore(checklist, "SECURE_BY_DESIGN", 5, null);
        if (incidentScore > 20) incidentScore = 20;
        categories.add(new ReadinessScoreResponse.CategoryScore("INCIDENT_REPORTING", incidentScore, 20, "Incident Reporting"));
        totalScore += incidentScore;

        // 5. DOCUMENTATION (15 pts)
        int docScore = 0;
        if (hasConformityEvidence) docScore += 5;
        else actionItems.add("Uploader une déclaration de conformité");
        if (hasDesignDoc) docScore += 5;
        else actionItems.add("Uploader un document de conception sécurisée");
        docScore += computeChecklistCategoryScore(checklist, "SECURE_BY_DESIGN", 5, null);
        if (docScore > 15) docScore = 15;
        categories.add(new ReadinessScoreResponse.CategoryScore("DOCUMENTATION", docScore, 15, "Documentation"));
        totalScore += docScore;

        if (totalScore > 100) totalScore = 100;

        return new ReadinessScoreResponse(productId, totalScore, categories, actionItems);
    }

    public ReadinessScoreResponse snapshotScore(UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        ReadinessScoreResponse score = computeScore(productId);

        ReadinessSnapshot snapshot = new ReadinessSnapshot();
        snapshot.setOrgId(orgId);
        snapshot.setProductId(productId);
        snapshot.setOverallScore(score.overallScore());
        snapshot.setSnapshotAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        snapshot.setCreatedBy(userId);

        try {
            snapshot.setCategoryScoresJson(objectMapper.writeValueAsString(score.categories()));
            snapshot.setActionItemsJson(objectMapper.writeValueAsString(score.actionItems()));
        } catch (Exception e) {
            snapshot.setCategoryScoresJson("{}");
            snapshot.setActionItemsJson("[]");
        }

        snapshotRepository.save(snapshot);
        return score;
    }

    private int computeChecklistCategoryScore(List<CraChecklistItem> checklist, String category,
                                               int maxPoints, List<String> actionItems) {
        List<CraChecklistItem> items = checklist.stream()
                .filter(i -> category.equals(i.getCategory()))
                .toList();

        if (items.isEmpty()) {
            if (actionItems != null) actionItems.add("Initialiser la checklist CRA pour ce produit");
            return 0;
        }

        long compliant = items.stream()
                .filter(i -> "COMPLIANT".equals(i.getStatus()) || "NOT_APPLICABLE".equals(i.getStatus()))
                .count();

        double ratio = (double) compliant / items.size();
        int score = (int) Math.round(ratio * maxPoints);

        if (ratio < 1.0 && actionItems != null) {
            long remaining = items.size() - compliant;
            actionItems.add("Compléter " + remaining + " exigences " + category.toLowerCase().replace('_', ' '));
        }

        return score;
    }
}
