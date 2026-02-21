package com.lexsecura.application.service;

import com.lexsecura.application.dto.EvidenceResponse;
import com.lexsecura.application.port.StoragePort;
import com.lexsecura.domain.model.Evidence;
import com.lexsecura.domain.model.EvidenceType;
import com.lexsecura.domain.repository.EvidenceRepository;
import com.lexsecura.domain.repository.ReleaseRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import io.micrometer.core.instrument.Counter;
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
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class EvidenceService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceService.class);

    private final EvidenceRepository evidenceRepository;
    private final ReleaseRepository releaseRepository;
    private final StoragePort storagePort;
    private final Counter evidencesUploadedCounter;
    private final AuditService auditService;

    public EvidenceService(EvidenceRepository evidenceRepository,
                           ReleaseRepository releaseRepository,
                           StoragePort storagePort,
                           Counter evidencesUploadedCounter,
                           AuditService auditService) {
        this.evidenceRepository = evidenceRepository;
        this.releaseRepository = releaseRepository;
        this.storagePort = storagePort;
        this.evidencesUploadedCounter = evidencesUploadedCounter;
        this.auditService = auditService;
    }

    public EvidenceResponse upload(UUID releaseId, String evidenceType, MultipartFile file) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        releaseRepository.findById(releaseId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found: " + releaseId));

        EvidenceType type = EvidenceType.valueOf(evidenceType);

        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();
        String storageUri = orgId + "/releases/" + releaseId + "/" + UUID.randomUUID() + "_" + filename;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = file.getInputStream();
                 DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest)) {

                storagePort.upload(storageUri, digestInputStream, size, contentType);
            }

            String sha256 = HexFormat.of().formatHex(digest.digest());

            Evidence evidence = new Evidence(releaseId, orgId, type, filename,
                    contentType, size, sha256, storageUri, userId);
            evidence = evidenceRepository.save(evidence);

            evidencesUploadedCounter.increment();
            log.info("Evidence uploaded: id={}, sha256={}, size={}", evidence.getId(), sha256, size);

            auditService.record(orgId, "EVIDENCE", evidence.getId(), "CREATE", userId,
                    Map.of("releaseId", releaseId.toString(), "type", type.name(),
                            "filename", filename, "sha256", sha256));

            return toResponse(evidence);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }

    @Transactional(readOnly = true)
    public List<EvidenceResponse> findAllByReleaseId(UUID releaseId) {
        UUID orgId = TenantContext.getOrgId();
        return evidenceRepository.findAllByReleaseIdAndOrgId(releaseId, orgId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Evidence getEvidence(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        return evidenceRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Evidence not found: " + id));
    }

    @Transactional(readOnly = true)
    public InputStream download(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        Evidence evidence = evidenceRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Evidence not found: " + id));
        return storagePort.download(evidence.getStorageUri());
    }

    public void delete(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();
        Evidence evidence = evidenceRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Evidence not found: " + id));

        storagePort.delete(evidence.getStorageUri());
        evidenceRepository.deleteByIdAndOrgId(id, orgId);
        log.info("Evidence deleted: id={}", id);

        auditService.record(orgId, "EVIDENCE", id, "DELETE", userId,
                Map.of("filename", evidence.getFilename(), "sha256", evidence.getSha256()));
    }

    private EvidenceResponse toResponse(Evidence e) {
        return new EvidenceResponse(
                e.getId(), e.getReleaseId(), e.getOrgId(),
                e.getType().name(), e.getFilename(), e.getContentType(),
                e.getSize(), e.getSha256(), e.getCreatedAt(), e.getCreatedBy());
    }
}
