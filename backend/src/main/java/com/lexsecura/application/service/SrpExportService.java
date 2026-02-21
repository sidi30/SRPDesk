package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lexsecura.application.dto.AuditVerifyResponse;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.model.SrpSubmission;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SrpExportService {

    private static final Logger log = LoggerFactory.getLogger(SrpExportService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.of("Europe/Paris"));

    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final CraEventService craEventService;

    public SrpExportService(ObjectMapper objectMapper,
                            AuditService auditService,
                            CraEventService craEventService) {
        this.objectMapper = objectMapper.copy()
                .enable(SerializationFeature.INDENT_OUTPUT);
        this.auditService = auditService;
        this.craEventService = craEventService;
    }

    /**
     * Stream a ZIP bundle to the given OutputStream.
     * Content: submission.json, audit/chain_summary.json, human_readable.pdf
     */
    public void exportBundle(SrpSubmission submission, CraEvent event, OutputStream out) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(out)) {

            // 1) submission.json
            zos.putNextEntry(new ZipEntry("submission.json"));
            Map<String, Object> submissionData = Map.of(
                    "id", submission.getId().toString(),
                    "craEventId", submission.getCraEventId().toString(),
                    "submissionType", submission.getSubmissionType(),
                    "status", submission.getStatus(),
                    "schemaVersion", submission.getSchemaVersion(),
                    "generatedAt", submission.getGeneratedAt().toString(),
                    "generatedBy", submission.getGeneratedBy().toString(),
                    "content", objectMapper.readTree(submission.getContentJson())
            );
            zos.write(objectMapper.writeValueAsBytes(submissionData));
            zos.closeEntry();

            // 2) audit/chain_summary.json
            zos.putNextEntry(new ZipEntry("audit/chain_summary.json"));
            AuditVerifyResponse auditResult = auditService.verify(event.getOrgId());
            Map<String, Object> auditData = Map.of(
                    "valid", auditResult.valid(),
                    "totalEvents", auditResult.totalEvents(),
                    "verifiedEvents", auditResult.verifiedEvents(),
                    "message", auditResult.message(),
                    "exportedAt", Instant.now().toString()
            );
            zos.write(objectMapper.writeValueAsBytes(auditData));
            zos.closeEntry();

            // 3) human_readable.pdf
            zos.putNextEntry(new ZipEntry("human_readable.pdf"));
            byte[] pdf = generatePdf(submission, event);
            zos.write(pdf);
            zos.closeEntry();
        }
    }

    private byte[] generatePdf(SrpSubmission submission, CraEvent event) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float width = page.getMediaBox().getWidth() - 2 * margin;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = yStart;

                // Title
                y = drawText(cs, fontBold, 16, margin, y,
                        "CRA " + formatType(submission.getSubmissionType()));
                y -= 10;

                // Event info
                y = drawText(cs, fontBold, 11, margin, y, "Event: " + event.getTitle());
                y = drawText(cs, fontNormal, 10, margin, y,
                        "Type: " + event.getEventType().replace('_', ' '));
                y = drawText(cs, fontNormal, 10, margin, y,
                        "Status: " + event.getStatus());
                y = drawText(cs, fontNormal, 10, margin, y,
                        "Detected: " + FMT.format(event.getDetectedAt()));
                if (event.getPatchAvailableAt() != null) {
                    y = drawText(cs, fontNormal, 10, margin, y,
                            "Patch available: " + FMT.format(event.getPatchAvailableAt()));
                }
                if (event.getResolvedAt() != null) {
                    y = drawText(cs, fontNormal, 10, margin, y,
                            "Resolved: " + FMT.format(event.getResolvedAt()));
                }
                y -= 15;

                // Submission meta
                y = drawText(cs, fontBold, 11, margin, y, "Submission Details");
                y = drawText(cs, fontNormal, 10, margin, y,
                        "ID: " + submission.getId());
                y = drawText(cs, fontNormal, 10, margin, y,
                        "Schema version: " + submission.getSchemaVersion());
                y = drawText(cs, fontNormal, 10, margin, y,
                        "Generated: " + FMT.format(submission.getGeneratedAt()));
                if (submission.getSubmittedAt() != null) {
                    y = drawText(cs, fontNormal, 10, margin, y,
                            "Submitted: " + FMT.format(submission.getSubmittedAt()));
                }
                if (submission.getSubmittedReference() != null) {
                    y = drawText(cs, fontNormal, 10, margin, y,
                            "Reference: " + submission.getSubmittedReference());
                }
                y -= 15;

                // Content summary (truncated for PDF)
                y = drawText(cs, fontBold, 11, margin, y, "Content Summary");
                String contentStr = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(objectMapper.readTree(submission.getContentJson()));
                String[] lines = contentStr.split("\n");
                for (int i = 0; i < Math.min(lines.length, 40) && y > margin + 30; i++) {
                    String line = lines[i];
                    if (line.length() > 90) line = line.substring(0, 87) + "...";
                    y = drawText(cs, fontNormal, 8, margin, y, line);
                }
                if (lines.length > 40) {
                    y = drawText(cs, fontNormal, 8, margin, y, "... (truncated, see submission.json)");
                }

                y -= 20;
                // Footer
                drawText(cs, fontNormal, 8, margin, Math.max(y, margin),
                        "Generated by LexSecura - " + FMT.format(Instant.now()));
            }

            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private float drawText(PDPageContentStream cs, PDType1Font font, float size,
                           float x, float y, String text) throws IOException {
        // Strip control characters that are invalid in WinAnsiEncoding
        String clean = text.replaceAll("[\\r\\t]", " ")
                          .replaceAll("[^\\x20-\\x7E\\xA0-\\xFF]", "");
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(clean);
        cs.endText();
        return y - size - 4;
    }

    private String formatType(String type) {
        return switch (type) {
            case "EARLY_WARNING" -> "Early Warning Report";
            case "NOTIFICATION" -> "Notification Report";
            case "FINAL_REPORT" -> "Final Report";
            default -> type;
        };
    }
}
