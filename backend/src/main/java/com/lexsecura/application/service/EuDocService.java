package com.lexsecura.application.service;

import com.lexsecura.application.dto.EuDocRequest;
import com.lexsecura.application.dto.EuDocResponse;
import com.lexsecura.domain.model.EuDeclarationOfConformity;
import com.lexsecura.domain.repository.EuDocRepository;
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
 * Service for managing EU Declarations of Conformity (Annex V).
 * CRA Art. 28: Manufacturers must draw up an EU declaration of conformity.
 */
@Service
@Transactional
public class EuDocService {

    private static final Logger log = LoggerFactory.getLogger(EuDocService.class);

    private final EuDocRepository euDocRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public EuDocService(EuDocRepository euDocRepository,
                        ProductRepository productRepository,
                        AuditService auditService) {
        this.euDocRepository = euDocRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    public EuDocResponse create(UUID productId, EuDocRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        productRepository.findByIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        EuDeclarationOfConformity doc = new EuDeclarationOfConformity();
        doc.setOrgId(orgId);
        doc.setProductId(productId);
        applyRequest(doc, request);
        doc.setStatus("DRAFT");
        doc.setCreatedBy(userId);
        doc.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        doc.setUpdatedAt(doc.getCreatedAt());

        doc = euDocRepository.save(doc);

        auditService.record(orgId, "EU_DOC", doc.getId(), "CREATE", userId,
                Map.of("productId", productId.toString()));

        log.info("EU Declaration of Conformity created for product {}", productId);
        return toResponse(doc);
    }

    public EuDocResponse update(UUID docId, EuDocRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        EuDeclarationOfConformity doc = euDocRepository.findByIdAndOrgId(docId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("EU DoC not found: " + docId));

        if (!"DRAFT".equals(doc.getStatus())) {
            throw new IllegalStateException("Cannot update EU DoC in status: " + doc.getStatus());
        }

        applyRequest(doc, request);
        doc.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        doc = euDocRepository.save(doc);

        auditService.record(orgId, "EU_DOC", doc.getId(), "UPDATE", userId,
                Map.of("productId", doc.getProductId().toString()));

        log.info("EU Declaration of Conformity updated: {}", docId);
        return toResponse(doc);
    }

    public EuDocResponse sign(UUID docId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        EuDeclarationOfConformity doc = euDocRepository.findByIdAndOrgId(docId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("EU DoC not found: " + docId));

        doc.setStatus("SIGNED");
        doc.setSignedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        doc.setUpdatedAt(doc.getSignedAt());

        doc = euDocRepository.save(doc);

        auditService.record(orgId, "EU_DOC", doc.getId(), "SIGN", userId,
                Map.of("productId", doc.getProductId().toString()));

        log.info("EU Declaration of Conformity signed: {}", docId);
        return toResponse(doc);
    }

    public EuDocResponse publish(UUID docId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        EuDeclarationOfConformity doc = euDocRepository.findByIdAndOrgId(docId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("EU DoC not found: " + docId));

        doc.setStatus("PUBLISHED");
        doc.setPublishedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        doc.setUpdatedAt(doc.getPublishedAt());

        doc = euDocRepository.save(doc);

        auditService.record(orgId, "EU_DOC", doc.getId(), "PUBLISH", userId,
                Map.of("productId", doc.getProductId().toString()));

        log.info("EU Declaration of Conformity published: {}", docId);
        return toResponse(doc);
    }

    @Transactional(readOnly = true)
    public List<EuDocResponse> findByProductId(UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        return euDocRepository.findAllByProductIdAndOrgId(productId, orgId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EuDocResponse findById(UUID docId) {
        UUID orgId = TenantContext.getOrgId();
        EuDeclarationOfConformity doc = euDocRepository.findByIdAndOrgId(docId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("EU DoC not found: " + docId));
        return toResponse(doc);
    }

    private void applyRequest(EuDeclarationOfConformity doc, EuDocRequest request) {
        doc.setDeclarationNumber(request.declarationNumber());
        doc.setManufacturerName(request.manufacturerName());
        doc.setManufacturerAddress(request.manufacturerAddress());
        doc.setAuthorizedRepName(request.authorizedRepName());
        doc.setAuthorizedRepAddress(request.authorizedRepAddress());
        doc.setProductName(request.productName());
        doc.setProductIdentification(request.productIdentification());
        doc.setConformityAssessmentModule(
                request.conformityAssessmentModule() != null ? request.conformityAssessmentModule() : "MODULE_A");
        doc.setNotifiedBodyName(request.notifiedBodyName());
        doc.setNotifiedBodyNumber(request.notifiedBodyNumber());
        doc.setNotifiedBodyCertificate(request.notifiedBodyCertificate());
        doc.setHarmonisedStandards(request.harmonisedStandards());
        doc.setAdditionalInfo(request.additionalInfo());
        doc.setDeclarationText(request.declarationText());
        doc.setSignedBy(request.signedBy());
        doc.setSignedRole(request.signedRole());
    }

    private EuDocResponse toResponse(EuDeclarationOfConformity d) {
        return new EuDocResponse(
                d.getId(), d.getProductId(), d.getDeclarationNumber(),
                d.getManufacturerName(), d.getManufacturerAddress(),
                d.getAuthorizedRepName(), d.getAuthorizedRepAddress(),
                d.getProductName(), d.getProductIdentification(),
                d.getConformityAssessmentModule(),
                d.getNotifiedBodyName(), d.getNotifiedBodyNumber(), d.getNotifiedBodyCertificate(),
                d.getHarmonisedStandards(), d.getAdditionalInfo(),
                d.getDeclarationText(), d.getSignedBy(), d.getSignedRole(),
                d.getSignedAt(), d.getStatus(), d.getPublishedAt(),
                d.getCreatedBy(), d.getCreatedAt(), d.getUpdatedAt()
        );
    }
}
