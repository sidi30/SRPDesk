package com.lexsecura.application.service;

import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(PdfReportGenerator.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Europe/Paris"));
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.of("Europe/Paris"));

    private final String complianceTemplate;

    public record EnrichedFinding(
            Finding finding,
            String osvId,
            String summary,
            String severity,
            List<FindingDecision> decisions
    ) {}

    public PdfReportGenerator() {
        this.complianceTemplate = loadTemplate("templates/compliance-report.html");
    }

    public byte[] generate(Product product, Release release, List<Evidence> evidences,
                           List<Component> components, List<EnrichedFinding> enrichedFindings,
                           String auditHashHead) throws IOException {

        long openCount = enrichedFindings.stream()
                .filter(ef -> "OPEN".equals(ef.finding().getStatus())).count();
        long criticalHighCount = enrichedFindings.stream()
                .filter(ef -> "CRITICAL".equals(ef.severity()) || "HIGH".equals(ef.severity())).count();

        String html = complianceTemplate
                .replace("{{generatedAt}}", DATE_FMT.format(Instant.now()))
                .replace("{{totalFindings}}", String.valueOf(enrichedFindings.size()))
                .replace("{{openFindings}}", String.valueOf(openCount))
                .replace("{{criticalHighFindings}}", String.valueOf(criticalHighCount))
                .replace("{{productName}}", esc(product.getName()))
                .replace("{{productType}}", esc(product.getType()))
                .replace("{{productCriticality}}", esc(product.getCriticality()))
                .replace("{{releaseVersion}}", esc(release.getVersion()))
                .replace("{{releaseStatus}}", release.getStatus().name())
                .replace("{{releaseGitRef}}", esc(release.getGitRef() != null ? release.getGitRef() : "N/A"))
                .replace("{{evidenceCount}}", String.valueOf(evidences.size()))
                .replace("{{evidenceRows}}", buildEvidenceRows(evidences))
                .replace("{{componentCount}}", String.valueOf(components.size()))
                .replace("{{componentRows}}", buildComponentRows(components))
                .replace("{{findingRows}}", buildFindingRows(enrichedFindings))
                .replace("{{auditHashHead}}", auditHashHead != null ? auditHashHead : "N/A");

        return renderPdf(html);
    }

    private String buildEvidenceRows(List<Evidence> evidences) {
        StringBuilder sb = new StringBuilder();
        for (Evidence e : evidences) {
            sb.append("<tr>")
                    .append("<td>").append(esc(e.getType().name())).append("</td>")
                    .append("<td>").append(esc(e.getFilename())).append("</td>")
                    .append("<td class=\"mono\">").append(e.getSha256(), 0, Math.min(24, e.getSha256().length())).append("...</td>")
                    .append("<td>").append(DATE_SHORT.format(e.getCreatedAt())).append("</td>")
                    .append("</tr>\n");
        }
        return sb.toString();
    }

    private String buildComponentRows(List<Component> components) {
        StringBuilder sb = new StringBuilder();
        for (Component c : components) {
            sb.append("<tr>")
                    .append("<td>").append(esc(c.getName())).append("</td>")
                    .append("<td>").append(esc(c.getVersion() != null ? c.getVersion() : "")).append("</td>")
                    .append("<td>").append(esc(c.getType())).append("</td>")
                    .append("<td class=\"mono\">").append(esc(truncate(c.getPurl(), 60))).append("</td>")
                    .append("</tr>\n");
        }
        return sb.toString();
    }

    private String buildFindingRows(List<EnrichedFinding> findings) {
        StringBuilder sb = new StringBuilder();
        for (EnrichedFinding ef : findings) {
            Finding f = ef.finding();
            String sev = ef.severity() != null ? ef.severity() : "UNKNOWN";
            String osvId = ef.osvId() != null ? ef.osvId() : "";
            String summary = ef.summary() != null ? ef.summary() : "";

            sb.append("<tr>")
                    .append("<td><span class=\"severity sev-").append(sev).append("\">").append(sev).append("</span></td>")
                    .append("<td class=\"mono\">").append(esc(osvId)).append("</td>")
                    .append("<td>").append(esc(truncate(summary, 80))).append("</td>")
                    .append("<td><span class=\"status st-").append(f.getStatus()).append("\">").append(esc(f.getStatus())).append("</span></td>")
                    .append("<td>").append(esc(f.getSource())).append("</td>")
                    .append("<td>").append(DATE_SHORT.format(f.getDetectedAt())).append("</td>")
                    .append("</tr>\n");

            for (FindingDecision d : ef.decisions()) {
                sb.append("<tr class=\"decision-row\">")
                        .append("<td colspan=\"6\">")
                        .append("D\u00e9cision : ").append(esc(d.getDecisionType()))
                        .append(" \u2014 ").append(esc(truncate(d.getRationale(), 100)))
                        .append(" (").append(DATE_SHORT.format(d.getCreatedAt())).append(")")
                        .append("</td>")
                        .append("</tr>\n");
            }
        }
        return sb.toString();
    }

    private byte[] renderPdf(String html) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to render PDF", e);
            throw new IOException("PDF generation failed", e);
        }
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

    private static String esc(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max) + "..." : text;
    }
}
