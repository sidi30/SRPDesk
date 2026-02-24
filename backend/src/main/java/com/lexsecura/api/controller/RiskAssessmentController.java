package com.lexsecura.api.controller;

import com.lexsecura.application.dto.RiskAssessmentRequest;
import com.lexsecura.application.dto.RiskAssessmentResponse;
import com.lexsecura.application.dto.RiskItemRequest;
import com.lexsecura.application.dto.RiskItemResponse;
import com.lexsecura.application.service.RiskAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for cybersecurity risk assessments.
 * CRA Annexe I §1 + Art. 13(2): Risk assessment requirements.
 */
@RestController
@RequestMapping("/api/v1/products/{productId}/risk-assessments")
@Tag(name = "Risk Assessment", description = "Cybersecurity risk assessment management (CRA Annexe I §1)")
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;

    public RiskAssessmentController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Create a new risk assessment for a product")
    public ResponseEntity<RiskAssessmentResponse> create(
            @PathVariable UUID productId,
            @Valid @RequestBody RiskAssessmentRequest request) {
        RiskAssessmentResponse response = riskAssessmentService.create(productId, request);
        return ResponseEntity.created(
                URI.create("/api/v1/products/" + productId + "/risk-assessments/" + response.id()))
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "List risk assessments for a product")
    public ResponseEntity<List<RiskAssessmentResponse>> list(@PathVariable UUID productId) {
        return ResponseEntity.ok(riskAssessmentService.findByProductId(productId));
    }

    @GetMapping("/{assessmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Get a risk assessment by ID (with items)")
    public ResponseEntity<RiskAssessmentResponse> getById(
            @PathVariable UUID productId,
            @PathVariable UUID assessmentId) {
        return ResponseEntity.ok(riskAssessmentService.findById(assessmentId));
    }

    @PostMapping("/{assessmentId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Add a risk item to an assessment")
    public ResponseEntity<RiskItemResponse> addItem(
            @PathVariable UUID productId,
            @PathVariable UUID assessmentId,
            @Valid @RequestBody RiskItemRequest request) {
        RiskItemResponse response = riskAssessmentService.addItem(assessmentId, request);
        return ResponseEntity.created(
                URI.create("/api/v1/products/" + productId + "/risk-assessments/" + assessmentId + "/items/" + response.id()))
                .body(response);
    }

    @PutMapping("/{assessmentId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Update a risk item")
    public ResponseEntity<RiskItemResponse> updateItem(
            @PathVariable UUID productId,
            @PathVariable UUID assessmentId,
            @PathVariable UUID itemId,
            @Valid @RequestBody RiskItemRequest request) {
        return ResponseEntity.ok(riskAssessmentService.updateItem(itemId, request));
    }

    @DeleteMapping("/{assessmentId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Delete a risk item from an assessment")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID productId,
            @PathVariable UUID assessmentId,
            @PathVariable UUID itemId) {
        riskAssessmentService.deleteItem(assessmentId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{assessmentId}/submit-review")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Submit a risk assessment for review")
    public ResponseEntity<RiskAssessmentResponse> submitForReview(
            @PathVariable UUID productId,
            @PathVariable UUID assessmentId) {
        return ResponseEntity.ok(riskAssessmentService.submitForReview(assessmentId));
    }

    @PostMapping("/{assessmentId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Approve a risk assessment")
    public ResponseEntity<RiskAssessmentResponse> approve(
            @PathVariable UUID productId,
            @PathVariable UUID assessmentId) {
        return ResponseEntity.ok(riskAssessmentService.approve(assessmentId));
    }
}
