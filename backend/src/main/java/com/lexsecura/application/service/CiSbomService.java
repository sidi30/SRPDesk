package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.CiSbomEnrichedResponse;
import com.lexsecura.application.dto.CiScanResponse;
import com.lexsecura.application.dto.SbomQualityScoreResponse;
import com.lexsecura.application.dto.SbomUploadResponse;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.*;

@Service
@Transactional
public class CiSbomService {

    private static final Logger log = LoggerFactory.getLogger(CiSbomService.class);

    private final ProductRepository productRepository;
    private final ReleaseRepository releaseRepository;
    private final ComponentRepository componentRepository;
    private final SbomService sbomService;
    private final SbomQualityService sbomQualityService;
    private final VulnerabilityScanService vulnerabilityScanService;
    private final CiPolicyRepository ciPolicyRepository;
    private final CiUploadEventRepository ciUploadEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.ci.default-max-critical:0}")
    private int defaultMaxCritical;

    @Value("${app.ci.default-max-high:5}")
    private int defaultMaxHigh;

    @Value("${app.ci.default-min-quality-score:50}")
    private int defaultMinQualityScore;

    @Value("${app.ci.details-base-url:}")
    private String detailsBaseUrl;

    public CiSbomService(ProductRepository productRepository,
                          ReleaseRepository releaseRepository,
                          ComponentRepository componentRepository,
                          SbomService sbomService,
                          SbomQualityService sbomQualityService,
                          VulnerabilityScanService vulnerabilityScanService,
                          CiPolicyRepository ciPolicyRepository,
                          CiUploadEventRepository ciUploadEventRepository,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.releaseRepository = releaseRepository;
        this.componentRepository = componentRepository;
        this.sbomService = sbomService;
        this.sbomQualityService = sbomQualityService;
        this.vulnerabilityScanService = vulnerabilityScanService;
        this.ciPolicyRepository = ciPolicyRepository;
        this.ciUploadEventRepository = ciUploadEventRepository;
        this.objectMapper = objectMapper;
    }

    public CiSbomEnrichedResponse uploadFromCi(String productName, String version,
                                                 String gitRef, MultipartFile file) {
        UUID orgId = TenantContext.getOrgId();

        Product product = productRepository.findByNameAndOrgId(productName, orgId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found: '" + productName + "'. Create it in SRPDesk first."));

        // Get previous components for diff
        List<Component> previousComponents = getLatestComponents(product.getId(), orgId);
        Set<String> previousPurls = new HashSet<>();
        for (Component c : previousComponents) {
            if (c.getPurl() != null) previousPurls.add(c.getPurl());
        }

        // Create or find release
        boolean releaseCreated = false;
        Release release = releaseRepository
                .findByProductIdAndVersionAndOrgId(product.getId(), version, orgId)
                .orElse(null);

        if (release == null) {
            release = new Release(product.getId(), version);
            release.setOrgId(orgId);
            if (gitRef != null && !gitRef.isBlank()) {
                release.setGitRef(gitRef);
            }
            release = releaseRepository.save(release);
            releaseCreated = true;
            log.info("CI: created release {} v{} for product {}", release.getId(), version, product.getId());
        }

        // Ingest SBOM (store, parse, create components)
        SbomUploadResponse sbomResult = sbomService.ingest(release.getId(), file);

        // Get current components for diff
        List<Component> currentComponents = componentRepository.findAllByReleaseId(release.getId());
        Set<String> currentPurls = new HashSet<>();
        for (Component c : currentComponents) {
            if (c.getPurl() != null) currentPurls.add(c.getPurl());
        }

        int newComponents = 0;
        int removedComponents = 0;
        if (!previousPurls.isEmpty()) {
            for (String purl : currentPurls) {
                if (!previousPurls.contains(purl)) newComponents++;
            }
            for (String purl : previousPurls) {
                if (!currentPurls.contains(purl)) removedComponents++;
            }
        }

        // Quality scoring
        SbomQualityScoreResponse quality = evaluateQuality(file);

        // Vulnerability scan (synchronous)
        VulnerabilityScanService.VulnScanResult vulnResult =
                vulnerabilityScanService.scanComponents(currentComponents);

        // Also trigger the persistent scan (creates Findings in DB)
        int newVulns = vulnerabilityScanService.scanRelease(release.getId());

        // Policy evaluation
        String policyResult = evaluatePolicy(orgId, vulnResult, quality.totalScore());

        // Build details URL
        String detailsUrl = buildDetailsUrl(product.getId(), release.getId());

        // Track upload event
        CiUploadEvent event = new CiUploadEvent();
        event.setOrgId(orgId);
        event.setProductId(product.getId());
        event.setReleaseId(release.getId());
        event.setComponentCount(sbomResult.componentCount());
        event.setNewComponents(newComponents);
        event.setRemovedComponents(removedComponents);
        event.setQualityScore(quality.totalScore());
        event.setQualityGrade(quality.grade());
        event.setVulnCritical(vulnResult.critical());
        event.setVulnHigh(vulnResult.high());
        event.setVulnMedium(vulnResult.medium());
        event.setVulnLow(vulnResult.low());
        event.setVulnTotal(vulnResult.total());
        event.setNewVulnerabilities(newVulns);
        event.setPolicyResult(policyResult);
        event.setGitRef(gitRef);
        event.setSha256(sbomResult.sha256());
        ciUploadEventRepository.save(event);

        log.info("CI SBOM uploaded: product={}, version={}, components={}, quality={}/{}, vulns={}, policy={}",
                productName, version, sbomResult.componentCount(),
                quality.totalScore(), quality.grade(),
                vulnResult.total(), policyResult);

        return new CiSbomEnrichedResponse(
                release.getId(),
                sbomResult.componentCount(),
                newComponents,
                removedComponents,
                quality.totalScore(),
                quality.grade(),
                new CiSbomEnrichedResponse.VulnSummary(
                        vulnResult.critical(), vulnResult.high(),
                        vulnResult.medium(), vulnResult.low(), vulnResult.total()),
                newVulns,
                policyResult,
                sbomResult.sha256(),
                detailsUrl
        );
    }

    public CiScanResponse scanFromCi(String productName, MultipartFile file) {
        UUID orgId = TenantContext.getOrgId();

        Product product = productRepository.findByNameAndOrgId(productName, orgId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found: '" + productName + "'. Create it in SRPDesk first."));

        try {
            byte[] content = file.getBytes();
            JsonNode root = objectMapper.readTree(content);

            // Quality scoring (in-memory, no persistence)
            SbomQualityScoreResponse quality = sbomQualityService.evaluate(root);

            // Parse components for vuln scan (in-memory only)
            List<Component> components = parseComponentsInMemory(root);

            // Vulnerability scan (synchronous, no DB writes)
            VulnerabilityScanService.VulnScanResult vulnResult =
                    vulnerabilityScanService.scanComponents(components);

            // Policy evaluation
            String policyResult = evaluatePolicy(orgId, vulnResult, quality.totalScore());

            // Compute SHA-256
            String sha256 = computeSha256(content);

            log.info("CI Scan (PR mode): product={}, components={}, quality={}/{}, vulns={}, policy={}",
                    productName, components.size(),
                    quality.totalScore(), quality.grade(),
                    vulnResult.total(), policyResult);

            return new CiScanResponse(
                    components.size(),
                    quality.totalScore(),
                    quality.grade(),
                    new CiSbomEnrichedResponse.VulnSummary(
                            vulnResult.critical(), vulnResult.high(),
                            vulnResult.medium(), vulnResult.low(), vulnResult.total()),
                    policyResult,
                    sha256
            );
        } catch (Exception e) {
            log.error("CI Scan failed for product {}: {}", productName, e.getMessage());
            throw new RuntimeException("SBOM scan failed: " + e.getMessage(), e);
        }
    }

    private SbomQualityScoreResponse evaluateQuality(MultipartFile file) {
        try {
            JsonNode root = objectMapper.readTree(file.getBytes());
            return sbomQualityService.evaluate(root);
        } catch (Exception e) {
            log.warn("Quality evaluation failed, returning zero score: {}", e.getMessage());
            return new SbomQualityScoreResponse(0, "F", 0, List.of());
        }
    }

    private String evaluatePolicy(UUID orgId,
                                   VulnerabilityScanService.VulnScanResult vulnResult,
                                   int qualityScore) {
        CiPolicy policy = ciPolicyRepository.findByOrgId(orgId).orElse(null);
        if (policy != null) {
            return policy.evaluate(vulnResult.critical(), vulnResult.high(), qualityScore);
        }
        // Default policy
        if (vulnResult.critical() > defaultMaxCritical) return "FAIL";
        if (vulnResult.high() > defaultMaxHigh) return "FAIL";
        if (qualityScore < defaultMinQualityScore) return "FAIL";
        if (vulnResult.critical() > 0 || vulnResult.high() > 0) return "WARN";
        return "PASS";
    }

    private List<Component> getLatestComponents(UUID productId, UUID orgId) {
        List<Release> releases = releaseRepository.findAllByProductIdAndOrgId(productId, orgId);
        if (releases.isEmpty()) return List.of();
        Release latest = releases.get(releases.size() - 1);
        return componentRepository.findAllByReleaseId(latest.getId());
    }

    private List<Component> parseComponentsInMemory(JsonNode root) {
        List<Component> components = new ArrayList<>();
        boolean isCycloneDx = "CycloneDX".equals(root.path("bomFormat").asText(""));

        JsonNode comps = isCycloneDx ? root.path("components") : root.path("packages");
        if (!comps.isArray()) return components;

        for (JsonNode cn : comps) {
            String name = cn.path("name").asText("");
            String version = isCycloneDx ? cn.path("version").asText(null) : cn.path("versionInfo").asText(null);
            String purl = cn.path("purl").asText(null);
            if (purl == null || purl.isBlank()) {
                // Try SPDX external refs
                JsonNode refs = cn.path("externalRefs");
                if (refs.isArray()) {
                    for (JsonNode ref : refs) {
                        if ("purl".equals(ref.path("referenceType").asText(""))) {
                            purl = ref.path("referenceLocator").asText(null);
                            break;
                        }
                    }
                }
            }
            if (purl == null || purl.isBlank()) {
                purl = "pkg:generic/" + name + (version != null ? "@" + version : "");
            }
            Component c = new Component(purl, name, version, "library");
            components.add(c);
        }
        return components;
    }

    private String buildDetailsUrl(UUID productId, UUID releaseId) {
        if (detailsBaseUrl != null && !detailsBaseUrl.isBlank()) {
            return detailsBaseUrl + "/products/" + productId + "/releases/" + releaseId;
        }
        return null;
    }

    private String computeSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
