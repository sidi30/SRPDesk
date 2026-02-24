package com.lexsecura.api.controller;

import com.lexsecura.application.dto.AppliedStandardRequest;
import com.lexsecura.application.dto.AppliedStandardResponse;
import com.lexsecura.application.service.AppliedStandardService;
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
 * REST controller for applied harmonised standards.
 * CRA Art. 27: Presumption of conformity through harmonised standards.
 */
@RestController
@RequestMapping("/api/v1/products/{productId}/standards")
@Tag(name = "Applied Standards", description = "Applied harmonised standards management (CRA Art. 27)")
public class AppliedStandardController {

    private final AppliedStandardService appliedStandardService;

    public AppliedStandardController(AppliedStandardService appliedStandardService) {
        this.appliedStandardService = appliedStandardService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Add an applied standard to a product")
    public ResponseEntity<AppliedStandardResponse> create(
            @PathVariable UUID productId,
            @Valid @RequestBody AppliedStandardRequest request) {
        AppliedStandardResponse response = appliedStandardService.create(productId, request);
        return ResponseEntity.created(
                URI.create("/api/v1/products/" + productId + "/standards/" + response.id()))
                .body(response);
    }

    @PutMapping("/{standardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Update an applied standard")
    public ResponseEntity<AppliedStandardResponse> update(
            @PathVariable UUID productId,
            @PathVariable UUID standardId,
            @Valid @RequestBody AppliedStandardRequest request) {
        return ResponseEntity.ok(appliedStandardService.update(standardId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "List applied standards for a product")
    public ResponseEntity<List<AppliedStandardResponse>> list(@PathVariable UUID productId) {
        return ResponseEntity.ok(appliedStandardService.findByProductId(productId));
    }

    @DeleteMapping("/{standardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Remove an applied standard")
    public ResponseEntity<Void> delete(
            @PathVariable UUID productId,
            @PathVariable UUID standardId) {
        appliedStandardService.delete(standardId);
        return ResponseEntity.noContent().build();
    }
}
