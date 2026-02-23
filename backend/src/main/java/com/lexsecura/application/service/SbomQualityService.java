package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.SbomQualityScoreResponse;
import com.lexsecura.application.dto.SbomQualityScoreResponse.CriterionResult;
import com.lexsecura.application.port.StoragePort;
import com.lexsecura.domain.model.Evidence;
import com.lexsecura.domain.model.EvidenceType;
import com.lexsecura.domain.repository.EvidenceRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Evaluates SBOM quality against BSI TR-03183-2 / NTIA Minimum Elements.
 * 8 criteria, 100-point scale.
 */
@Service
public class SbomQualityService {

    private static final Logger log = LoggerFactory.getLogger(SbomQualityService.class);

    private final EvidenceRepository evidenceRepository;
    private final StoragePort storagePort;
    private final ObjectMapper objectMapper;

    public SbomQualityService(EvidenceRepository evidenceRepository,
                              StoragePort storagePort,
                              ObjectMapper objectMapper) {
        this.evidenceRepository = evidenceRepository;
        this.storagePort = storagePort;
        this.objectMapper = objectMapper;
    }

    public SbomQualityScoreResponse scoreRelease(UUID releaseId) {
        UUID orgId = TenantContext.getOrgId();

        // Find latest SBOM evidence for this release
        Evidence sbomEvidence = evidenceRepository.findAllByReleaseIdAndOrgId(releaseId, orgId).stream()
                .filter(e -> e.getType() == EvidenceType.SBOM)
                .reduce((a, b) -> b) // last one = latest
                .orElseThrow(() -> new IllegalStateException("No SBOM found for release " + releaseId));

        try (InputStream is = storagePort.download(sbomEvidence.getStorageUri())) {
            JsonNode root = objectMapper.readTree(is);
            return evaluate(root);
        } catch (Exception e) {
            log.error("Failed to score SBOM for release {}: {}", releaseId, e.getMessage());
            throw new RuntimeException("SBOM quality scoring failed", e);
        }
    }

    SbomQualityScoreResponse evaluate(JsonNode root) {
        boolean isCycloneDx = "CycloneDX".equals(root.path("bomFormat").asText(""));
        boolean isSpdx = root.has("spdxVersion");

        JsonNode components;
        if (isCycloneDx) {
            components = root.path("components");
        } else if (isSpdx) {
            components = root.path("packages");
        } else {
            return new SbomQualityScoreResponse(0, "F", 0, List.of());
        }

        int count = components.isArray() ? components.size() : 0;
        if (count == 0) {
            return new SbomQualityScoreResponse(0, "F", 0, List.of());
        }

        List<CriterionResult> criteria = new ArrayList<>();
        criteria.add(checkAuthorSupplier(root, isCycloneDx));
        criteria.add(checkComponentNames(components, isCycloneDx));
        criteria.add(checkComponentVersions(components, isCycloneDx));
        criteria.add(checkUniqueIdentifiers(components, isCycloneDx));
        criteria.add(checkDependencies(root, isCycloneDx));
        criteria.add(checkLicenses(components, isCycloneDx));
        criteria.add(checkHashes(components, isCycloneDx));
        criteria.add(checkTimestamp(root, isCycloneDx));

        int total = criteria.stream().mapToInt(CriterionResult::score).sum();
        String grade = computeGrade(total);

        return new SbomQualityScoreResponse(total, grade, count, criteria);
    }

    // C1: Author/Supplier name (15 pts)
    private CriterionResult checkAuthorSupplier(JsonNode root, boolean cdx) {
        boolean hasAuthor;
        if (cdx) {
            JsonNode metadata = root.path("metadata");
            hasAuthor = !metadata.path("supplier").path("name").asText("").isBlank()
                    || !metadata.path("manufacture").path("name").asText("").isBlank()
                    || (metadata.path("authors").isArray() && !metadata.path("authors").isEmpty());
        } else {
            hasAuthor = !root.path("creationInfo").path("creators").isMissingNode()
                    && root.path("creationInfo").path("creators").isArray()
                    && !root.path("creationInfo").path("creators").isEmpty();
        }
        int score = hasAuthor ? 15 : 0;
        return new CriterionResult("AUTHOR", "Author/Supplier Name (NTIA)", 15, score,
                hasAuthor ? 1.0 : 0.0, hasAuthor ? "Present" : "Missing — required by NTIA & BSI TR-03183-2");
    }

    // C2: Component names (15 pts)
    private CriterionResult checkComponentNames(JsonNode components, boolean cdx) {
        int total = 0, withName = 0;
        for (JsonNode c : components) {
            total++;
            String name = c.path("name").asText("");
            if (!name.isBlank()) withName++;
        }
        double coverage = total > 0 ? (double) withName / total : 0;
        int score = (int) Math.round(coverage * 15);
        return new CriterionResult("NAMES", "Component Names", 15, score, coverage,
                withName + "/" + total + " components have names");
    }

