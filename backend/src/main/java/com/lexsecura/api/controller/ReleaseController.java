package com.lexsecura.api.controller;

import com.lexsecura.application.dto.ReleaseCreateRequest;
import com.lexsecura.application.dto.ReleaseResponse;
import com.lexsecura.application.service.ReleaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Releases", description = "Product release management")
public class ReleaseController {

    private final ReleaseService releaseService;

    public ReleaseController(ReleaseService releaseService) {
        this.releaseService = releaseService;
    }

    @PostMapping("/api/v1/products/{productId}/releases")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Create a release for a product")
    public ResponseEntity<ReleaseResponse> create(@PathVariable UUID productId,
                                                   @Valid @RequestBody ReleaseCreateRequest request) {
        ReleaseResponse response = releaseService.create(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/products/{productId}/releases")
    @Operation(summary = "List releases for a product")
    public ResponseEntity<List<ReleaseResponse>> listByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(releaseService.findAllByProductId(productId));
    }

    @GetMapping("/api/v1/releases/{id}")
    @Operation(summary = "Get a release by ID")
    public ResponseEntity<ReleaseResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(releaseService.findById(id));
    }
}
