package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lexsecura.application.dto.AuditVerifyResponse;
import com.lexsecura.domain.model.CraEvent;
import com.lexsecura.domain.model.SrpSubmission;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SrpExportService {

    private static final Logger log = LoggerFactory.getLogger(SrpExportService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Europe/Paris"));

    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final CraEventService craEventService;
    private final String srpTemplate;

    public SrpExportService(ObjectMapper objectMapper,
                            AuditService auditService,
                            CraEventService craEventService) {
        this.objectMapper = objectMapper.copy()
                .enable(SerializationFeature.INDENT_OUTPUT);
        this.auditService = auditService;
        this.craEventService = craEventService;
        this.srpTemplate = loadTemplate("templates/srp-report.html");
    }

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
        String contentJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(submission.getContentJson()));
        // Truncate for PDF readability
        String[] lines = contentJson.split("\n");
        if (lines.length > 80) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 80; i++) {
                sb.append(esc(lines[i])).append("\n");
            }
            sb.append("... (tronqu\u00e9, voir submission.json)");
            contentJson = sb.toString();
        } else {
            contentJson = esc(contentJson);
        }

        String patchRow = event.getPatchAvailableAt() != null
                ? "<div class=\"row\"><span class=\"lbl\">Correctif disponible :</span><span class=\"val\">" + FMT.format(event.getPatchAvailableAt()) + "</span></div>"
                : "";
        String resolvedRow = event.getResolvedAt() != null
                ? "<div class=\"row\"><span class=\"lbl\">R\u00e9solu le :</span><span class=\"val\">" + FMT.format(event.getResolvedAt()) + "</span></div>"
                : "";
        String submittedAtRow = submission.getSubmittedAt() != null
                ? "<div class=\"row\"><span class=\"lbl\">Soumis le :</span><span class=\"val\">" + FMT.format(submission.getSubmittedAt()) + "</span></div>"
                : "";
        String referenceRow = submission.getSubmittedReference() != null
                ? "<div class=\"row\"><span class=\"lbl\">R\u00e9f\u00e9rence :</span><span class=\"val\">" + esc(submission.getSubmittedReference()) + "</span></div>"
                : "";

        String html = srpTemplate
                .replace("{{reportTypeLabel}}", formatType(submission.getSubmissionType()))
                .replace("{{generatedAt}}", FMT.format(Instant.now()))
                .replace("{{eventTitle}}", esc(event.getTitle()))
                .replace("{{eventType}}", esc(event.getEventType().replace('_', ' ')))
                .replace("{{eventStatus}}", esc(event.getStatus()))
                .replace("{{detectedAt}}", FMT.format(event.getDetectedAt()))
                .replace("{{patchRow}}", patchRow)
                .replace("{{resolvedRow}}", resolvedRow)
                .replace("{{submissionId}}", submission.getId().toString())
                .replace("{{schemaVersion}}", esc(submission.getSchemaVersion()))
                .replace("{{submissionGeneratedAt}}", FMT.format(submission.getGeneratedAt()))
                .replace("{{submittedAtRow}}", submittedAtRow)
                .replace("{{referenceRow}}", referenceRow)
                .replace("{{contentJson}}", contentJson);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to render SRP PDF", e);
            throw new IOException("SRP PDF generation failed", e);
        }
    }

    private String formatType(String type) {
        return switch (type) {
            case "EARLY_WARNING" -> "Alerte Pr\u00e9coce";
            case "NOTIFICATION" -> "Notification";
            case "FINAL_REPORT" -> "Rapport Final";
            default -> type;
        };
    }

    private static String esc(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String loadTemplate(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Template not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load template: " + path, e);
        }
    }
}
