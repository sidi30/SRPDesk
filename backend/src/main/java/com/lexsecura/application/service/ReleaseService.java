package com.lexsecura.application.service;

import com.lexsecura.application.dto.ReleaseCreateRequest;
import com.lexsecura.application.dto.ReleaseResponse;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.domain.repository.ReleaseRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public ReleaseService(ReleaseRepository releaseRepository, ProductRepository productRepository,
                          AuditService auditService) {
        this.releaseRepository = releaseRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    public ReleaseResponse create(UUID productId, ReleaseCreateRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();
        productRepository.findByIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        Release release = new Release(productId, request.version());
        release.setOrgId(orgId);
        release.setGitRef(request.gitRef());
        release.setBuildId(request.buildId());
        release.setReleasedAt(request.releasedAt());
        release.setSupportedUntil(request.supportedUntil());

        release = releaseRepository.save(release);

        auditService.record(orgId, "RELEASE", release.getId(), "CREATE", userId,
                Map.of("productId", productId.toString(), "version", release.getVersion()));

        return toResponse(release);
    }

    @Transactional(readOnly = true)
    public List<ReleaseResponse> findAllByProductId(UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        productRepository.findByIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        return releaseRepository.findAllByProductId(productId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReleaseResponse findById(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        Release release = releaseRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found: " + id));
        return toResponse(release);
    }

    public Release getRelease(UUID id) {
        UUID orgId = TenantContext.getOrgId();
        return releaseRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found: " + id));
    }

    private ReleaseResponse toResponse(Release r) {
        return new ReleaseResponse(
                r.getId(), r.getProductId(), r.getVersion(),
                r.getGitRef(), r.getBuildId(), r.getReleasedAt(),
                r.getSupportedUntil(), r.getStatus().name(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
