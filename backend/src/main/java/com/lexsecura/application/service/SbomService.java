package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.SbomUploadResponse;
import com.lexsecura.application.port.StoragePort;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.*;
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
    private static final long MAX_SBOM_SIZE = 10 * 1024 * 1024; // 10MB

    private final ReleaseRepository releaseRepository;
    private final ComponentRepository componentRepository;
    private final ReleaseComponentRepository releaseComponentRepository;
    private final EvidenceRepository evidenceRepository;
    private final StoragePort storagePort;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public SbomService(ReleaseRepository releaseRepository,
                       ComponentRepository componentRepository,
                       ReleaseComponentRepository releaseComponentRepository,
                       EvidenceRepository evidenceRepository,
                       StoragePort storagePort,
                       AuditService auditService,
                       ObjectMapper objectMapper) {
        this.releaseRepository = releaseRepository;
        this.componentRepository = componentRepository;
        this.releaseComponentRepository = releaseComponentRepository;
        this.evidenceRepository = evidenceRepository;
        this.storagePort = storagePort;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public SbomUploadResponse ingest(UUID releaseId, MultipartFile file) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        if (file.getSize() > MAX_SBOM_SIZE) {
            throw new IllegalArgumentException("SBOM file exceeds maximum size of 10MB");
        }

        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found: " + releaseId));

        try {
            byte[] content = file.getBytes();
            JsonNode root = objectMapper.readTree(content);

            // Validate CycloneDX format
            String bomFormat = root.path("bomFormat").asText("");
            if (!"CycloneDX".equals(bomFormat)) {
                throw new IllegalArgumentException("Invalid SBOM format: expected CycloneDX, got '" + bomFormat + "'");
            }

            // Parse components
            JsonNode componentsNode = root.path("components");
            if (!componentsNode.isArray()) {
                throw new IllegalArgumentException("SBOM has no 'components' array");
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