    // C3: Component versions (15 pts)
    private CriterionResult checkComponentVersions(JsonNode components, boolean cdx) {
        int total = 0, withVersion = 0;
        String versionField = cdx ? "version" : "versionInfo";
        for (JsonNode c : components) {
            total++;
            String ver = c.path(versionField).asText("");
            if (!ver.isBlank()) withVersion++;
        }
        double coverage = total > 0 ? (double) withVersion / total : 0;
        int score = (int) Math.round(coverage * 15);
        return new CriterionResult("VERSIONS", "Component Versions", 15, score, coverage,
                withVersion + "/" + total + " components have versions");
    }

    // C4: Unique identifiers / purl (15 pts)
    private CriterionResult checkUniqueIdentifiers(JsonNode components, boolean cdx) {
        int total = 0, withPurl = 0;
        for (JsonNode c : components) {
            total++;
            if (cdx) {
                if (!c.path("purl").asText("").isBlank()) withPurl++;
            } else {
                JsonNode refs = c.path("externalRefs");
                if (refs.isArray()) {
                    for (JsonNode ref : refs) {
                        if ("purl".equals(ref.path("referenceType").asText(""))) {
                            withPurl++;
                            break;
                        }
                    }
                }
            }
        }
        double coverage = total > 0 ? (double) withPurl / total : 0;
        int score = (int) Math.round(coverage * 15);
        return new CriterionResult("PURL", "Unique Identifiers (purl)", 15, score, coverage,
                withPurl + "/" + total + " components have purl");
    }

    // C5: Dependency relationships (10 pts)
    private CriterionResult checkDependencies(JsonNode root, boolean cdx) {
        boolean hasDeps;
        if (cdx) {
            JsonNode deps = root.path("dependencies");
            hasDeps = deps.isArray() && !deps.isEmpty();
        } else {
            JsonNode rels = root.path("relationships");
            hasDeps = rels.isArray() && !rels.isEmpty();
        }
        int score = hasDeps ? 10 : 0;
        return new CriterionResult("DEPS", "Dependency Relationships", 10, score,
                hasDeps ? 1.0 : 0.0, hasDeps ? "Present" : "Missing — recommended by BSI TR-03183-2");
    }

    // C6: License information (10 pts)
    private CriterionResult checkLicenses(JsonNode components, boolean cdx) {
        int total = 0, withLicense = 0;
        for (JsonNode c : components) {
            total++;
            if (cdx) {
                JsonNode licenses = c.path("licenses");
                if (licenses.isArray() && !licenses.isEmpty()) withLicense++;
            } else {
                String declared = c.path("licenseDeclared").asText("");
                if (!declared.isBlank() && !"NOASSERTION".equals(declared)) withLicense++;
            }
        }
        double coverage = total > 0 ? (double) withLicense / total : 0;
        int score = (int) Math.round(coverage * 10);
        return new CriterionResult("LICENSES", "License Information", 10, score, coverage,
                withLicense + "/" + total + " components have licenses");
    }

    // C7: Cryptographic hashes (10 pts)
    private CriterionResult checkHashes(JsonNode components, boolean cdx) {
        int total = 0, withHash = 0;
        for (JsonNode c : components) {
            total++;
            if (cdx) {
                JsonNode hashes = c.path("hashes");
                if (hashes.isArray() && !hashes.isEmpty()) withHash++;
            } else {
                JsonNode checksums = c.path("checksums");
                if (checksums.isArray() && !checksums.isEmpty()) withHash++;
            }
        }
        double coverage = total > 0 ? (double) withHash / total : 0;
        int score = (int) Math.round(coverage * 10);
        return new CriterionResult("HASHES", "Cryptographic Hashes", 10, score, coverage,
                withHash + "/" + total + " components have hashes");
    }

    // C8: Timestamp (10 pts)
    private CriterionResult checkTimestamp(JsonNode root, boolean cdx) {
        boolean hasTimestamp;
        if (cdx) {
            hasTimestamp = !root.path("metadata").path("timestamp").asText("").isBlank();
        } else {
            hasTimestamp = !root.path("creationInfo").path("created").asText("").isBlank();
        }
        int score = hasTimestamp ? 10 : 0;
        return new CriterionResult("TIMESTAMP", "SBOM Timestamp", 10, score,
                hasTimestamp ? 1.0 : 0.0, hasTimestamp ? "Present" : "Missing — required by NTIA");
    }

    private String computeGrade(int score) {
        if (score >= 90) return "A";
        if (score >= 75) return "B";
        if (score >= 60) return "C";
        if (score >= 40) return "D";
        return "F";
    }
}
