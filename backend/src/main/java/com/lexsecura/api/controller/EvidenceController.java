package com.lexsecura.api.controller;

import com.lexsecura.application.dto.EvidenceResponse;
import com.lexsecura.application.service.EvidenceService;
import com.lexsecura.domain.model.Evidence;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Evidences", description = "Evidence file management")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @PostMapping("/api/v1/releases/{releaseId}/evidences")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER', 'CONTRIBUTOR')")
    @Operation(summary = "Upload an evidence file for a release")
    public ResponseEntity<EvidenceResponse> upload(
            @PathVariable UUID releaseId,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) {
        EvidenceResponse response = evidenceService.upload(releaseId, type, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/releases/{releaseId}/evidences")
    @Operation(summary = "List evidences for a release")
    public ResponseEntity<List<EvidenceResponse>> listByRelease(@PathVariable UUID releaseId) {
        return ResponseEntity.ok(evidenceService.findAllByReleaseId(releaseId));
    }

    @GetMapping("/api/v1/evidences/{id}/download")
    @Operation(summary = "Download evidence file")
    public ResponseEntity<InputStreamResource> download(@PathVariable UUID id) {
        Evidence evidence = evidenceService.getEvidence(id);
        InputStream inputStream = evidenceService.download(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(evidence.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + evidence.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(evidence.getSize()))
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/api/v1/evidences/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Delete an evidence")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        evidenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
