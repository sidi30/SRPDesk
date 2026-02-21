package com.lexsecura.api.controller;

import com.lexsecura.application.dto.AuditEventResponse;
import com.lexsecura.application.dto.AuditVerifyResponse;
import com.lexsecura.application.service.AuditService;
import com.lexsecura.infrastructure.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit", description = "Audit trail and hash chain verification")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Verify audit hash chain integrity for the organization")
    public ResponseEntity<AuditVerifyResponse> verify() {
        UUID orgId = TenantContext.getOrgId();
        return ResponseEntity.ok(auditService.verify(orgId));
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Query audit events")
    public ResponseEntity<List<AuditEventResponse>> getEvents(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId) {

        if (entityType != null && entityId != null) {
            return ResponseEntity.ok(auditService.findEvents(entityType, entityId));
        }

        UUID orgId = TenantContext.getOrgId();
        return ResponseEntity.ok(auditService.findAllByOrgId(orgId));
    }
}
