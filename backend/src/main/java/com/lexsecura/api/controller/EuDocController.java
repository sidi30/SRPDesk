package com.lexsecura.api.controller;

import com.lexsecura.application.dto.EuDocRequest;
import com.lexsecura.application.dto.EuDocResponse;
import com.lexsecura.application.service.EuDocService;
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
 * REST controller for EU Declaration of Conformity (Annex V).
 * CRA Art. 28: Manufacturers must draw up an EU declaration of conformity.
 */
@RestController
@RequestMapping("/api/v1/products/{productId}/eu-doc")
@Tag(name = "EU Declaration of Conformity", description = "EU Declaration of Conformity management (CRA Annex V)")
public class EuDocController {

    private final EuDocService euDocService;

    public EuDocController(EuDocService euDocService) {
        this.euDocService = euDocService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Create a new EU Declaration of Conformity")
    public ResponseEntity<EuDocResponse> create(
            @PathVariable UUID productId,
            @Valid @RequestBody EuDocRequest request) {
        EuDocResponse response = euDocService.create(productId, request);
        return ResponseEntity.created(URI.create("/api/v1/products/" + productId + "/eu-doc/" + response.id()))
                .body(response);
    }

    @PutMapping("/{docId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Update an EU Declaration of Conformity (DRAFT only)")
    public ResponseEntity<EuDocResponse> update(
            @PathVariable UUID productId,
            @PathVariable UUID docId,
            @Valid @RequestBody EuDocRequest request) {
        return ResponseEntity.ok(euDocService.update(docId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "List EU Declarations of Conformity for a product")
    public ResponseEntity<List<EuDocResponse>> list(@PathVariable UUID productId) {
        return ResponseEntity.ok(euDocService.findByProductId(productId));
    }

    @GetMapping("/{docId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Get an EU Declaration of Conformity by ID")
    public ResponseEntity<EuDocResponse> getById(
            @PathVariable UUID productId,
            @PathVariable UUID docId) {
        return ResponseEntity.ok(euDocService.findById(docId));
    }

    @PostMapping("/{docId}/sign")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Sign an EU Declaration of Conformity")
    public ResponseEntity<EuDocResponse> sign(
            @PathVariable UUID productId,
            @PathVariable UUID docId) {
        return ResponseEntity.ok(euDocService.sign(docId));
    }

    @PostMapping("/{docId}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Publish an EU Declaration of Conformity")
    public ResponseEntity<EuDocResponse> publish(
            @PathVariable UUID productId,
            @PathVariable UUID docId) {
        return ResponseEntity.ok(euDocService.publish(docId));
    }
}
