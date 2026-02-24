package com.lexsecura.api.controller;

import com.lexsecura.application.dto.CvdPolicyRequest;
import com.lexsecura.application.dto.CvdPolicyResponse;
import com.lexsecura.application.service.CvdPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Coordinated Vulnerability Disclosure (CVD) policies.
 * CRA Annexe I §2(5) + Art. 13(6).
 */
@RestController
@RequestMapping("/api/v1/products/{productId}/cvd-policy")
@Tag(name = "CVD Policy", description = "Coordinated Vulnerability Disclosure policy management (CRA Annexe I §2(5))")
public class CvdPolicyController {

    private final CvdPolicyService cvdPolicyService;

    public CvdPolicyController(CvdPolicyService cvdPolicyService) {
        this.cvdPolicyService = cvdPolicyService;
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Create or update CVD policy for a product")
    public ResponseEntity<CvdPolicyResponse> createOrUpdate(
            @PathVariable UUID productId,
            @Valid @RequestBody CvdPolicyRequest request) {
        return ResponseEntity.ok(cvdPolicyService.createOrUpdate(productId, request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get CVD policy for a product")
    public ResponseEntity<CvdPolicyResponse> get(@PathVariable UUID productId) {
        return ResponseEntity.ok(cvdPolicyService.findByProductId(productId));
    }

    @PostMapping("/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Publish CVD policy (DRAFT -> PUBLISHED)")
    public ResponseEntity<CvdPolicyResponse> publish(@PathVariable UUID productId) {
        return ResponseEntity.ok(cvdPolicyService.publish(productId));
    }
}
