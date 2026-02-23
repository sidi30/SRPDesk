package com.lexsecura.api.controller;

import com.lexsecura.application.dto.CraChecklistItemResponse;
import com.lexsecura.application.dto.CraChecklistSummaryResponse;
import com.lexsecura.application.dto.CraChecklistUpdateRequest;
import com.lexsecura.application.service.CraChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/cra-checklist")
@Tag(name = "CRA Checklist", description = "CRA Annex I compliance checklist management")
public class CraChecklistController {

    private final CraChecklistService checklistService;

    public CraChecklistController(CraChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Initialize the 21 Annex I checklist items for a product")
    public ResponseEntity<List<CraChecklistItemResponse>> initialize(@PathVariable UUID productId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checklistService.initializeChecklist(productId));
    }

    @GetMapping
    @Operation(summary = "List all checklist items for a product")
    public ResponseEntity<List<CraChecklistItemResponse>> list(@PathVariable UUID productId) {
        return ResponseEntity.ok(checklistService.findAll(productId));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get checklist compliance summary for a product")
    public ResponseEntity<CraChecklistSummaryResponse> summary(@PathVariable UUID productId) {
        return ResponseEntity.ok(checklistService.getSummary(productId));
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Update a checklist item status, notes or evidence")
    public ResponseEntity<CraChecklistItemResponse> update(
            @PathVariable UUID productId,
            @PathVariable UUID itemId,
            @RequestBody CraChecklistUpdateRequest request) {
        return ResponseEntity.ok(checklistService.update(itemId, request));
    }

    @PostMapping("/{itemId}/evidences/{evidenceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Link an evidence to a checklist item")
    public ResponseEntity<CraChecklistItemResponse> linkEvidence(
            @PathVariable UUID productId,
            @PathVariable UUID itemId,
            @PathVariable UUID evidenceId) {
        return ResponseEntity.ok(checklistService.linkEvidence(itemId, evidenceId));
    }
}
