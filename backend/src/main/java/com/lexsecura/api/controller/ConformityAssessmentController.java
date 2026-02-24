package com.lexsecura.api.controller;

import com.lexsecura.application.dto.ConformityAssessmentResponse;
import com.lexsecura.application.service.ConformityAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Conformity Assessment workflows.
 * CRA Art. 32: Conformity assessment procedures (Module A / Module H).
 */
@RestController
@RequestMapping("/api/v1/products/{productId}/conformity-assessment")
@Tag(name = "Conformity Assessment", description = "Conformity assessment workflow management (CRA Art. 32)")
public class ConformityAssessmentController {

    private final ConformityAssessmentService conformityAssessmentService;

    public ConformityAssessmentController(ConformityAssessmentService conformityAssessmentService) {
        this.conformityAssessmentService = conformityAssessmentService;
    }

    @PostMapping("/initiate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Initiate a conformity assessment for a product")
    public ResponseEntity<ConformityAssessmentResponse> initiate(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "MODULE_A") String module) {
        ConformityAssessmentResponse response = conformityAssessmentService.initiate(productId, module);
        return ResponseEntity.created(
                URI.create("/api/v1/products/" + productId + "/conformity-assessment/" + response.id()))
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Get conformity assessment for a product and module")
    public ResponseEntity<ConformityAssessmentResponse> get(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "MODULE_A") String module) {
        return ResponseEntity.ok(conformityAssessmentService.getAssessment(productId, module));
    }

    @GetMapping("/{assessmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Get conformity assessment by ID")
    public ResponseEntity<ConformityAssessmentResponse> getById(
            @PathVariable UUID productId,
            @PathVariable UUID assessmentId) {
        return ResponseEntity.ok(conformityAssessmentService.getById(assessmentId));
    }

    @PostMapping("/{assessmentId}/steps/{stepIndex}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Complete a step in the conformity assessment")
    public ResponseEntity<ConformityAssessmentResponse> completeStep(
            @PathVariable UUID productId,
            @PathVariable UUID assessmentId,
            @PathVariable int stepIndex,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(conformityAssessmentService.completeStep(assessmentId, stepIndex, notes));
    }

    @PostMapping("/{assessmentId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Approve a completed conformity assessment")
    public ResponseEntity<ConformityAssessmentResponse> approve(
            @PathVariable UUID productId,
            @PathVariable UUID assessmentId) {
        return ResponseEntity.ok(conformityAssessmentService.approve(assessmentId));
    }
}
