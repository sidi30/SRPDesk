package com.lexsecura.application.service;

import com.lexsecura.application.dto.CvdPolicyRequest;
import com.lexsecura.application.dto.CvdPolicyResponse;
import com.lexsecura.domain.model.CvdPolicy;
import com.lexsecura.application.service.EntityNotFoundException;
import com.lexsecura.domain.repository.CvdPolicyRepository;
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
import java.util.stream.Collectors;

/**
 * Service for managing Coordinated Vulnerability Disclosure (CVD) policies.
 * CRA Annexe I §2(5): Manufacturers must establish and enforce a CVD policy.
 * Art. 13(6): Single point of contact for vulnerability reporting.
 */
@Service
@Transactional
public class CvdPolicyService {

    private static final Logger log = LoggerFactory.getLogger(CvdPolicyService.class);

    private final CvdPolicyRepository cvdPolicyRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public CvdPolicyService(CvdPolicyRepository cvdPolicyRepository,
                            ProductRepository productRepository,
                            AuditService auditService) {
        this.cvdPolicyRepository = cvdPolicyRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    public CvdPolicyResponse createOrUpdate(UUID productId, CvdPolicyRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        productRepository.findByIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        CvdPolicy policy = cvdPolicyRepository.findByProductIdAndOrgId(productId, orgId)
                .orElseGet(CvdPolicy::new);

        boolean isNew = policy.getId() == null;

        policy.setOrgId(orgId);
        policy.setProductId(productId);
        policy.setContactEmail(request.contactEmail());
        policy.setContactUrl(request.contactUrl());
        policy.setPgpKeyUrl(request.pgpKeyUrl());
        policy.setPolicyUrl(request.policyUrl());
        if (request.disclosureTimelineDays() != null) {
            policy.setDisclosureTimelineDays(request.disclosureTimelineDays());
        }
        if (request.acceptsAnonymous() != null) {
            policy.setAcceptsAnonymous(request.acceptsAnonymous());
        }
        policy.setBugBountyUrl(request.bugBountyUrl());
        if (request.acceptedLanguages() != null) {
            policy.setAcceptedLanguages(request.acceptedLanguages());
        }
        policy.setScopeDescription(request.scopeDescription());
        if (isNew) {
            policy.setCreatedBy(userId);
            policy.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        }
        policy.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        policy = cvdPolicyRepository.save(policy);

        auditService.record(orgId, "CVD_POLICY", policy.getId(),
                isNew ? "CREATE" : "UPDATE", userId,
                Map.of("productId", productId.toString()));

        log.info("CVD policy {} for product {}", isNew ? "created" : "updated", productId);
        return toResponse(policy);
    }

    @Transactional(readOnly = true)
    public CvdPolicyResponse findByProductId(UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        CvdPolicy policy = cvdPolicyRepository.findByProductIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CVD policy not found for product: " + productId));
        return toResponse(policy);
    }

    @Transactional(readOnly = true)
    public List<CvdPolicyResponse> findAll() {
        UUID orgId = TenantContext.getOrgId();
        return cvdPolicyRepository.findAllByOrgId(orgId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public CvdPolicyResponse publish(UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        CvdPolicy policy = cvdPolicyRepository.findByProductIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CVD policy not found for product: " + productId));

        policy.setStatus("PUBLISHED");
        policy.setPublishedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        policy.setUpdatedAt(policy.getPublishedAt());
        policy = cvdPolicyRepository.save(policy);

        auditService.record(orgId, "CVD_POLICY", policy.getId(), "PUBLISH", userId,
                Map.of("productId", productId.toString()));

        return toResponse(policy);
    }

    private CvdPolicyResponse toResponse(CvdPolicy p) {
        return new CvdPolicyResponse(
                p.getId(), p.getProductId(), p.getContactEmail(), p.getContactUrl(),
                p.getPgpKeyUrl(), p.getPolicyUrl(), p.getDisclosureTimelineDays(),
                p.isAcceptsAnonymous(), p.getBugBountyUrl(), p.getAcceptedLanguages(),
                p.getScopeDescription(), p.getStatus(), p.getPublishedAt(),
                p.getCreatedBy(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
