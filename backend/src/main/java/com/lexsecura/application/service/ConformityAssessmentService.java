package com.lexsecura.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.ConformityAssessmentResponse;
import com.lexsecura.domain.model.ConformityAssessment;
import com.lexsecura.domain.repository.ConformityAssessmentRepository;
import com.lexsecura.domain.repository.ProductRepository;
import com.lexsecura.infrastructure.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service for managing Conformity Assessment workflows.
 * CRA Art. 32: Conformity assessment procedures (Module A internal control,
 * Module H full quality assurance for critical products).
 */
@Service
@Transactional
public class ConformityAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(ConformityAssessmentService.class);

    private final ConformityAssessmentRepository conformityAssessmentRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    private static final List<Map<String, Object>> MODULE_A_STEPS = List.of(
            stepDef("Technical Documentation", "Prepare complete technical documentation per Annex VII"),
            stepDef("Risk Assessment", "Perform cybersecurity risk assessment per Annex I"),
            stepDef("Security Testing", "Conduct security testing and vulnerability analysis"),
            stepDef("SBOM Generation", "Generate Software Bill of Materials"),
            stepDef("Vulnerability Handling Verification", "Verify vulnerability handling procedures are in place"),
            stepDef("Internal Review", "Internal review of all conformity evidence"),
            stepDef("EU Declaration of Conformity", "Draw up EU declaration of conformity per Annex V"),
            stepDef("CE Marking", "Affix CE marking per Art. 30")
    );

    private static final List<Map<String, Object>> MODULE_H_STEPS = List.of(
            stepDef("Quality System Documentation", "Document quality management system"),
            stepDef("Technical Documentation", "Prepare complete technical documentation per Annex VII"),
            stepDef("Risk Assessment", "Perform cybersecurity risk assessment per Annex I"),
            stepDef("Design Examination", "Notified body examines product design"),
            stepDef("Production Quality Assurance", "Verify production quality assurance processes"),
            stepDef("Third-Party Audit", "Third-party audit of quality management system"),
            stepDef("Notified Body Review", "Notified body reviews conformity assessment"),
            stepDef("Certificate Issuance", "Notified body issues EU-type examination certificate"),
            stepDef("EU Declaration of Conformity", "Draw up EU declaration of conformity per Annex V"),
            stepDef("CE Marking", "Affix CE marking per Art. 30")
    );

    public ConformityAssessmentService(ConformityAssessmentRepository conformityAssessmentRepository,
                                       ProductRepository productRepository,
                                       AuditService auditService,
                                       ObjectMapper objectMapper) {
        this.conformityAssessmentRepository = conformityAssessmentRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public ConformityAssessmentResponse initiate(UUID productId, String module) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        productRepository.findByIdAndOrgId(productId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        // Check if assessment already exists for this product/module
        Optional<ConformityAssessment> existing =
                conformityAssessmentRepository.findByProductIdAndModuleAndOrgId(productId, module, orgId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Assessment already exists for product " + productId + " module " + module);
        }

        List<Map<String, Object>> steps = "MODULE_H".equals(module) ? MODULE_H_STEPS : MODULE_A_STEPS;
        List<Map<String, Object>> stepsWithState = new ArrayList<>();
        for (Map<String, Object> step : steps) {
            Map<String, Object> s = new LinkedHashMap<>(step);
            s.put("status", "PENDING");
            s.put("completedAt", null);
            s.put("notes", null);
            s.put("evidenceIds", List.of());
            stepsWithState.add(s);
        }

        String stepsJson;
        try {
            stepsJson = objectMapper.writeValueAsString(stepsWithState);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize steps data", e);
        }

        ConformityAssessment assessment = new ConformityAssessment();
        assessment.setOrgId(orgId);
        assessment.setProductId(productId);
        assessment.setModule(module);
        assessment.setStatus("IN_PROGRESS");
        assessment.setCurrentStep(0);
        assessment.setTotalSteps(stepsWithState.size());
        assessment.setStepsData(stepsJson);
        assessment.setStartedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        assessment.setCreatedBy(userId);
        assessment.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        assessment.setUpdatedAt(assessment.getCreatedAt());

        assessment = conformityAssessmentRepository.save(assessment);

        auditService.record(orgId, "CONFORMITY_ASSESSMENT", assessment.getId(), "INITIATE", userId,
                Map.of("productId", productId.toString(), "module", module));

        log.info("Conformity assessment initiated for product {} module {}", productId, module);
        return toResponse(assessment);
    }

    public ConformityAssessmentResponse completeStep(UUID assessmentId, int stepIndex, String notes) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        ConformityAssessment assessment = conformityAssessmentRepository.findByIdAndOrgId(assessmentId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found: " + assessmentId));

        List<Map<String, Object>> steps;
        try {
            steps = objectMapper.readValue(assessment.getStepsData(),
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse steps data", e);
        }

        if (stepIndex < 0 || stepIndex >= steps.size()) {
            throw new IllegalArgumentException("Invalid step index: " + stepIndex);
        }

        Map<String, Object> step = steps.get(stepIndex);
        step.put("status", "COMPLETED");
        step.put("completedAt", Instant.now().truncatedTo(ChronoUnit.MICROS).toString());
        if (notes != null) {
            step.put("notes", notes);
        }

        // Update currentStep to next incomplete step
        int nextStep = assessment.getCurrentStep();
        for (int i = 0; i < steps.size(); i++) {
            if (!"COMPLETED".equals(steps.get(i).get("status"))) {
                nextStep = i;
                break;
            }
            if (i == steps.size() - 1) {
                nextStep = steps.size();
            }
        }

        try {
            assessment.setStepsData(objectMapper.writeValueAsString(steps));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize steps data", e);
        }

        assessment.setCurrentStep(nextStep);
        assessment.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));

        // Check if all steps are completed
        boolean allCompleted = steps.stream()
                .allMatch(s -> "COMPLETED".equals(s.get("status")));
        if (allCompleted) {
            assessment.setStatus("COMPLETED");
            assessment.setCompletedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        }

        assessment = conformityAssessmentRepository.save(assessment);

        auditService.record(orgId, "CONFORMITY_ASSESSMENT", assessment.getId(), "COMPLETE_STEP", userId,
                Map.of("stepIndex", String.valueOf(stepIndex), "stepName", String.valueOf(step.get("name"))));

        log.info("Conformity assessment step {} completed for assessment {}", stepIndex, assessmentId);
        return toResponse(assessment);
    }

    @Transactional(readOnly = true)
    public ConformityAssessmentResponse getAssessment(UUID productId, String module) {
        UUID orgId = TenantContext.getOrgId();
        ConformityAssessment assessment = conformityAssessmentRepository
                .findByProductIdAndModuleAndOrgId(productId, module, orgId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Assessment not found for product " + productId + " module " + module));
        return toResponse(assessment);
    }

    @Transactional(readOnly = true)
    public ConformityAssessmentResponse getById(UUID assessmentId) {
        UUID orgId = TenantContext.getOrgId();
        ConformityAssessment assessment = conformityAssessmentRepository.findByIdAndOrgId(assessmentId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found: " + assessmentId));
        return toResponse(assessment);
    }

    public ConformityAssessmentResponse approve(UUID assessmentId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();

        ConformityAssessment assessment = conformityAssessmentRepository.findByIdAndOrgId(assessmentId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Assessment not found: " + assessmentId));

        assessment.setStatus("APPROVED");
        assessment.setApprovedBy(userId);
        assessment.setApprovedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        assessment.setUpdatedAt(assessment.getApprovedAt());

        assessment = conformityAssessmentRepository.save(assessment);

        auditService.record(orgId, "CONFORMITY_ASSESSMENT", assessment.getId(), "APPROVE", userId,
                Map.of("productId", assessment.getProductId().toString()));

        log.info("Conformity assessment approved: {}", assessmentId);
        return toResponse(assessment);
    }

    private ConformityAssessmentResponse toResponse(ConformityAssessment a) {
        return new ConformityAssessmentResponse(
                a.getId(), a.getProductId(), a.getModule(), a.getStatus(),
                a.getCurrentStep(), a.getTotalSteps(), a.getStepsData(),
                a.getStartedAt(), a.getCompletedAt(),
                a.getApprovedBy(), a.getApprovedAt(),
                a.getCreatedBy(), a.getCreatedAt(), a.getUpdatedAt()
        );
    }

    private static Map<String, Object> stepDef(String name, String description) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("name", name);
        step.put("description", description);
        return step;
    }
}
