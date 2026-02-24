package com.lexsecura.application.service;

import com.lexsecura.application.dto.ShareLinkCreateRequest;
import com.lexsecura.application.dto.ShareLinkResponse;
import com.lexsecura.domain.model.Evidence;
import com.lexsecura.domain.model.EvidenceType;
import com.lexsecura.domain.model.SbomShareLink;
import com.lexsecura.domain.repository.EvidenceRepository;
import com.lexsecura.domain.repository.SbomShareLinkRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SbomShareService {

    private static final Logger log = LoggerFactory.getLogger(SbomShareService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SbomShareLinkRepository shareLinkRepository;
    private final EvidenceRepository evidenceRepository;

    public SbomShareService(SbomShareLinkRepository shareLinkRepository,
                            EvidenceRepository evidenceRepository) {
        this.shareLinkRepository = shareLinkRepository;
        this.evidenceRepository = evidenceRepository;
    }

    public ShareLinkResponse createShareLink(UUID releaseId, ShareLinkCreateRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        // Find the SBOM evidence for this release
        Evidence sbomEvidence = evidenceRepository.findAllByReleaseIdAndOrgId(releaseId, orgId).stream()
                .filter(e -> e.getType() == EvidenceType.SBOM)
                .reduce((a, b) -> b)
                .orElseThrow(() -> new IllegalStateException("No SBOM found for release " + releaseId));

        String token = generateToken();
        int hours = request.expiresInHours() > 0 ? request.expiresInHours() : 72;

        SbomShareLink link = new SbomShareLink();
        link.setOrgId(orgId);
        link.setReleaseId(releaseId);
        link.setEvidenceId(sbomEvidence.getId());
        link.setToken(token);
        link.setRecipientEmail(request.recipientEmail());
        link.setRecipientOrg(request.recipientOrg());
        link.setExpiresAt(Instant.now().plus(hours, ChronoUnit.HOURS));
        link.setMaxDownloads(request.maxDownloads());
        link.setDownloadCount(0);
        link.setIncludeVex(request.includeVex());
        link.setIncludeQualityScore(request.includeQualityScore());
        link.setCreatedBy(userId);
        link.setCreatedAt(Instant.now());
        link.setRevoked(false);
        link = shareLinkRepository.save(link);

        log.info("Share link created for release {}: recipient={}, expires={}h",
                releaseId, request.recipientEmail(), hours);

        return toResponse(link);
    }

    @Transactional(readOnly = true)
    public List<ShareLinkResponse> listByRelease(UUID releaseId) {
        UUID orgId = TenantContext.getOrgId();
        return shareLinkRepository.findAllByReleaseIdAndOrgId(releaseId, orgId)
                .stream().map(this::toResponse).toList();
    }

    public void revoke(UUID linkId) {
        UUID orgId = TenantContext.getOrgId();
        SbomShareLink link = shareLinkRepository.findByIdAndOrgId(linkId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Share link not found"));
        link.setRevoked(true);
        link.setRevokedAt(Instant.now());
        shareLinkRepository.save(link);
        log.info("Share link revoked: {}", linkId);
    }

    /**
     * Validates a token and increments download count. Used by public download endpoint.
     */
    public SbomShareLink validateAndConsume(String token) {
        SbomShareLink link = shareLinkRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid share link"));

        if (!link.isValid()) {
            throw new IllegalStateException("Share link expired, revoked, or download limit reached");
        }

        link.setDownloadCount(link.getDownloadCount() + 1);
        return shareLinkRepository.save(link);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private ShareLinkResponse toResponse(SbomShareLink link) {
        return new ShareLinkResponse(
                link.getId(),
                link.getToken(),
                "/share/sbom/" + link.getToken(),
                link.getRecipientEmail(),
                link.getRecipientOrg(),
                link.getExpiresAt(),
                link.getMaxDownloads(),
                link.getDownloadCount(),
                link.isIncludeVex(),
                link.isIncludeQualityScore(),
                link.isRevoked(),
                link.getCreatedAt()
        );
    }
}
