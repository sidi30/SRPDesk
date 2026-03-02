package com.lexsecura.application.service;

import com.lexsecura.application.dto.DashboardResponse;
import com.lexsecura.application.dto.ReadinessScoreResponse;
import com.lexsecura.application.dto.SlaResponse;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductRepository productRepository;
    private final ReleaseRepository releaseRepository;
    private final FindingRepository findingRepository;
    private final CraEventRepository craEventRepository;
    private final CraChecklistRepository checklistRepository;
    private final ReadinessService readinessService;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final CiUploadEventRepository ciUploadEventRepository;
    private final ConformityAssessmentRepository conformityAssessmentRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final EuDocRepository euDocRepository;
    private final SlaService slaService;

    public DashboardService(ProductRepository productRepository,
                            ReleaseRepository releaseRepository,
                            FindingRepository findingRepository,
                            CraEventRepository craEventRepository,
                            CraChecklistRepository checklistRepository,
                            ReadinessService readinessService,
                            VulnerabilityRepository vulnerabilityRepository,
                            CiUploadEventRepository ciUploadEventRepository,
                            ConformityAssessmentRepository conformityAssessmentRepository,
                            RiskAssessmentRepository riskAssessmentRepository,
                            EuDocRepository euDocRepository,
                            SlaService slaService) {
        this.productRepository = productRepository;
        this.releaseRepository = releaseRepository;
        this.findingRepository = findingRepository;
        this.craEventRepository = craEventRepository;
        this.checklistRepository = checklistRepository;
        this.readinessService = readinessService;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.ciUploadEventRepository = ciUploadEventRepository;
        this.conformityAssessmentRepository = conformityAssessmentRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.euDocRepository = euDocRepository;
        this.slaService = slaService;
    }

    public DashboardResponse getDashboard() {
        UUID orgId = TenantContext.getOrgId();
        Instant now = Instant.now();

        List<Product> products = productRepository.findAllByOrgId(orgId);
        List<CraEvent> events = craEventRepository.findAllByOrgId(orgId);

        // Build a map of latest CI upload events per product
        Map<UUID, CiUploadEvent> latestCiEvents = new HashMap<>();
        List<CiUploadEvent> allCiEvents = ciUploadEventRepository.findAllByOrgId(orgId);
        for (CiUploadEvent ev : allCiEvents) {
            CiUploadEvent existing = latestCiEvents.get(ev.getProductId());
            if (existing == null || (ev.getCreatedAt() != null && existing.getCreatedAt() != null
                    && ev.getCreatedAt().isAfter(existing.getCreatedAt()))) {
                latestCiEvents.put(ev.getProductId(), ev);
            }
        }

        int totalReleases = 0;
        int totalFindings = 0;
        int openFindings = 0;
        int criticalHighFindings = 0;
        int productsWithEuDoc = 0;
        int productsFullyCompliant = 0;
        int productsAutomated = 0;

        List<DashboardResponse.ProductReadiness> productReadiness = new ArrayList<>();
        List<DashboardResponse.Alert> alerts = new ArrayList<>();
        double totalReadiness = 0;

        for (Product product : products) {
            UUID productId = product.getId();
            String productName = product.getName();

            List<Release> releases = releaseRepository.findAllByProductIdAndOrgId(productId, orgId);
            totalReleases += releases.size();

            int productOpenFindings = 0;
            int productCriticalFindings = 0;

            for (Release release : releases) {
                List<Finding> findings = findingRepository.findAllByReleaseId(release.getId());
                totalFindings += findings.size();
                for (Finding f : findings) {
                    if ("OPEN".equals(f.getStatus())) {
                        openFindings++;
                        productOpenFindings++;
                        Vulnerability vuln = vulnerabilityRepository.findById(f.getVulnerabilityId()).orElse(null);
                        if (vuln != null) {
                            if ("CRITICAL".equals(vuln.getSeverity()) || "HIGH".equals(vuln.getSeverity())) {
                                criticalHighFindings++;
                            }
                            if ("CRITICAL".equals(vuln.getSeverity())) {
                                productCriticalFindings++;
                            }
                        }
                    }
                }
            }

            // Alert: CRITICAL_VULN
            if (productCriticalFindings > 0) {
                alerts.add(new DashboardResponse.Alert(
                        "CRITICAL_VULN", "CRITICAL", productId.toString(), productName,
                        productCriticalFindings + " vulnérabilité(s) CRITICAL ouverte(s)", now));
            }

            // Latest released version and supportedUntil
            Release latestRelease = releases.stream()
                    .filter(r -> "RELEASED".equals(r.getStatus().name()))
                    .max(Comparator.comparing(r -> r.getReleasedAt() != null ? r.getReleasedAt() : r.getCreatedAt()))
                    .orElse(null);
            String latestVersion = latestRelease != null ? latestRelease.getVersion() : null;
            Instant supportedUntil = latestRelease != null ? latestRelease.getSupportedUntil() : null;

            // Alert: EOL_IMMINENT
            if (supportedUntil != null) {
                long daysUntilEol = Duration.between(now, supportedUntil).toDays();
                if (daysUntilEol <= 90 && daysUntilEol >= 0) {
                    alerts.add(new DashboardResponse.Alert(
                            "EOL_IMMINENT", "HIGH", productId.toString(), productName,
                            "Fin de support dans " + daysUntilEol + " jours", now));
                }
            }

            // Conformity Assessment
            List<ConformityAssessment> conformities = conformityAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId);
            ConformityAssessment latestConformity = conformities.isEmpty() ? null : conformities.get(0);
            String conformityStatus = latestConformity != null ? latestConformity.getStatus() : null;
            int conformityProgress = 0;
            if (latestConformity != null && latestConformity.getTotalSteps() > 0) {
                conformityProgress = (int) Math.round((double) latestConformity.getCurrentStep() / latestConformity.getTotalSteps() * 100);
            }

            // Alert: CONFORMITY_BLOCKED
            if (latestConformity != null && "IN_PROGRESS".equals(latestConformity.getStatus())
                    && latestConformity.getStartedAt() != null) {
                long daysSinceStart = Duration.between(latestConformity.getStartedAt(), now).toDays();
                if (daysSinceStart > 30) {
                    alerts.add(new DashboardResponse.Alert(
                            "CONFORMITY_BLOCKED", "MEDIUM", productId.toString(), productName,
                            "Évaluation de conformité en cours depuis " + daysSinceStart + " jours", now));
                }
            }

            // Risk Assessment
            List<RiskAssessment> riskAssessments = riskAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId);
            RiskAssessment latestRisk = riskAssessments.isEmpty() ? null : riskAssessments.get(0);
            String riskLevel = latestRisk != null ? latestRisk.getOverallRiskLevel() : null;
            String riskStatus = latestRisk != null ? latestRisk.getStatus() : null;

            // Alert: NO_RISK_ASSESSMENT
            if (riskAssessments.isEmpty()) {
                alerts.add(new DashboardResponse.Alert(
                        "NO_RISK_ASSESSMENT", "LOW", productId.toString(), productName,
                        "Aucune analyse de risque configurée", now));
            }

            // EU Declaration of Conformity
            List<EuDeclarationOfConformity> euDocs = euDocRepository.findAllByProductIdAndOrgId(productId, orgId);
            EuDeclarationOfConformity latestEuDoc = euDocs.isEmpty() ? null : euDocs.get(0);
            String euDocStatus = latestEuDoc != null ? latestEuDoc.getStatus() : null;
            if ("SIGNED".equals(euDocStatus) || "PUBLISHED".equals(euDocStatus)) {
                productsWithEuDoc++;
            }

            // Fully compliant check
            boolean conformityApproved = "APPROVED".equals(conformityStatus);
            boolean riskApproved = "APPROVED".equals(riskStatus);
            boolean euDocSigned = "SIGNED".equals(euDocStatus) || "PUBLISHED".equals(euDocStatus);
            if (conformityApproved && riskApproved && euDocSigned) {
                productsFullyCompliant++;
            }

            // Readiness score
            int readinessScore = 0;
            try {
                ReadinessScoreResponse score = readinessService.computeScore(productId);
                readinessScore = score.overallScore();
            } catch (Exception ignored) {
                // Product may not have checklist yet
            }
            totalReadiness += readinessScore;

            long checklistTotal = checklistRepository.countByProductIdAndOrgId(productId, orgId);
            long checklistCompliant = checklistRepository.countByProductIdAndOrgIdAndStatus(productId, orgId, "COMPLIANT");

            // CI health data
            CiUploadEvent latestCi = latestCiEvents.get(productId);
            Instant lastCiUploadAt = latestCi != null ? latestCi.getCreatedAt() : null;
            String sbomFreshness = computeFreshness(lastCiUploadAt);
            Integer lastQualityScore = latestCi != null ? latestCi.getQualityScore() : null;
            String lastPolicyResult = latestCi != null ? latestCi.getPolicyResult() : null;

            // Alert: SBOM_OUTDATED
            if ("OUTDATED".equals(sbomFreshness)) {
                alerts.add(new DashboardResponse.Alert(
                        "SBOM_OUTDATED", "MEDIUM", productId.toString(), productName,
                        "SBOM obsolète (> 30 jours)", now));
            }

            // Alert: CI_FAILING
            if ("FAIL".equals(lastPolicyResult)) {
                alerts.add(new DashboardResponse.Alert(
                        "CI_FAILING", "HIGH", productId.toString(), productName,
                        "Pipeline CI en échec", now));
            }

            // Automation check: SBOM FRESH + CI PASS
            if ("FRESH".equals(sbomFreshness) && "PASS".equals(lastPolicyResult)) {
                productsAutomated++;
            }

            productReadiness.add(new DashboardResponse.ProductReadiness(
                    productId.toString(),
                    productName,
                    product.getType(),
                    product.getConformityPath(),
                    readinessScore,
                    (int) checklistTotal,
                    (int) checklistCompliant,
                    lastCiUploadAt,
                    sbomFreshness,
                    lastQualityScore,
                    lastPolicyResult,
                    productOpenFindings,
                    productCriticalFindings,
                    conformityStatus,
                    conformityProgress,
                    riskLevel,
                    riskStatus,
                    euDocStatus,
                    supportedUntil,
                    releases.size(),
                    latestVersion
            ));
        }

        // SLA alerts from CRA events
        for (CraEvent event : events) {
            if ("CLOSED".equals(event.getStatus())) continue;
            try {
                SlaResponse sla = slaService.computeSla(event);
                if (sla.earlyWarning().overdue() || sla.notification().overdue()
                        || (sla.finalReport() != null && sla.finalReport().overdue())) {
                    alerts.add(new DashboardResponse.Alert(
                            "SLA_OVERDUE", "HIGH", event.getProductId().toString(),
                            findProductName(products, event.getProductId()),
                            "SLA dépassée pour l'événement: " + event.getTitle(), now));
                } else {
                    long minRemaining = sla.earlyWarning().remainingSeconds();
                    if (sla.notification().remainingSeconds() < minRemaining) {
                        minRemaining = sla.notification().remainingSeconds();
                    }
                    if (sla.finalReport() != null && sla.finalReport().remainingSeconds() < minRemaining) {
                        minRemaining = sla.finalReport().remainingSeconds();
                    }
                    if (minRemaining < 86400) { // < 24h
                        alerts.add(new DashboardResponse.Alert(
                                "SLA_WARNING", "MEDIUM", event.getProductId().toString(),
                                findProductName(products, event.getProductId()),
                                "SLA critique dans moins de 24h: " + event.getTitle(), now));
                    }
                }
            } catch (Exception ignored) {
                // SLA computation may fail if settings are missing
            }
        }

        int activeCraEvents = (int) events.stream()
                .filter(e -> !"CLOSED".equals(e.getStatus()))
                .count();

        double averageReadiness = products.isEmpty() ? 0 : totalReadiness / products.size();
        int automationScore = products.isEmpty() ? 0 : (int) Math.round((double) productsAutomated / products.size() * 100);

        int alertsCritical = (int) alerts.stream().filter(a -> "CRITICAL".equals(a.severity())).count();
        int alertsHigh = (int) alerts.stream().filter(a -> "HIGH".equals(a.severity())).count();
        int alertsMedium = (int) alerts.stream().filter(a -> "MEDIUM".equals(a.severity())).count();

        return new DashboardResponse(
                products.size(),
                totalReleases,
                totalFindings,
                openFindings,
                criticalHighFindings,
                events.size(),
                activeCraEvents,
                Math.round(averageReadiness * 10.0) / 10.0,
                openFindings,
                productsWithEuDoc,
                productsFullyCompliant,
                automationScore,
                alerts,
                alertsCritical,
                alertsHigh,
                alertsMedium,
                productReadiness
        );
    }

    private String findProductName(List<Product> products, UUID productId) {
        return products.stream()
                .filter(p -> p.getId().equals(productId))
                .map(Product::getName)
                .findFirst()
                .orElse("Unknown");
    }

    private String computeFreshness(Instant lastUpload) {
        if (lastUpload == null) return "NONE";
        Duration age = Duration.between(lastUpload, Instant.now());
        if (age.toDays() <= 7) return "FRESH";
        if (age.toDays() <= 30) return "STALE";
        return "OUTDATED";
    }
}
