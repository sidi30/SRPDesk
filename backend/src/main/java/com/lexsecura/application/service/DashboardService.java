package com.lexsecura.application.service;

import com.lexsecura.application.dto.DashboardResponse;
import com.lexsecura.application.dto.ReadinessScoreResponse;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.repository.*;
import com.lexsecura.domain.model.Vulnerability;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

    public DashboardService(ProductRepository productRepository,
                            ReleaseRepository releaseRepository,
                            FindingRepository findingRepository,
                            CraEventRepository craEventRepository,
                            CraChecklistRepository checklistRepository,
                            ReadinessService readinessService,
                            VulnerabilityRepository vulnerabilityRepository) {
        this.productRepository = productRepository;
        this.releaseRepository = releaseRepository;
        this.findingRepository = findingRepository;
        this.craEventRepository = craEventRepository;
        this.checklistRepository = checklistRepository;
        this.readinessService = readinessService;
        this.vulnerabilityRepository = vulnerabilityRepository;
    }

    public DashboardResponse getDashboard() {
        UUID orgId = TenantContext.getOrgId();

        List<Product> products = productRepository.findAllByOrgId(orgId);
        List<CraEvent> events = craEventRepository.findAllByOrgId(orgId);

        int totalReleases = 0;
        int totalFindings = 0;
        int openFindings = 0;
        int criticalHighFindings = 0;

        List<DashboardResponse.ProductReadiness> productReadiness = new ArrayList<>();
        double totalReadiness = 0;

        for (Product product : products) {
            List<Release> releases = releaseRepository.findAllByProductIdAndOrgId(product.getId(), orgId);
            totalReleases += releases.size();

            for (Release release : releases) {
                List<Finding> findings = findingRepository.findAllByReleaseId(release.getId());
                totalFindings += findings.size();
                for (Finding f : findings) {
                    if ("OPEN".equals(f.getStatus())) {
                        openFindings++;
                        Vulnerability vuln = vulnerabilityRepository.findById(f.getVulnerabilityId()).orElse(null);
                        if (vuln != null && ("CRITICAL".equals(vuln.getSeverity()) || "HIGH".equals(vuln.getSeverity()))) {
                            criticalHighFindings++;
                        }
                    }
                }
            }

            // Readiness score
            int readinessScore = 0;
            try {
                ReadinessScoreResponse score = readinessService.computeScore(product.getId());
                readinessScore = score.overallScore();
            } catch (Exception ignored) {
                // Product may not have checklist yet
            }
            totalReadiness += readinessScore;

            long checklistTotal = checklistRepository.countByProductIdAndOrgId(product.getId(), orgId);
            long checklistCompliant = checklistRepository.countByProductIdAndOrgIdAndStatus(product.getId(), orgId, "COMPLIANT");

            productReadiness.add(new DashboardResponse.ProductReadiness(
                    product.getId().toString(),
                    product.getName(),
                    product.getType(),
                    product.getConformityPath(),
                    readinessScore,
                    (int) checklistTotal,
                    (int) checklistCompliant
            ));
        }

        int activeCraEvents = (int) events.stream()
                .filter(e -> !"CLOSED".equals(e.getStatus()))
                .count();

        double averageReadiness = products.isEmpty() ? 0 : totalReadiness / products.size();

        return new DashboardResponse(
                products.size(),
                totalReleases,
                totalFindings,
                openFindings,
                criticalHighFindings,
                events.size(),
                activeCraEvents,
                Math.round(averageReadiness * 10.0) / 10.0,
                productReadiness
        );
    }
}
