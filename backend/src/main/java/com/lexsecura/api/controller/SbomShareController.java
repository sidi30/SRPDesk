package com.lexsecura.api.controller;

import com.lexsecura.application.dto.ShareLinkCreateRequest;
import com.lexsecura.application.dto.ShareLinkResponse;
import com.lexsecura.application.service.SbomShareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Manage SBOM share links (requires JWT authentication).
 */
@RestController
@RequestMapping("/api/v1/releases/{releaseId}/share-links")
public class SbomShareController {

    private final SbomShareService sbomShareService;

    public SbomShareController(SbomShareService sbomShareService) {
        this.sbomShareService = sbomShareService;
    }

    @PostMapping
    public ResponseEntity<ShareLinkResponse> create(
            @PathVariable UUID releaseId,
            @RequestBody ShareLinkCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sbomShareService.createShareLink(releaseId, request));
    }

    @GetMapping
    public ResponseEntity<List<ShareLinkResponse>> list(@PathVariable UUID releaseId) {
        return ResponseEntity.ok(sbomShareService.listByRelease(releaseId));
    }

    @DeleteMapping("/{linkId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID releaseId, @PathVariable UUID linkId) {
        sbomShareService.revoke(linkId);
        return ResponseEntity.noContent().build();
    }
}
