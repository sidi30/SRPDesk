package com.lexsecura.extras.ai.questionnaire.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.ai.AiDtos;
import com.lexsecura.application.ai.AiService;
import com.lexsecura.domain.model.AiArtifact;
import com.lexsecura.domain.model.AiJob;
import com.lexsecura.extras.ai.questionnaire.service.QuestionnaireParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/questionnaire")
@Tag(name = "AI Questionnaire", description = "AI-powered questionnaire parsing and filling (extras module)")
public class QuestionnaireController {

    private static final Logger log = LoggerFactory.getLogger(QuestionnaireController.class);

    private final AiService aiService;
    private final QuestionnaireParser questionnaireParser;
    private final ObjectMapper objectMapper;

    public QuestionnaireController(AiService aiService, QuestionnaireParser questionnaireParser,
                                   ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.questionnaireParser = questionnaireParser;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Parse a questionnaire file (xlsx/docx/txt) to extract text")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER', 'CONTRIBUTOR')")
    public ResponseEntity<String> parse(@RequestParam("file") MultipartFile file) {
        log.info("Questionnaire parse requested: {}", file.getOriginalFilename());
        String text = questionnaireParser.parse(file);
        return ResponseEntity.ok(text);
    }

    @PostMapping("/fill")
    @Operation(summary = "Auto-fill questionnaire answers via AI")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    public ResponseEntity<AiDtos.AiJobResponse> fill(@Valid @RequestBody AiDtos.QuestionnaireFillRequest request) {
        log.info("AI questionnaire fill requested");
        AiJob job = aiService.fillQuestionnaire(request.questionnaireText(), request.productId());

        List<AiArtifact> artifacts = aiService.getJob(job.getId()).artifacts();
        AiDtos.AiJobResponse response = new AiDtos.AiJobResponse(
                job.getId(), job.getJobType(), job.getStatus(), job.getModel(), job.getError(),
                job.getCreatedAt() != null ? job.getCreatedAt().toString() : null,
                job.getCompletedAt() != null ? job.getCompletedAt().toString() : null,
                artifacts.stream().map(a -> {
                    Object content;
                    try { content = objectMapper.readTree(a.getContentJson()); }
                    catch (Exception e) { content = a.getContentJson(); }
                    return new AiDtos.AiArtifactResponse(a.getId(), a.getKind(), content,
                            a.getCreatedAt() != null ? a.getCreatedAt().toString() : null);
                }).toList()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
