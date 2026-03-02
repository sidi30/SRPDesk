package com.lexsecura.api.controller;

import com.lexsecura.application.dto.CiSbomEnrichedResponse;
import com.lexsecura.application.dto.CiScanResponse;
import com.lexsecura.application.service.CiSbomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/integrations/ci")
public class CiSbomController {

    private final CiSbomService ciSbomService;

    public CiSbomController(CiSbomService ciSbomService) {
        this.ciSbomService = ciSbomService;
    }

    @PostMapping("/sbom")
    public ResponseEntity<CiSbomEnrichedResponse> uploadSbom(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productName") String productName,
            @RequestParam("version") String version,
            @RequestParam(value = "gitRef", required = false) String gitRef) {

        CiSbomEnrichedResponse response = ciSbomService.uploadFromCi(productName, version, gitRef, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/scan")
    public ResponseEntity<CiScanResponse> scanSbom(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productName") String productName) {

        CiScanResponse response = ciSbomService.scanFromCi(productName, file);
        return ResponseEntity.ok(response);
    }
}
