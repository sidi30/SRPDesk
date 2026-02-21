package com.lexsecura.api;

import com.lexsecura.application.dto.*;
import com.lexsecura.application.service.*;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.model.SrpSubmission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cra-events")
@Tag(name = "CRA Events", description = "CRA War Room - Security event management and SRP submissions")
public class CraEventController {

    private final CraEventService craEventService;
    private final SrpSubmissionService submissionService;
    private final SlaService slaService;
    private final SrpExportService exportService;

    public CraEventController(CraEventService craEventService,
                              SrpSubmissionService submissionService,
                              SlaService slaService,
                              SrpExportService exportService) {
        this.craEventService = craEventService;
        this.submissionService = submissionService;
        this.slaService = slaService;
        this.exportService = exportService;
    }

    // ── CRA Events CRUD ─────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Create a new CRA event")
    public ResponseEntity<CraEventResponse> create(@Valid @RequestBody CraEventCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(craEventService.create(request));
    }

    @GetMapping
    @Operation(summary = "List CRA events (filterable by productId, status)")
    public ResponseEntity<List<CraEventResponse>> list(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(craEventService.findAll(productId, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get CRA event details")
    public ResponseEntity<CraEventResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(craEventService.findById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Update a CRA event")
    public ResponseEntity<CraEventResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody CraEventUpdateRequest request) {
        return ResponseEntity.ok(craEventService.update(id, request));
    }

    @PostMapping("/{id}/links")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Add links (releases, findings, evidences) to a CRA event")
    public ResponseEntity<Void> addLinks(@PathVariable UUID id,
                                         @RequestBody CraEventLinkRequest request) {
        craEventService.addLinks(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/participants")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Add or update a participant on a CRA event")
    public ResponseEntity<CraEventParticipantResponse> addParticipant(
            @PathVariable UUID id,
            @Valid @RequestBody CraEventParticipantRequest request) {
        return ResponseEntity.ok(craEventService.addParticipant(id, request));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Close a CRA event")
    public ResponseEntity<CraEventResponse> close(@PathVariable UUID id) {
        return ResponseEntity.ok(craEventService.close(id));
    }

    // ── SLA ─────────────────────────────────────────────────

    @GetMapping("/{id}/sla")
    @Operation(summary = "Get SLA deadlines and remaining time for a CRA event")
    public ResponseEntity<SlaResponse> getSla(@PathVariable UUID id) {
        CraEvent event = craEventService.getEvent(id);
        return ResponseEntity.ok(slaService.computeSla(event));
    }

    // ── SRP Submissions ─────────────────────────────────────

    @PostMapping("/{id}/submissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Create a new SRP submission with autofill")
    public ResponseEntity<SrpSubmissionResponse> createSubmission(
            @PathVariable UUID id,
            @Valid @RequestBody SrpSubmissionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(submissionService.create(id, request));
    }

    @GetMapping("/{id}/submissions")
    @Operation(summary = "List all submissions for a CRA event")
    public ResponseEntity<List<SrpSubmissionResponse>> listSubmissions(@PathVariable UUID id) {
        return ResponseEntity.ok(submissionService.findAll(id));
    }

    @GetMapping("/{id}/submissions/{subId}")
    @Operation(summary = "Get a specific submission")
    public ResponseEntity<SrpSubmissionResponse> getSubmission(
            @PathVariable UUID id, @PathVariable UUID subId) {
        return ResponseEntity.ok(submissionService.findById(id, subId));
    }

    @PostMapping("/{id}/submissions/{subId}/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Validate a submission against its schema")
    public ResponseEntity<SrpSubmissionResponse> validateSubmission(
            @PathVariable UUID id, @PathVariable UUID subId) {
        return ResponseEntity.ok(submissionService.validate(id, subId));
    }

    @PostMapping("/{id}/submissions/{subId}/mark-ready")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Mark a submission as ready (fails if validation errors)")
    public ResponseEntity<SrpSubmissionResponse> markReady(
            @PathVariable UUID id, @PathVariable UUID subId) {
        return ResponseEntity.ok(submissionService.markReady(id, subId));
    }

    @GetMapping("/{id}/submissions/{subId}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Export submission bundle as ZIP")
    public void exportBundle(@PathVariable UUID id, @PathVariable UUID subId,
                             HttpServletResponse response) throws IOException {
        CraEvent event = craEventService.getEvent(id);
        SrpSubmission sub = submissionService.getSubmission(id, subId);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"srp_" + sub.getSubmissionType().toLowerCase() + "_" + subId + ".zip\"");

        exportService.exportBundle(sub, event, response.getOutputStream());

        submissionService.markExported(sub);
    }

    @PostMapping("/{id}/submissions/{subId}/mark-submitted")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    @Operation(summary = "Mark a submission as submitted with external reference")
    public ResponseEntity<SrpSubmissionResponse> markSubmitted(
            @PathVariable UUID id, @PathVariable UUID subId,
            @Valid @RequestBody MarkSubmittedRequest request,
            @RequestParam(required = false) UUID ackEvidenceId) {
        return ResponseEntity.ok(submissionService.markSubmitted(id, subId, request, ackEvidenceId));
    }
}
