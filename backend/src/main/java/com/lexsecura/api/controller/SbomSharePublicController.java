package com.lexsecura.api.controller;

import com.lexsecura.application.port.StoragePort;
import com.lexsecura.application.service.SbomShareService;
import com.lexsecura.domain.model.Evidence;
import com.lexsecura.domain.model.SbomShareLink;
import com.lexsecura.domain.repository.EvidenceRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

/**
 * Public SBOM download endpoint (no authentication required).
 * Access controlled via secure token validation.
 */
@RestController
@RequestMapping("/share/sbom")
public class SbomSharePublicController {

    private final SbomShareService sbomShareService;
    private final EvidenceRepository evidenceRepository;
    private final StoragePort storagePort;

    public SbomSharePublicController(SbomShareService sbomShareService,
                                     EvidenceRepository evidenceRepository,
                                     StoragePort storagePort) {
        this.sbomShareService = sbomShareService;
        this.evidenceRepository = evidenceRepository;
        this.storagePort = storagePort;
    }

    @GetMapping("/{token}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String token) {
        SbomShareLink link = sbomShareService.validateAndConsume(token);

        // Load evidence (no org filter needed — token is the auth)
        Evidence evidence = evidenceRepository.findAllByReleaseIdAndOrgId(link.getReleaseId(), link.getOrgId())
                .stream()
                .filter(e -> e.getId().equals(link.getEvidenceId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("SBOM evidence not found"));

        InputStream is = storagePort.download(evidence.getStorageUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + evidence.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new InputStreamResource(is));
    }
}
