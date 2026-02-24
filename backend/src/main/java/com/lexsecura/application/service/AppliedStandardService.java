package com.lexsecura.application.service;

import com.lexsecura.application.dto.AppliedStandardRequest;
import com.lexsecura.application.dto.AppliedStandardResponse;
import com.lexsecura.domain.model.AppliedStandard;
import com.lexsecura.domain.repository.AppliedStandardRepository;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing applied harmonised standards.
 * CRA Art. 27: Presumption of conformity when harmonised standards are applied.
 */
@Service
@Transactional
public class AppliedStandardService {

    private static final Logger log = LoggerFactory.getLogger(AppliedStandardService.class);

    private final AppliedStandardRepository appliedStandardRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public AppliedStandardService(AppliedStandardRepository appliedStandardRepository,
                                  ProductRepository productRepository,
                                  AuditService auditService) {
        this.appliedStandardRepository = appliedStandardRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    public AppliedStandardResponse create(UUID productId, AppliedStandardRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        productRepository.findByIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        AppliedStandard standard = new AppliedStandard();
        standard.setOrgId(orgId);
        standard.setProductId(productId);
        applyRequest(standard, request);
        standard.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        standard.setUpdatedAt(standard.getCreatedAt());

        standard = appliedStandardRepository.save(standard);

        auditService.record(orgId, "APPLIED_STANDARD", standard.getId(), "CREATE", userId,
                Map.of("productId", productId.toString(), "standardCode", request.standardCode()));

        log.info("Applied standard {} created for product {}", request.standardCode(), productId);
        return toResponse(standard);
    }

    public AppliedStandardResponse update(UUID standardId, AppliedStandardRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        AppliedStandard standard = appliedStandardRepository.findByIdAndOrgId(standardId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Applied standard not found: " + standardId));

        applyRequest(standard, request);
        standard.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        standard = appliedStandardRepository.save(standard);

        auditService.record(orgId, "APPLIED_STANDARD", standard.getId(), "UPDATE", userId,
                Map.of("standardCode", request.standardCode()));

        log.info("Applied standard updated: {}", standardId);
        return toResponse(standard);
    }

    @Transactional(readOnly = true)
    public List<AppliedStandardResponse> findByProductId(UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        return appliedStandardRepository.findAllByProductIdAndOrgId(productId, orgId).stream()
                .map(this::toResponse).toList();
    }

    public void delete(UUID standardId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        appliedStandardRepository.findByIdAndOrgId(standardId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Applied standard not found: " + standardId));

        appliedStandardRepository.deleteById(standardId);

        auditService.record(orgId, "APPLIED_STANDARD", standardId, "DELETE", userId,
                Map.of());

        log.info("Applied standard deleted: {}", standardId);
    }

    private void applyRequest(AppliedStandard standard, AppliedStandardRequest request) {
        standard.setStandardCode(request.standardCode());
        standard.setStandardTitle(request.standardTitle());
        standard.setVersion(request.version());
        standard.setComplianceStatus(
                request.complianceStatus() != null ? request.complianceStatus() : "CLAIMED");
        standard.setNotes(request.notes());
        standard.setEvidenceIds(request.evidenceIds());
    }

    private AppliedStandardResponse toResponse(AppliedStandard s) {
        return new AppliedStandardResponse(
                s.getId(), s.getProductId(), s.getStandardCode(), s.getStandardTitle(),
                s.getVersion(), s.getComplianceStatus(), s.getNotes(), s.getEvidenceIds(),
                s.getCreatedAt(), s.getUpdatedAt()
        );
    }
}
