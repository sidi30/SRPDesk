package com.lexsecura.api.controller;

import com.lexsecura.application.dto.ComponentResponse;
import com.lexsecura.application.dto.SbomUploadResponse;
import com.lexsecura.application.service.SbomService;
import com.lexsecura.domain.model.Component;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Tag(name = "SBOM", description = "Software Bill of Materials management")
public class SbomController {

    private final SbomService sbomService;

    public SbomController(SbomService sbomService) {
        this.sbomService = sbomService;
    }

    @PostMapping("/api/v1/releases/{releaseId}/sbom")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER', 'CONTRIBUTOR')")
    @Operation(summary = "Upload a CycloneDX SBOM for a release")
    public ResponseEntity<SbomUploadResponse> uploadSbom(
            @PathVariable UUID releaseId,
            @RequestParam("file") MultipartFile file) {
        SbomUploadResponse response = sbomService.ingest(releaseId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/releases/{releaseId}/components")
    @Operation(summary = "List components from SBOM for a release")
    public ResponseEntity<List<ComponentResponse>> listComponents(@PathVariable UUID releaseId) {
        List<ComponentResponse> components = sbomService.getComponentsByRelease(releaseId).stream()
                .map(c -> new ComponentResponse(c.getId(), c.getPurl(), c.getName(), c.getVersion(), c.getType()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(components);
    }
}
