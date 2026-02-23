package com.lexsecura.api.controller;

import com.lexsecura.application.dto.VexDocumentResponse;
import com.lexsecura.application.dto.VexGenerateRequest;
import com.lexsecura.application.service.VexService;
import com.lexsecura.domain.model.vex.VexFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "VEX", description = "VEX (Vulnerability Exploitability eXchange) lifecycle management")
public class VexController {

    private final VexService vexService;

    public VexController(VexService vexService) {
        this.vexService = vexService;
    }

    @PostMapping("/api/v1/releases/{releaseId}/vex")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Generate a VEX document for a release")
    public ResponseEntity<VexDocumentResponse> generate(
            @PathVariable UUID releaseId,
            @Valid @RequestBody VexGenerateRequest request) {
        VexFormat format = VexFormat.valueOf(request.format());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vexService.generateVexDocument(releaseId, format));
    }

    @GetMapping("/api/v1/releases/{releaseId}/vex")
    @Operation(summary = "List VEX documents for a release")
    public ResponseEntity<List<VexDocumentResponse>> listByRelease(@PathVariable UUID releaseId) {
        return ResponseEntity.ok(vexService.listByRelease(releaseId));
    }

    @GetMapping("/api/v1/vex/{id}")
    @Operation(summary = "Get VEX document details")
    public ResponseEntity<VexDocumentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(vexService.getById(id));
    }

    @PostMapping("/api/v1/vex/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Publish a VEX document (DRAFT -> PUBLISHED)")
    public ResponseEntity<VexDocumentResponse> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(vexService.publish(id));
    }

    @GetMapping("/api/v1/vex/{id}/download")
    @Operation(summary = "Download VEX document as JSON")
    public ResponseEntity<String> download(@PathVariable UUID id) {
        String json = vexService.downloadDocument(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"vex-" + id + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    @DeleteMapping("/api/v1/vex/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Delete a DRAFT VEX document")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        vexService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
