package com.lexsecura.api.controller;

import com.lexsecura.application.dto.VulnerabilityReportResponse;
import com.lexsecura.application.dto.VulnerabilityReportTriageRequest;
import com.lexsecura.application.service.VulnerabilityReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Internal CVD management endpoints — requires authentication.
 * GET    /api/v1/cvd/reports           — list all reports
 * GET    /api/v1/cvd/reports/{id}      — get report details
 * PUT    /api/v1/cvd/reports/{id}/triage — workflow transition
 * GET    /api/v1/cvd/reports/count/new  — count NEW reports (for badge)
 */
@RestController
@RequestMapping("/api/v1/cvd/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
public class CvdInternalController {

    private final VulnerabilityReportService reportService;

    public CvdInternalController(VulnerabilityReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<VulnerabilityReportResponse>> list(
            @RequestParam(required = false) String status) {
        List<VulnerabilityReportResponse> reports;
        if (status != null && !status.isBlank()) {
            reports = reportService.listByStatus(status);
        } else {
            reports = reportService.listAll();
        }
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VulnerabilityReportResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reportService.getById(id));
    }

    @PutMapping("/{id}/triage")
    public ResponseEntity<VulnerabilityReportResponse> triage(
            @PathVariable UUID id,
            @Valid @RequestBody VulnerabilityReportTriageRequest request) {
        return ResponseEntity.ok(reportService.triage(id, request));
    }

    @GetMapping("/count/new")
    public ResponseEntity<Map<String, Long>> countNew() {
        return ResponseEntity.ok(Map.of("count", reportService.countNew()));
    }
}
