package com.lexsecura.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.ai.AiDtos;
import com.lexsecura.application.ai.AiService;
import com.lexsecura.domain.model.AiArtifact;
import com.lexsecura.domain.model.AiJob;
import com.lexsecura.infrastructure.ai.QuestionnaireParser;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI", description = "AI-assisted draft generation (local LLM)")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final AiService aiService;
    private final QuestionnaireParser questionnaireParser;
    private final ObjectMapper objectMapper;

    public AiController(AiService aiService, QuestionnaireParser questionnaireParser, ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.questionnaireParser = questionnaireParser;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/srp-draft")
    @Operation(summary = "Generate an SRP submission draft via AI")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    public ResponseEntity<AiDtos.AiJobResponse> generateSrpDraft(@Valid @RequestBody AiDtos.SrpDraftRequest request) {
        log.info("AI SRP draft requested for event={} type={}", request.craEventId(), request.submissionType());
        AiJob job = aiService.generateSrpDraft(request.craEventId(), request.submissionType());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(job));
    }

    @PostMapping("/comm-pack")
    @Operation(summary = "Generate a Customer Communication Pack via AI")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    public ResponseEntity<AiDtos.AiJobResponse> generateCommPack(@Valid @RequestBody AiDtos.CommPackRequest request) {
        log.info("AI comm pack requested for event={}", request.craEventId());
        AiJob job = aiService.generateCommPack(request.craEventId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(job));
    }

    @PostMapping(value = "/questionnaire/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Parse a questionnaire file (xlsx/docx/txt) to extract text")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER', 'CONTRIBUTOR')")
    public ResponseEntity<String> parseQuestionnaire(@RequestParam("file") MultipartFile file) {
        log.info("Questionnaire parse requested: {}", file.getOriginalFilename());
        String text = questionnaireParser.parse(file);
        return ResponseEntity.ok(text);
    }

    @PostMapping("/questionnaire/fill")
    @Operation(summary = "Auto-fill questionnaire answers via AI")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER')")
    public ResponseEntity<AiDtos.AiJobResponse> fillQuestionnaire(@Valid @RequestBody AiDtos.QuestionnaireFillRequest request) {
        log.info("AI questionnaire fill requested");
        AiJob job = aiService.fillQuestionnaire(request.questionnaireText(), request.productId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(job));
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "Get AI job status and result")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_MANAGER', 'CONTRIBUTOR')")
    public ResponseEntity<AiDtos.AiJobResponse> getJob(@PathVariable UUID id) {
        AiService.AiJobResponse result = aiService.getJob(id);
        return ResponseEntity.ok(toResponse(result.job(), result.artifacts()));
    }

    private AiDtos.AiJobResponse toResponse(AiJob job) {
        List<AiArtifact> artifacts = aiService.getJob(job.getId()).artifacts();
        return toResponse(job, artifacts);
    }

    private AiDtos.AiJobResponse toResponse(AiJob job, List<AiArtifact> artifacts) {
        return new AiDtos.AiJobResponse(
                job.getId(),
                job.getJobType(),
                job.getStatus(),
                job.getModel(),
                job.getError(),
                job.getCreatedAt() != null ? job.getCreatedAt().toString() : null,
                job.getCompletedAt() != null ? job.getCompletedAt().toString() : null,
                artifacts.stream().map(a -> {
                    Object content;
                    try {
                        content = objectMapper.readTree(a.getContentJson());
                    } catch (Exception e) {
                        content = a.getContentJson();
                    }
                    return new AiDtos.AiArtifactResponse(
                            a.getId(),
                            a.getKind(),
                            content,
                            a.getCreatedAt() != null ? a.getCreatedAt().toString() : null
                    );
                }).toList()
        );
    }
}
