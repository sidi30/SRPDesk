package com.lexsecura.api.controller;

import com.lexsecura.application.service.CompliancePackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@Tag(name = "Compliance Pack", description = "Export compliance pack for a release")
public class CompliancePackController {

    private final CompliancePackService compliancePackService;

    public CompliancePackController(CompliancePackService compliancePackService) {
        this.compliancePackService = compliancePackService;
    }

    @GetMapping("/api/v1/releases/{releaseId}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Export compliance pack (ZIP) for a release")
    public void exportPack(@PathVariable UUID releaseId, HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"compliance-pack-" + releaseId + ".zip\"");

        compliancePackService.generatePack(releaseId, response.getOutputStream());
        response.flushBuffer();
    }
}
