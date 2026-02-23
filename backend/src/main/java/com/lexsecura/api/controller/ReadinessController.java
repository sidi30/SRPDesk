package com.lexsecura.api.controller;

import com.lexsecura.application.dto.ReadinessScoreResponse;
import com.lexsecura.application.service.ReadinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/readiness")
@Tag(name = "Readiness Score", description = "CRA readiness score computation and snapshots")
public class ReadinessController {

    private final ReadinessService readinessService;

    public ReadinessController(ReadinessService readinessService) {
        this.readinessService = readinessService;
    }

    @GetMapping
    @Operation(summary = "Compute the current readiness score for a product")
    public ResponseEntity<ReadinessScoreResponse> getScore(@PathVariable UUID productId) {
        return ResponseEntity.ok(readinessService.computeScore(productId));
    }

    @PostMapping("/snapshot")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Save a readiness score snapshot for historical tracking")
    public ResponseEntity<ReadinessScoreResponse> snapshot(@PathVariable UUID productId) {
        return ResponseEntity.ok(readinessService.snapshotScore(productId));
    }
}
