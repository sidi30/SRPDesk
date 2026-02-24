package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.SbomUploadResponse;
import com.lexsecura.application.port.StoragePort;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.config.SbomProperties;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@Transactional
public class SbomService {

    private static final Logger log = LoggerFactory.getLogger(SbomService.class);

    private final ReleaseRepository releaseRepository;
    private final ComponentRepository componentRepository;
    private final ReleaseComponentRepository releaseComponentRepository;
    private final EvidenceRepository evidenceRepository;
    private final StoragePort storagePort;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final SbomProperties sbomProperties;

    public SbomService(ReleaseRepository releaseRepository,
                       ComponentRepository componentRepository,
                       ReleaseComponentRepository releaseComponentRepository,
                       EvidenceRepository evidenceRepository,
                       StoragePort storagePort,
                       AuditService auditService,
                       ObjectMapper objectMapper,
                       SbomProperties sbomProperties) {
        this.releaseRepository = releaseRepository;
        this.componentRepository = componentRepository;
        this.releaseComponentRepository = releaseComponentRepository;
        this.evidenceRepository = evidenceRepository;
        this.storagePort = storagePort;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.sbomProperties = sbomProperties;
    }

    public SbomUploadResponse ingest(UUID releaseId, MultipartFile file) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        if (file.getSize() > sbomProperties.getMaxSizeBytes()) {
            throw new IllegalArgumentException("SBOM file exceeds maximum size of " + sbomProperties.getMaxSizeMb() + "MB");
        }

        Release release = releaseRepository.findByIdAndOrgId(releaseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found: " + releaseId));

        try {
            byte[] content = file.getBytes();
            JsonNode root = objectMapper.readTree(content);

            // Auto-detect format: CycloneDX vs SPDX
            List<Component> parsed;
            if (root.has("bomFormat") && "CycloneDX".equals(root.path("bomFormat").asText(""))) {
                parsed = parseCycloneDx(root, releaseId);
            } else if (SpdxParser.isSpdx(root)) {
                parsed = parseSpdx(root, releaseId);
            } else {
                throw new IllegalArgumentException("Unsupported SBOM format: expected CycloneDX or SPDX JSON");
            }

            // Store SBOM as evidence
            String sha256 = computeSha256(content);
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "sbom.json";
            String storageUri = orgId + "/releases/" + releaseId + "/sbom_" + UUID.randomUUID() + ".json";

            storagePort.upload(storageUri,
                    new java.io.ByteArrayInputStream(content), content.length, "application/json");

            Evidence evidence = new Evidence(releaseId, orgId, EvidenceType.SBOM, filename,
                    "application/json", content.length, sha256, storageUri, userId);
            evidence = evidenceRepository.save(evidence);

            auditService.record(orgId, "SBOM", releaseId, "INGEST", userId,
                    Map.of("components", parsed.size(), "filename", filename, "sha256", sha256));

            log.info("SBOM ingested: releaseId={}, components={}, evidenceId={}",
                    releaseId, parsed.size(), evidence.getId());

            return new SbomUploadResponse(evidence.getId(), parsed.size(), sha256);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON in SBOM file: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SBOM file", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Component> getComponentsByRelease(UUID releaseId) {
        return componentRepository.findAllByReleaseId(releaseId);
    }

    private List<Component> parseCycloneDx(JsonNode root, UUID releaseId) {
        JsonNode componentsNode = root.path("components");
        if (!componentsNode.isArray()) {
            throw new IllegalArgumentException("CycloneDX SBOM has no 'components' array");
        }

        List<Component> parsed = new ArrayList<>();
        for (JsonNode cn : componentsNode) {
            String name = cn.path("name").asText("");
            String version = cn.path("version").asText(null);
            String type = cn.path("type").asText("library");
            String rawPurl = cn.path("purl").asText(null);
            final String purl = (rawPurl == null || rawPurl.isBlank())
                    ? "pkg:generic/" + name + (version != null ? "@" + version : "")
                    : rawPurl;

            Component component = componentRepository.findByPurl(purl)
                    .orElseGet(() -> {
                        Component c = new Component(purl, name, version, type);
                        return componentRepository.save(c);
                    });

            if (!releaseComponentRepository.existsByReleaseIdAndComponentId(releaseId, component.getId())) {
                releaseComponentRepository.save(new ReleaseComponent(releaseId, component.getId()));
            }
            parsed.add(component);
        }
        return parsed;
    }

    private List<Component> parseSpdx(JsonNode root, UUID releaseId) {
        List<SpdxParser.ParsedPackage> packages = SpdxParser.parse(root);

        List<Component> parsed = new ArrayList<>();
        for (SpdxParser.ParsedPackage pkg : packages) {
            Component component = componentRepository.findByPurl(pkg.purl())
                    .orElseGet(() -> {
                        Component c = new Component(pkg.purl(), pkg.name(), pkg.version(), "library");
                        c.setLicense(pkg.license());
                        c.setSupplier(pkg.supplier());
                        return componentRepository.save(c);
                    });

            if (!releaseComponentRepository.existsByReleaseIdAndComponentId(releaseId, component.getId())) {
                releaseComponentRepository.save(new ReleaseComponent(releaseId, component.getId()));
            }
            parsed.add(component);
        }
        return parsed;
    }

    private String computeSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
