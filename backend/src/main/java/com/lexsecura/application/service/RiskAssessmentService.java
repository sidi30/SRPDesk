package com.lexsecura.application.service;

import com.lexsecura.application.dto.RiskAssessmentRequest;
import com.lexsecura.application.dto.RiskAssessmentResponse;
import com.lexsecura.application.dto.RiskItemRequest;
import com.lexsecura.application.dto.RiskItemResponse;
import com.lexsecura.domain.model.RiskAssessment;
import com.lexsecura.domain.model.RiskItem;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.domain.repository.RiskAssessmentRepository;
import com.lexsecura.domain.repository.RiskItemRepository;
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
 * Service for managing cybersecurity risk assessments.
 * CRA Annexe I §1: Products must be designed based on a cybersecurity risk assessment.
 * Art. 13(2): Manufacturers must perform and document risk assessments.
 */
@Service
@Transactional
public class RiskAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentService.class);

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RiskItemRepository riskItemRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public RiskAssessmentService(RiskAssessmentRepository riskAssessmentRepository,
                                 RiskItemRepository riskItemRepository,
                                 ProductRepository productRepository,
                                 AuditService auditService) {
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.riskItemRepository = riskItemRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    public RiskAssessmentResponse create(UUID productId, RiskAssessmentRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        productRepository.findByIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        RiskAssessment assessment = new RiskAssessment();
        assessment.setOrgId(orgId);
        assessment.setProductId(productId);
        assessment.setTitle(request.title());
        assessment.setMethodology(request.methodology() != null ? request.methodology() : "STRIDE");
        assessment.setSummary(request.summary());
        assessment.setStatus("DRAFT");
        assessment.setCreatedBy(userId);
        assessment.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        assessment.setUpdatedAt(assessment.getCreatedAt());

        assessment = riskAssessmentRepository.save(assessment);

        auditService.record(orgId, "RISK_ASSESSMENT", assessment.getId(), "CREATE", userId,
                Map.of("productId", productId.toString(), "title", request.title()));

        log.info("Risk assessment created for product {}", productId);
        return toResponse(assessment, List.of());
    }

    public RiskItemResponse addItem(UUID assessmentId, RiskItemRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        RiskAssessment assessment = riskAssessmentRepository.findByIdAndOrgId(assessmentId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Risk assessment not found: " + assessmentId));

        RiskItem item = new RiskItem();
        item.setRiskAssessmentId(assessmentId);
        item.setThreatCategory(request.threatCategory());
        item.setThreatDescription(request.threatDescription());
        item.setAffectedAsset(request.affectedAsset());
        item.setLikelihood(request.likelihood());
        item.setImpact(request.impact());
        item.setRiskLevel(RiskItem.computeRiskLevel(request.likelihood(), request.impact()));
        item.setExistingControls(request.existingControls());
        item.setMitigationPlan(request.mitigationPlan());
        item.setMitigationStatus("PENDING");
        item.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        item.setUpdatedAt(item.getCreatedAt());

        item = riskItemRepository.save(item);

        recalculateOverallRisk(assessmentId, orgId);

        auditService.record(orgId, "RISK_ITEM", item.getId(), "CREATE", userId,
                Map.of("assessmentId", assessmentId.toString(), "threatCategory", request.threatCategory()));

        log.info("Risk item added to assessment {}", assessmentId);
        return toItemResponse(item);
    }

    public RiskItemResponse updateItem(UUID itemId, RiskItemRequest request) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        RiskItem item = riskItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Risk item not found: " + itemId));

        item.setThreatCategory(request.threatCategory());
        item.setThreatDescription(request.threatDescription());
        item.setAffectedAsset(request.affectedAsset());
        item.setLikelihood(request.likelihood());
        item.setImpact(request.impact());
        item.setRiskLevel(RiskItem.computeRiskLevel(request.likelihood(), request.impact()));
        item.setExistingControls(request.existingControls());
        item.setMitigationPlan(request.mitigationPlan());
        item.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        item = riskItemRepository.save(item);

        recalculateOverallRisk(item.getRiskAssessmentId(), orgId);

        auditService.record(orgId, "RISK_ITEM", item.getId(), "UPDATE", userId,
                Map.of("assessmentId", item.getRiskAssessmentId().toString()));

        log.info("Risk item updated: {}", itemId);
        return toItemResponse(item);
    }

    public void deleteItem(UUID assessmentId, UUID itemId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        riskItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Risk item not found: " + itemId));

        riskItemRepository.deleteById(itemId);

        recalculateOverallRisk(assessmentId, orgId);

        auditService.record(orgId, "RISK_ITEM", itemId, "DELETE", userId,
                Map.of("assessmentId", assessmentId.toString()));

        log.info("Risk item deleted: {} from assessment {}", itemId, assessmentId);
    }

    public RiskAssessmentResponse submitForReview(UUID assessmentId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        RiskAssessment assessment = riskAssessmentRepository.findByIdAndOrgId(assessmentId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Risk assessment not found: " + assessmentId));

        assessment.setStatus("IN_REVIEW");
        assessment.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        assessment = riskAssessmentRepository.save(assessment);

        auditService.record(orgId, "RISK_ASSESSMENT", assessment.getId(), "SUBMIT_FOR_REVIEW", userId,
                Map.of("productId", assessment.getProductId().toString()));

        log.info("Risk assessment submitted for review: {}", assessmentId);
        List<RiskItem> items = riskItemRepository.findAllByRiskAssessmentId(assessmentId);
        return toResponse(assessment, items);
    }

    public RiskAssessmentResponse approve(UUID assessmentId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        RiskAssessment assessment = riskAssessmentRepository.findByIdAndOrgId(assessmentId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Risk assessment not found: " + assessmentId));

        assessment.setStatus("APPROVED");
        assessment.setApprovedBy(userId);
        assessment.setApprovedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        assessment.setUpdatedAt(assessment.getApprovedAt());

        assessment = riskAssessmentRepository.save(assessment);

        auditService.record(orgId, "RISK_ASSESSMENT", assessment.getId(), "APPROVE", userId,
                Map.of("productId", assessment.getProductId().toString()));

        log.info("Risk assessment approved: {}", assessmentId);
        List<RiskItem> items = riskItemRepository.findAllByRiskAssessmentId(assessmentId);
        return toResponse(assessment, items);
    }

    @Transactional(readOnly = true)
    public List<RiskAssessmentResponse> findByProductId(UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        return riskAssessmentRepository.findAllByProductIdAndOrgId(productId, orgId).stream()
                .map(a -> {
                    List<RiskItem> items = riskItemRepository.findAllByRiskAssessmentId(a.getId());
                    return toResponse(a, items);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public RiskAssessmentResponse findById(UUID assessmentId) {
        UUID orgId = TenantContext.getOrgId();
        RiskAssessment assessment = riskAssessmentRepository.findByIdAndOrgId(assessmentId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Risk assessment not found: " + assessmentId));
        List<RiskItem> items = riskItemRepository.findAllByRiskAssessmentId(assessmentId);
        return toResponse(assessment, items);
    }

    private void recalculateOverallRisk(UUID assessmentId, UUID orgId) {
        RiskAssessment assessment = riskAssessmentRepository.findByIdAndOrgId(assessmentId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Risk assessment not found: " + assessmentId));

        List<RiskItem> items = riskItemRepository.findAllByRiskAssessmentId(assessmentId);

        if (items.isEmpty()) {
            assessment.setOverallRiskLevel(null);
        } else {
            // Take the highest risk level from all items
            String highest = items.stream()
                    .map(RiskItem::getRiskLevel)
                    .reduce((a, b) -> riskPriority(a) >= riskPriority(b) ? a : b)
                    .orElse(null);
            assessment.setOverallRiskLevel(highest);
        }

        assessment.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        riskAssessmentRepository.save(assessment);
    }

    private int riskPriority(String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    private RiskAssessmentResponse toResponse(RiskAssessment a, List<RiskItem> items) {
        List<RiskItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();

        return new RiskAssessmentResponse(
                a.getId(), a.getProductId(), a.getTitle(), a.getMethodology(),
                a.getStatus(), a.getOverallRiskLevel(), a.getSummary(),
                a.getApprovedBy(), a.getApprovedAt(),
                a.getCreatedBy(), a.getCreatedAt(), a.getUpdatedAt(),
                itemResponses
        );
    }

    private RiskItemResponse toItemResponse(RiskItem i) {
        return new RiskItemResponse(
                i.getId(), i.getRiskAssessmentId(),
                i.getThreatCategory(), i.getThreatDescription(), i.getAffectedAsset(),
                i.getLikelihood(), i.getImpact(), i.getRiskLevel(),
                i.getExistingControls(), i.getMitigationPlan(),
                i.getMitigationStatus(), i.getResidualRiskLevel(),
                i.getCreatedAt(), i.getUpdatedAt()
        );
    }
}
