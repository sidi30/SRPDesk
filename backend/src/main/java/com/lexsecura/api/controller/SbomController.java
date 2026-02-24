package com.lexsecura.api.controller;

import com.lexsecura.application.dto.ComponentResponse;
import com.lexsecura.application.dto.SbomUploadResponse;
import com.lexsecura.application.service.SbomExportService;
import com.lexsecura.application.service.SbomService;
import com.lexsecura.domain.model.Component;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/releases/{releaseId}")
@Tag(name = "SBOM", description = "Software Bill of Materials management")
public class SbomController {

    private final SbomService sbomService;
    private final SbomExportService sbomExportService;

    public SbomController(SbomService sbomService, SbomExportService sbomExportService) {
        this.sbomService = sbomService;
        this.sbomExportService = sbomExportService;
    }

    @PostMapping("/sbom")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER', 'CONTRIBUTOR')")
    @Operation(summary = "Upload a CycloneDX SBOM for a release")
    public ResponseEntity<SbomUploadResponse> uploadSbom(
            @PathVariable UUID releaseId,
            @RequestParam("file") MultipartFile file) {
        SbomUploadResponse response = sbomService.ingest(releaseId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/components")
    @Operation(summary = "List components from SBOM for a release")
    public ResponseEntity<List<ComponentResponse>> listComponents(@PathVariable UUID releaseId) {
        List<ComponentResponse> components = sbomService.getComponentsByRelease(releaseId).stream()
                .map(c -> new ComponentResponse(c.getId(), c.getPurl(), c.getName(), c.getVersion(), c.getType()))
                .toList();
        return ResponseEntity.ok(components);
    }

    @GetMapping("/sbom/export")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Export SBOM in CycloneDX or SPDX 2.3 format")
    public ResponseEntity<byte[]> exportSbom(
            @PathVariable UUID releaseId,
            @RequestParam(defaultValue = "cyclonedx") String format) {
        String json = sbomExportService.export(releaseId, format);
        String filename = "sbom_" + releaseId.toString().substring(0, 8) + "_" + format + ".json";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json.getBytes(StandardCharsets.UTF_8));
    }
}
