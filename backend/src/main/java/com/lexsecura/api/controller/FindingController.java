package com.lexsecura.api.controller;

import com.lexsecura.application.dto.FindingDecisionRequest;
import com.lexsecura.application.dto.FindingDecisionResponse;
import com.lexsecura.application.dto.FindingResponse;
import com.lexsecura.application.service.FindingService;
import com.lexsecura.application.service.VulnerabilityScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Tag(name = "Findings", description = "Vulnerability findings and decisions")
public class FindingController {

    private final FindingService findingService;
    private final VulnerabilityScanService scanService;

    public FindingController(FindingService findingService, VulnerabilityScanService scanService) {
        this.findingService = findingService;
        this.scanService = scanService;
    }

    @GetMapping("/api/v1/products/{productId}/findings")
    @Operation(summary = "List findings for a product")
    public ResponseEntity<List<FindingResponse>> findByProduct(
            @PathVariable UUID productId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(findingService.findByProductId(productId, status));
    }

    @GetMapping("/api/v1/releases/{releaseId}/findings")
    @Operation(summary = "List findings for a release")
    public ResponseEntity<List<FindingResponse>> findByRelease(
            @PathVariable UUID releaseId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(findingService.findByReleaseId(releaseId, status));
    }

    @PostMapping("/api/v1/findings/{findingId}/decisions")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Add a decision to a finding")
    public ResponseEntity<FindingDecisionResponse> addDecision(
            @PathVariable UUID findingId,
            @Valid @RequestBody FindingDecisionRequest request) {
        return ResponseEntity.ok(findingService.addDecision(findingId, request));
    }

    @PostMapping("/api/v1/releases/{releaseId}/scan")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Trigger vulnerability scan for a release")
    public ResponseEntity<Map<String, Object>> scanRelease(@PathVariable UUID releaseId) {
        int newFindings = scanService.scanRelease(releaseId);
        return ResponseEntity.ok(Map.of("releaseId", releaseId, "newFindings", newFindings));
    }
}
