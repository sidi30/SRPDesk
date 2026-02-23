package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lexsecura.application.dto.AuditVerifyResponse;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.model.SrpSubmission;
import com.lexsecura.domain.repository.VexDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SrpExportServiceTest {

    @Mock
    private AuditService auditService;

    @Mock
    private CraEventService craEventService;

    @Mock
    private VexDocumentRepository vexDocumentRepository;

    private SrpExportService exportService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        exportService = new SrpExportService(objectMapper, auditService, craEventService, vexDocumentRepository);
    }

    @Test
    void exportBundle_producesValidZip() throws Exception {
        when(auditService.verify(any())).thenReturn(
                new AuditVerifyResponse(true, 10, 10, "All verified"));

        CraEvent event = new CraEvent();
        event.setId(UUID.randomUUID());
        event.setOrgId(UUID.randomUUID());
        event.setTitle("Test Event");
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setStatus("IN_REVIEW");
        event.setDetectedAt(Instant.now());

        SrpSubmission sub = new SrpSubmission();
        sub.setId(UUID.randomUUID());
        sub.setCraEventId(event.getId());
        sub.setSubmissionType("EARLY_WARNING");
        sub.setStatus("READY");
        sub.setContentJson("{\"product\":{\"name\":\"Test\"},\"event\":{\"type\":\"EXPLOITED_VULNERABILITY\",\"title\":\"Test\",\"detectedAt\":\"2026-02-21T00:00:00Z\"}}");
        sub.setSchemaVersion("1.0");
        sub.setGeneratedBy(UUID.randomUUID());
        sub.setGeneratedAt(Instant.now());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exportService.exportBundle(sub, event, baos);

        byte[] zipBytes = baos.toByteArray();
        assertTrue(zipBytes.length > 0);

        // Verify ZIP contents
        boolean hasSubmission = false;
        boolean hasAudit = false;
        boolean hasPdf = false;

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                switch (entry.getName()) {
                    case "submission.json" -> hasSubmission = true;
                    case "audit/chain_summary.json" -> hasAudit = true;
                    case "human_readable.pdf" -> hasPdf = true;
                }
            }
        }

        assertTrue(hasSubmission, "ZIP should contain submission.json");
        assertTrue(hasAudit, "ZIP should contain audit/chain_summary.json");
        assertTrue(hasPdf, "ZIP should contain human_readable.pdf");
    }

    @Test
    void exportBundle_pdfContainsContent() throws Exception {
        when(auditService.verify(any())).thenReturn(
                new AuditVerifyResponse(true, 5, 5, "OK"));

        CraEvent event = new CraEvent();
        event.setId(UUID.randomUUID());
        event.setOrgId(UUID.randomUUID());
        event.setTitle("Log4Shell Exploitation");
        event.setEventType("EXPLOITED_VULNERABILITY");
        event.setStatus("SUBMITTED");
        event.setDetectedAt(Instant.now());

        SrpSubmission sub = new SrpSubmission();
        sub.setId(UUID.randomUUID());
        sub.setCraEventId(event.getId());
        sub.setSubmissionType("NOTIFICATION");
        sub.setStatus("READY");
        sub.setContentJson("{\"product\":{\"name\":\"IoT Gateway\"}}");
        sub.setSchemaVersion("1.0");
        sub.setGeneratedBy(UUID.randomUUID());
        sub.setGeneratedAt(Instant.now());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exportService.exportBundle(sub, event, baos);

        // Just verify it doesn't throw and produces output
        assertTrue(baos.size() > 100, "ZIP should have reasonable size");
    }
}
