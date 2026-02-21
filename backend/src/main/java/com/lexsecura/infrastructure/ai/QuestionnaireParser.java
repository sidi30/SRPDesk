package com.lexsecura.infrastructure.ai;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class QuestionnaireParser {

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireParser.class);

    public String parse(MultipartFile file) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        try (InputStream is = file.getInputStream()) {
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                return parseXlsx(is);
            } else if (filename.endsWith(".docx")) {
                return parseDocx(is);
            } else if (filename.endsWith(".txt") || filename.endsWith(".csv")) {
                return new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            } else {
                throw new UnsupportedOperationException(
                        "Format non supporté : " + filename + ". Formats acceptés : xlsx, docx, txt, csv");
            }
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse questionnaire file: {}", filename, e);
            throw new RuntimeException("Erreur de lecture du fichier : " + e.getMessage(), e);
        }
    }

    private String parseXlsx(InputStream is) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            StringBuilder sb = new StringBuilder();
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                sb.append("=== Feuille: ").append(sheet.getSheetName()).append(" ===\n");
                for (Row row : sheet) {
                    List<String> cells = new ArrayList<>();
                    for (Cell cell : row) {
                        cells.add(getCellValue(cell));
                    }
                    sb.append(String.join(" | ", cells)).append("\n");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    private String parseDocx(InputStream is) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph para : doc.getParagraphs()) {
                String text = para.getText();
                if (text != null && !text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }
            return sb.toString();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
