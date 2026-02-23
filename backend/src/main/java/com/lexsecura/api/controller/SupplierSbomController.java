package com.lexsecura.api.controller;

import com.lexsecura.application.dto.SupplierSbomResponse;
import com.lexsecura.application.service.SupplierSbomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/releases/{releaseId}/supplier-sboms")
public class SupplierSbomController {

    private final SupplierSbomService supplierSbomService;

    public SupplierSbomController(SupplierSbomService supplierSbomService) {
        this.supplierSbomService = supplierSbomService;
    }

    @PostMapping
    public ResponseEntity<SupplierSbomResponse> importSupplierSbom(
            @PathVariable UUID releaseId,
            @RequestParam String supplierName,
            @RequestParam(required = false) String supplierUrl,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierSbomService.importSupplierSbom(releaseId, supplierName, supplierUrl, file));
    }

    @GetMapping
    public ResponseEntity<List<SupplierSbomResponse>> list(@PathVariable UUID releaseId) {
        return ResponseEntity.ok(supplierSbomService.listByRelease(releaseId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID releaseId, @PathVariable UUID id) {
        supplierSbomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
