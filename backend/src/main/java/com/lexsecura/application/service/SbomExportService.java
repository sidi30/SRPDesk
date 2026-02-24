package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lexsecura.domain.model.Component;
import com.lexsecura.application.service.EntityNotFoundException;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.repository.ComponentRepository;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.domain.repository.ReleaseRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service to export SBOM in CycloneDX and SPDX 2.3 formats.
 * CRA Annexe I §2(1): Manufacturers shall identify and document vulnerabilities
 * and components, including generating an SBOM in a commonly used machine-readable format.
 * Supporting both CycloneDX and SPDX ensures maximum interoperability.
 */
@Service
@Transactional(readOnly = true)
public class SbomExportService {

    private static final Logger log = LoggerFactory.getLogger(SbomExportService.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);

    private final ReleaseRepository releaseRepository;
    private final ProductRepository productRepository;
    private final ComponentRepository componentRepository;
    private final ObjectMapper objectMapper;

    public SbomExportService(ReleaseRepository releaseRepository,
                             ProductRepository productRepository,
                             ComponentRepository componentRepository,
                             ObjectMapper objectMapper) {
        this.releaseRepository = releaseRepository;
        this.productRepository = productRepository;
        this.componentRepository = componentRepository;
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Export SBOM for a release in the specified format.
     * @param releaseId the release UUID
     * @param format "cyclonedx" or "spdx"
     * @return JSON string of the SBOM document
     */
    public String export(UUID releaseId, String format) {
        UUID orgId = TenantContext.getOrgId();
        Release release = releaseRepository.findByIdAndOrgId(releaseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found: " + releaseId));

        Product product = productRepository.findByIdAndOrgId(release.getProductId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + release.getProductId()));

        List<Component> components = componentRepository.findAllByReleaseId(releaseId);

        return switch (format.toLowerCase()) {
            case "spdx" -> generateSpdx23(product, release, components);
            case "cyclonedx" -> generateCycloneDx16(product, release, components);
            default -> throw new IllegalArgumentException("Unsupported SBOM format: " + format + ". Use 'cyclonedx' or 'spdx'");
        };
    }

    /**
     * Generate SPDX 2.3 JSON document.
     * Compliant with ISO/IEC 5962:2021 (SPDX 2.3).
     */
    private String generateSpdx23(Product product, Release release, List<Component> components) {
        try {
            ObjectNode doc = objectMapper.createObjectNode();
            doc.put("spdxVersion", "SPDX-2.3");
            doc.put("dataLicense", "CC0-1.0");
            doc.put("SPDXID", "SPDXRef-DOCUMENT");
            doc.put("name", product.getName() + "-" + release.getVersion());
            doc.put("documentNamespace", "https://srpdesk.com/spdx/" + release.getId());

            // Creation info
            ObjectNode creationInfo = doc.putObject("creationInfo");
            creationInfo.put("created", ISO_FORMATTER.format(Instant.now()));
            ArrayNode creators = creationInfo.putArray("creators");
            creators.add("Tool: SRPDesk SBOM Export");
            creators.add("Organization: " + product.getName());
            creationInfo.put("licenseListVersion", "3.22");

            // Document-level describes
            ArrayNode documentDescribes = doc.putArray("documentDescribes");
            documentDescribes.add("SPDXRef-Package-" + sanitizeSpdxId(product.getName()));

            // Packages array
            ArrayNode packages = doc.putArray("packages");

            // Root package (the product itself)
            ObjectNode rootPkg = packages.addObject();
            rootPkg.put("SPDXID", "SPDXRef-Package-" + sanitizeSpdxId(product.getName()));
            rootPkg.put("name", product.getName());
            rootPkg.put("versionInfo", release.getVersion());
            rootPkg.put("downloadLocation", "NOASSERTION");
            rootPkg.put("filesAnalyzed", false);
            rootPkg.put("supplier", "NOASSERTION");
            rootPkg.put("licenseConcluded", "NOASSERTION");
            rootPkg.put("licenseDeclared", "NOASSERTION");
            rootPkg.put("copyrightText", "NOASSERTION");
            rootPkg.put("primaryPackagePurpose", "APPLICATION");

            // Component packages
            for (Component comp : components) {
                ObjectNode pkg = packages.addObject();
                String spdxId = "SPDXRef-Package-" + sanitizeSpdxId(comp.getPurl());
                pkg.put("SPDXID", spdxId);
                pkg.put("name", comp.getName());
                if (comp.getVersion() != null) {
                    pkg.put("versionInfo", comp.getVersion());
                }
                pkg.put("downloadLocation", "NOASSERTION");
                pkg.put("filesAnalyzed", false);
                pkg.put("licenseConcluded", "NOASSERTION");
                pkg.put("licenseDeclared", "NOASSERTION");
                pkg.put("copyrightText", "NOASSERTION");

                // External reference: purl
                if (comp.getPurl() != null && !comp.getPurl().isBlank()) {
                    ArrayNode externalRefs = pkg.putArray("externalRefs");
                    ObjectNode purlRef = externalRefs.addObject();
                    purlRef.put("referenceCategory", "PACKAGE-MANAGER");
                    purlRef.put("referenceType", "purl");
                    purlRef.put("referenceLocator", comp.getPurl());
                }
            }

            // Relationships
            ArrayNode relationships = doc.putArray("relationships");
            String rootId = "SPDXRef-Package-" + sanitizeSpdxId(product.getName());
            for (Component comp : components) {
                ObjectNode rel = relationships.addObject();
                rel.put("spdxElementId", rootId);
                rel.put("relationshipType", "DEPENDS_ON");
                rel.put("relatedSpdxElement", "SPDXRef-Package-" + sanitizeSpdxId(comp.getPurl()));
            }

            // DESCRIBES relationship
            ObjectNode describesRel = relationships.addObject();
            describesRel.put("spdxElementId", "SPDXRef-DOCUMENT");
            describesRel.put("relationshipType", "DESCRIBES");
            describesRel.put("relatedSpdxElement", rootId);

            log.info("Generated SPDX 2.3 SBOM for release {} with {} components", release.getId(), components.size());
            return objectMapper.writeValueAsString(doc);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate SPDX SBOM", e);
        }
    }

    /**
     * Generate CycloneDX 1.6 JSON document.
     */
    private String generateCycloneDx16(Product product, Release release, List<Component> components) {
        try {
            ObjectNode doc = objectMapper.createObjectNode();
            doc.put("bomFormat", "CycloneDX");
            doc.put("specVersion", "1.6");
            doc.put("serialNumber", "urn:uuid:" + UUID.randomUUID());
            doc.put("version", 1);

            // Metadata
            ObjectNode metadata = doc.putObject("metadata");
            metadata.put("timestamp", ISO_FORMATTER.format(Instant.now()));

            ObjectNode tool = metadata.putObject("tools");
            ArrayNode toolComponents = tool.putArray("components");
            ObjectNode srpTool = toolComponents.addObject();
            srpTool.put("type", "application");
            srpTool.put("name", "SRPDesk");
            srpTool.put("version", "1.0.0");

            ObjectNode componentNode = metadata.putObject("component");
            componentNode.put("type", "application");
            componentNode.put("name", product.getName());
            componentNode.put("version", release.getVersion());

            ObjectNode supplier = metadata.putObject("supplier");
            supplier.put("name", product.getName());

            // Components
            ArrayNode comps = doc.putArray("components");
            for (Component comp : components) {
                ObjectNode cn = comps.addObject();
                cn.put("type", comp.getType() != null ? comp.getType() : "library");
                cn.put("name", comp.getName());
                if (comp.getVersion() != null) {
                    cn.put("version", comp.getVersion());
                }
                if (comp.getPurl() != null && !comp.getPurl().isBlank()) {
                    cn.put("purl", comp.getPurl());
                    cn.put("bom-ref", comp.getPurl());
                }
            }

            // Dependencies
            ArrayNode deps = doc.putArray("dependencies");
            ObjectNode rootDep = deps.addObject();
            rootDep.put("ref", product.getName() + "@" + release.getVersion());
            ArrayNode dependsOn = rootDep.putArray("dependsOn");
            for (Component comp : components) {
                if (comp.getPurl() != null) {
                    dependsOn.add(comp.getPurl());
                }
            }

            log.info("Generated CycloneDX 1.6 SBOM for release {} with {} components", release.getId(), components.size());
            return objectMapper.writeValueAsString(doc);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CycloneDX SBOM", e);
        }
    }

    private String sanitizeSpdxId(String input) {
        if (input == null) return "unknown";
        return input.replaceAll("[^a-zA-Z0-9._-]", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }
}
