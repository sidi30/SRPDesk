package com.lexsecura.api.controller;

import com.lexsecura.application.dto.SbomQualityScoreResponse;
import com.lexsecura.application.service.SbomQualityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/releases/{releaseId}/sbom-quality")
public class SbomQualityController {

    private final SbomQualityService sbomQualityService;

    public SbomQualityController(SbomQualityService sbomQualityService) {
        this.sbomQualityService = sbomQualityService;
    }

    @GetMapping
    public ResponseEntity<SbomQualityScoreResponse> getQualityScore(@PathVariable UUID releaseId) {
        return ResponseEntity.ok(sbomQualityService.scoreRelease(releaseId));
    }
}
