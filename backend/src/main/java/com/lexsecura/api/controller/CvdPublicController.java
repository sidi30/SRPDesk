package com.lexsecura.api.controller;

import com.lexsecura.application.dto.VulnerabilityReportStatusResponse;
import com.lexsecura.application.dto.VulnerabilityReportSubmitRequest;
import com.lexsecura.application.service.VulnerabilityReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Public CVD endpoints — no authentication required.
 * POST  /api/v1/cvd/reports          — submit a vulnerability report
 * GET   /api/v1/cvd/reports/{trackingId}/status — check status
 * GET   /.well-known/security.txt     — RFC 9116 security.txt
 */
@RestController
public class CvdPublicController {

    private final VulnerabilityReportService reportService;

    @Value("${app.cvd.default-org-id:}")
    private String defaultOrgId;

    @Value("${app.cvd.contact-email:security@srpdesk.com}")
    private String contactEmail;

    public CvdPublicController(VulnerabilityReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/api/v1/cvd/reports")
    public ResponseEntity<VulnerabilityReportStatusResponse> submit(
            @Valid @RequestBody VulnerabilityReportSubmitRequest request,
            @RequestHeader(value = "X-Org-Id", required = false) String orgIdHeader) {

        UUID orgId = resolveOrgId(orgIdHeader);
        VulnerabilityReportStatusResponse response = reportService.submit(orgId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/api/v1/cvd/reports/{trackingId}/status")
    public ResponseEntity<VulnerabilityReportStatusResponse> getStatus(@PathVariable String trackingId) {
        return ResponseEntity.ok(reportService.getStatus(trackingId));
    }

    @GetMapping(value = "/.well-known/security.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> securityTxt() {
        StringBuilder sb = new StringBuilder();
        sb.append("# SRPDesk Security Policy (RFC 9116)\n");
        sb.append("Contact: mailto:").append(contactEmail).append("\n");
        sb.append("Preferred-Languages: en, fr\n");
        sb.append("Canonical: https://app.srpdesk.com/.well-known/security.txt\n");
        sb.append("Policy: https://app.srpdesk.com/security-policy\n");
        sb.append("Hiring: https://srpdesk.com/careers\n");
        sb.append("Expires: 2027-01-01T00:00:00.000Z\n");
        return ResponseEntity.ok(sb.toString());
    }

    private UUID resolveOrgId(String orgIdHeader) {
        if (orgIdHeader != null && !orgIdHeader.isBlank()) {
            return UUID.fromString(orgIdHeader);
        }
        if (defaultOrgId != null && !defaultOrgId.isBlank()) {
            return UUID.fromString(defaultOrgId);
        }
        throw new IllegalArgumentException("X-Org-Id header is required for public CVD submissions");
    }
}
