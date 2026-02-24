package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.SbomUploadResponse;
import com.lexsecura.application.dto.SupplierSbomResponse;
import com.lexsecura.domain.model.SupplierSbom;
import com.lexsecura.domain.repository.ReleaseRepository;
import com.lexsecura.domain.repository.SupplierSbomRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SupplierSbomService {

    private static final Logger log = LoggerFactory.getLogger(SupplierSbomService.class);

    private final SupplierSbomRepository supplierSbomRepository;
    private final ReleaseRepository releaseRepository;
    private final SbomService sbomService;
    private final ObjectMapper objectMapper;

    public SupplierSbomService(SupplierSbomRepository supplierSbomRepository,
                               ReleaseRepository releaseRepository,
                               SbomService sbomService,
                               ObjectMapper objectMapper) {
        this.supplierSbomRepository = supplierSbomRepository;
        this.releaseRepository = releaseRepository;
        this.sbomService = sbomService;
        this.objectMapper = objectMapper;
    }

    public SupplierSbomResponse importSupplierSbom(UUID releaseId, String supplierName,
                                                    String supplierUrl, MultipartFile file) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        releaseRepository.findByIdAndOrgId(releaseId, orgId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Release not found: " + releaseId));

        // Ingest the SBOM file (creates evidence + components)
        SbomUploadResponse sbomResult = sbomService.ingest(releaseId, file);

        // Detect format
        String format = "CYCLONEDX";
        try {
            JsonNode root = objectMapper.readTree(file.getBytes());
            if (root.has("spdxVersion")) format = "SPDX";
        } catch (Exception ignored) {}

        // Track supplier metadata
        SupplierSbom supplier = new SupplierSbom();
        supplier.setOrgId(orgId);
        supplier.setReleaseId(releaseId);
        supplier.setSupplierName(supplierName);
        supplier.setSupplierUrl(supplierUrl);
        supplier.setEvidenceId(sbomResult.evidenceId());
        supplier.setComponentCount(sbomResult.componentCount());
        supplier.setFormat(format);
        supplier.setImportedAt(Instant.now());
        supplier.setImportedBy(userId);
        supplier = supplierSbomRepository.save(supplier);

        log.info("Supplier SBOM imported: supplier={}, release={}, components={}",
                supplierName, releaseId, sbomResult.componentCount());

        return toResponse(supplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierSbomResponse> listByRelease(UUID releaseId) {
        UUID orgId = TenantContext.getOrgId();
        return supplierSbomRepository.findAllByReleaseIdAndOrgId(releaseId, orgId)
                .stream().map(this::toResponse).toList();
    }

    public void delete(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        supplierSbomRepository.deleteByIdAndOrgId(id, orgId);
    }

    private SupplierSbomResponse toResponse(SupplierSbom s) {
        return new SupplierSbomResponse(
                s.getId(), s.getSupplierName(), s.getSupplierUrl(),
                s.getEvidenceId(), s.getComponentCount(), s.getFormat(), s.getImportedAt());
    }
}
