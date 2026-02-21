package com.lexsecura.application.service;

import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PdfReportGenerator {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 14;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.of("UTC"));

    public byte[] generate(Product product, Release release, List<Evidence> evidences,
                           List<Component> components, List<Finding> findings,
                           Map<UUID, List<FindingDecision>> decisionsByFinding,
                           String auditHashHead) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            float[] yRef = {0};
            PDPageContentStream[] csRef = {null};

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontMono = new PDType1Font(Standard14Fonts.FontName.COURIER);

            // Start first page
            csRef[0] = newPage(doc, yRef);

            // Title
            writeText(csRef, yRef, doc, fontBold, 18, "CRA Compliance Report");
            writeText(csRef, yRef, doc, fontRegular, 10, "Generated: " + DATE_FMT.format(Instant.now()));
            yRef[0] -= LINE_HEIGHT;

            // Product section
            writeText(csRef, yRef, doc, fontBold, 14, "Product");
            writeText(csRef, yRef, doc, fontRegular, 10, "Name: " + product.getName());
            writeText(csRef, yRef, doc, fontRegular, 10, "Type: " + product.getType());
            writeText(csRef, yRef, doc, fontRegular, 10, "Criticality: " + product.getCriticality());
            yRef[0] -= LINE_HEIGHT;

            // Release section
            writeText(csRef, yRef, doc, fontBold, 14, "Release");
            writeText(csRef, yRef, doc, fontRegular, 10, "Version: " + release.getVersion());
            writeText(csRef, yRef, doc, fontRegular, 10, "Status: " + release.getStatus().name());
            if (release.getGitRef() != null) {
                writeText(csRef, yRef, doc, fontRegular, 10, "Git Ref: " + release.getGitRef());
            }
            yRef[0] -= LINE_HEIGHT;

            // Evidences section
            writeText(csRef, yRef, doc, fontBold, 14, "Evidences (" + evidences.size() + ")");
            for (Evidence e : evidences) {
                checkPageBreak(csRef, yRef, doc);
                writeText(csRef, yRef, doc, fontRegular, 9,
                        "  - [" + e.getType().name() + "] " + e.getFilename() + " (SHA-256: " + e.getSha256().substring(0, 16) + "...)");
            }
            yRef[0] -= LINE_HEIGHT;

            // Components section
            writeText(csRef, yRef, doc, fontBold, 14, "SBOM Components (" + components.size() + ")");
            int compLimit = Math.min(components.size(), 30);
            for (int i = 0; i < compLimit; i++) {
                checkPageBreak(csRef, yRef, doc);
                Component c = components.get(i);
                writeText(csRef, yRef, doc, fontMono, 8,
                        "  " + c.getName() + " " + (c.getVersion() != null ? c.getVersion() : "") + " [" + c.getType() + "]");
            }
            if (components.size() > 30) {
                writeText(csRef, yRef, doc, fontRegular, 9,
                        "  ... and " + (components.size() - 30) + " more components");
            }
            yRef[0] -= LINE_HEIGHT;

            // Findings section
            long openCount = findings.stream().filter(f -> "OPEN".equals(f.getStatus())).count();
            writeText(csRef, yRef, doc, fontBold, 14, "Findings (" + findings.size() + " total, " + openCount + " open)");
            int findingLimit = Math.min(findings.size(), 20);
            for (int i = 0; i < findingLimit; i++) {
                checkPageBreak(csRef, yRef, doc);
                Finding f = findings.get(i);
                writeText(csRef, yRef, doc, fontRegular, 9,
                        "  - [" + f.getStatus() + "] " + f.getSource() + " " + f.getDetectedAt().toString().substring(0, 10));

                List<FindingDecision> decisions = decisionsByFinding.getOrDefault(f.getId(), List.of());
                for (FindingDecision d : decisions) {
                    checkPageBreak(csRef, yRef, doc);
                    writeText(csRef, yRef, doc, fontRegular, 8,
                            "      Decision: " + d.getDecisionType() + " - " + truncate(d.getRationale(), 60));
                }
            }
            yRef[0] -= LINE_HEIGHT;

            // Audit hash
            checkPageBreak(csRef, yRef, doc);
            writeText(csRef, yRef, doc, fontBold, 12, "Audit Trail");
            writeText(csRef, yRef, doc, fontMono, 8,
                    "  Hash head: " + (auditHashHead != null ? auditHashHead : "N/A"));

            csRef[0].close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private PDPageContentStream newPage(PDDocument doc, float[] yRef) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        yRef[0] = page.getMediaBox().getHeight() - MARGIN;
        return new PDPageContentStream(doc, page);
    }

    private void checkPageBreak(PDPageContentStream[] csRef, float[] yRef, PDDocument doc) throws IOException {
        if (yRef[0] < MARGIN + 30) {
            csRef[0].close();
            csRef[0] = newPage(doc, yRef);
        }
    }

    private void writeText(PDPageContentStream[] csRef, float[] yRef, PDDocument doc,
                           PDType1Font font, float fontSize, String text) throws IOException {
        checkPageBreak(csRef, yRef, doc);
        csRef[0].beginText();
        csRef[0].setFont(font, fontSize);
        csRef[0].newLineAtOffset(MARGIN, yRef[0]);
        csRef[0].showText(sanitize(text));
        csRef[0].endText();
        yRef[0] -= LINE_HEIGHT + (fontSize > 12 ? 6 : 2);
    }

    private String sanitize(String text) {
        if (text == null) return "";
        return text.replace("\n", " ").replace("\r", "").replace("\t", "  ");
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max) + "..." : text;
    }
}
