package com.lexsecura.application.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.service.EntityNotFoundException;
import com.lexsecura.domain.model.*;
import com.lexsecura.domain.model.Component;
import com.lexsecura.domain.repository.*;
import com.lexsecura.infrastructure.ai.AiRedactor;
import com.lexsecura.infrastructure.ai.AiSchemaValidator;
import com.lexsecura.infrastructure.ai.OllamaClient;
import com.lexsecura.infrastructure.security.TenantContext;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.of("Europe/Paris"));
    private static final int MAX_RETRIES = 2;

    private final OllamaClient ollamaClient;
    private final AiRedactor redactor;
    private final AiSchemaValidator validator;
    private final AiJobRepository aiJobRepository;
    private final AiArtifactRepository aiArtifactRepository;
    private final CraEventRepository craEventRepository;
    private final ProductRepository productRepository;
    private final ReleaseRepository releaseRepository;
    private final FindingRepository findingRepository;
    private final FindingDecisionRepository decisionRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final EvidenceRepository evidenceRepository;
    private final ComponentRepository componentRepository;
    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;
    private final Counter jobsCounter;

    public AiService(
            OllamaClient ollamaClient,
            AiRedactor redactor,
            AiSchemaValidator validator,
            AiJobRepository aiJobRepository,
            AiArtifactRepository aiArtifactRepository,
            CraEventRepository craEventRepository,
            ProductRepository productRepository,
            ReleaseRepository releaseRepository,
            FindingRepository findingRepository,
            FindingDecisionRepository decisionRepository,
            VulnerabilityRepository vulnerabilityRepository,
            EvidenceRepository evidenceRepository,
            ComponentRepository componentRepository,
            AuditEventRepository auditEventRepository,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry) {
        this.ollamaClient = ollamaClient;
        this.redactor = redactor;
        this.validator = validator;
        this.aiJobRepository = aiJobRepository;
        this.aiArtifactRepository = aiArtifactRepository;
        this.craEventRepository = craEventRepository;
        this.productRepository = productRepository;
        this.releaseRepository = releaseRepository;
        this.findingRepository = findingRepository;
        this.decisionRepository = decisionRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.evidenceRepository = evidenceRepository;
        this.componentRepository = componentRepository;
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
        this.jobsCounter = Counter.builder("ai_jobs_total")
                .description("Total AI jobs created")
                .register(meterRegistry);
    }

    // ── SRP Draft ──────────────────────────────────────────

    @Transactional
    public AiJob generateSrpDraft(UUID craEventId, String submissionType) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();
        jobsCounter.increment();

        CraEvent event = craEventRepository.findByIdAndOrgId(craEventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found: " + craEventId));
        Product product = productRepository.findByIdAndOrgId(event.getProductId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        String context = buildEventContext(event, product, orgId);
        String userPrompt = buildSrpPrompt(context, submissionType);
        String redactedPrompt = redactor.redact(userPrompt);

        AiJob job = createJob(orgId, userId, "SRP_DRAFT", redactedPrompt,
                Map.of("craEventId", craEventId.toString(), "submissionType", submissionType));

        String result = callWithRetry(SRP_SYSTEM_PROMPT, redactedPrompt, "schemas/ai/srp-draft.schema.json");

        if (result != null) {
            completeJob(job, result, "SRP_DRAFT");
        } else {
            failJob(job, "Validation failed after retries, fallback used");
            result = srpFallback(event, product, submissionType);
            saveArtifact(job.getId(), "SRP_DRAFT", result);
        }

        return aiJobRepository.findByIdAndOrgId(job.getId(), orgId).orElse(job);
    }

    // ── Communication Pack ─────────────────────────────────

    @Transactional
    public AiJob generateCommPack(UUID craEventId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();
        jobsCounter.increment();

        CraEvent event = craEventRepository.findByIdAndOrgId(craEventId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("CRA Event not found"));
        Product product = productRepository.findByIdAndOrgId(event.getProductId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        String context = buildEventContext(event, product, orgId);
        String userPrompt = buildCommPackPrompt(context);
        String redactedPrompt = redactor.redact(userPrompt);

        AiJob job = createJob(orgId, userId, "COMM_PACK", redactedPrompt,
                Map.of("craEventId", craEventId.toString()));

        String result = callWithRetry(COMM_SYSTEM_PROMPT, redactedPrompt, "schemas/ai/comm-pack.schema.json");

        if (result != null) {
            completeJob(job, result, "ADVISORY");
        } else {
            failJob(job, "Validation failed after retries, fallback used");
            result = commPackFallback(event, product);
            saveArtifact(job.getId(), "ADVISORY", result);
        }

        return aiJobRepository.findByIdAndOrgId(job.getId(), orgId).orElse(job);
    }

    // ── Questionnaire Fill ─────────────────────────────────

    @Transactional
    public AiJob fillQuestionnaire(String questionnaireText, UUID productId) {
        UUID orgId = TenantContext.getOrgId();
        UUID userId = TenantContext.getUserId();
        jobsCounter.increment();

        String orgContext = buildOrgContext(orgId, productId);
        String userPrompt = buildQuestionnairePrompt(questionnaireText, orgContext);
        String redactedPrompt = redactor.redact(userPrompt);

        Map<String, String> params = new HashMap<>();
        if (productId != null) params.put("productId", productId.toString());

        AiJob job = createJob(orgId, userId, "QUESTIONNAIRE_FILL", redactedPrompt, params);

        String result = callWithRetry(QUESTIONNAIRE_SYSTEM_PROMPT, redactedPrompt, "schemas/ai/questionnaire.schema.json");

        if (result != null) {
            completeJob(job, result, "QUESTIONNAIRE_ANSWERS");
        } else {
            failJob(job, "Validation failed after retries, fallback used");
            result = questionnaireFallback(questionnaireText);
            saveArtifact(job.getId(), "QUESTIONNAIRE_ANSWERS", result);
        }

        return aiJobRepository.findByIdAndOrgId(job.getId(), orgId).orElse(job);
    }

    // ── Get Job ────────────────────────────────────────────

    public AiJobResponse getJob(UUID jobId) {
        UUID orgId = TenantContext.getOrgId();
        AiJob job = aiJobRepository.findByIdAndOrgId(jobId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("AI Job not found: " + jobId));
        List<AiArtifact> artifacts = aiArtifactRepository.findAllByAiJobId(jobId);
        return new AiJobResponse(job, artifacts);
    }

    public record AiJobResponse(AiJob job, List<AiArtifact> artifacts) {}

    // ── Private helpers ────────────────────────────────────

    private String callWithRetry(String systemPrompt, String userPrompt, String schemaPath) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                String raw = ollamaClient.generate(systemPrompt, userPrompt);
                String json = extractJson(raw);
                var validation = validator.validate(json, schemaPath);
                if (validation.valid()) {
                    return validation.json();
                }
                log.warn("Attempt {}: JSON validation failed: {}", attempt + 1, validation.errors());
            } catch (Exception e) {
                log.warn("Attempt {}: Generation failed: {}", attempt + 1, e.getMessage());
            }
        }
        return null;
    }

    private String extractJson(String raw) {
        if (raw == null) return "{}";
        // Try to find JSON block between ```json ... ``` or { ... } or [ ... ]
        int jsonStart = raw.indexOf("```json");
        if (jsonStart >= 0) {
            int start = raw.indexOf('\n', jsonStart) + 1;
            int end = raw.indexOf("```", start);
            if (end > start) return raw.substring(start, end).trim();
        }
        // Try raw JSON
        int braceStart = raw.indexOf('{');
        int bracketStart = raw.indexOf('[');
        int start = -1;
        char openChar = '{';
        char closeChar = '}';
        if (braceStart >= 0 && (bracketStart < 0 || braceStart < bracketStart)) {
            start = braceStart;
        } else if (bracketStart >= 0) {
            start = bracketStart;
            openChar = '[';
            closeChar = ']';
        }
        if (start >= 0) {
            int depth = 0;
            for (int i = start; i < raw.length(); i++) {
                if (raw.charAt(i) == openChar) depth++;
                else if (raw.charAt(i) == closeChar) depth--;
                if (depth == 0) return raw.substring(start, i + 1);
            }
        }
        return raw.trim();
    }

    private AiJob createJob(UUID orgId, UUID userId, String jobType, String prompt, Map<String, String> params) {
        AiJob job = new AiJob();
        job.setOrgId(orgId);
        job.setCreatedBy(userId);
        job.setJobType(jobType);
        job.setStatus("RUNNING");
        job.setModel(ollamaClient.getModel());
        job.setInputHash(sha256(prompt));
        try {
            job.setParamsJson(objectMapper.writeValueAsString(params));
        } catch (Exception e) {
            job.setParamsJson("{}");
        }
        return aiJobRepository.save(job);
    }

    private void completeJob(AiJob job, String resultJson, String artifactKind) {
        job.setStatus("COMPLETED");
        job.setOutputHash(sha256(resultJson));
        job.setCompletedAt(Instant.now());
        aiJobRepository.save(job);
        saveArtifact(job.getId(), artifactKind, resultJson);
    }

    private void failJob(AiJob job, String error) {
        job.setStatus("FAILED");
        job.setError(error);
        job.setCompletedAt(Instant.now());
        aiJobRepository.save(job);
    }

    private void saveArtifact(UUID jobId, String kind, String contentJson) {
        AiArtifact artifact = new AiArtifact();
        artifact.setAiJobId(jobId);
        artifact.setKind(kind);
        artifact.setContentJson(contentJson);
        aiArtifactRepository.save(artifact);
    }

    // ── Context builders ───────────────────────────────────

    private String buildEventContext(CraEvent event, Product product, UUID orgId) {
        StringBuilder sb = new StringBuilder();
        sb.append("PRODUCT: ").append(product.getName())
                .append(" (type=").append(product.getType())
                .append(", criticality=").append(product.getCriticality()).append(")\n");
        sb.append("EVENT: ").append(event.getTitle())
                .append(" (type=").append(event.getEventType())
                .append(", status=").append(event.getStatus())
                .append(", detected=").append(DATE_FMT.format(event.getDetectedAt()));
        if (event.getPatchAvailableAt() != null)
            sb.append(", patch_available=").append(DATE_FMT.format(event.getPatchAvailableAt()));
        if (event.getResolvedAt() != null)
            sb.append(", resolved=").append(DATE_FMT.format(event.getResolvedAt()));
        sb.append(")\n");

        if (event.getDescription() != null) {
            sb.append("DESCRIPTION: ").append(truncate(event.getDescription(), 500)).append("\n");
        }

        // Linked releases + findings
        List<Release> releases = releaseRepository.findAllByProductId(event.getProductId());
        if (!releases.isEmpty()) {
            sb.append("RELEASES:\n");
            for (Release r : releases) {
                sb.append("  - ").append(r.getVersion()).append(" (status=").append(r.getStatus()).append(")\n");
                List<Finding> findings = findingRepository.findAllByReleaseId(r.getId());
                for (Finding f : findings) {
                    Vulnerability v = vulnerabilityRepository.findById(f.getVulnerabilityId()).orElse(null);
                    sb.append("    FINDING: status=").append(f.getStatus())
                            .append(", source=").append(f.getSource());
                    if (v != null) {
                        sb.append(", osvId=").append(v.getOsvId())
                                .append(", severity=").append(v.getSeverity())
                                .append(", summary=").append(truncate(v.getSummary(), 100));
                    }
                    sb.append("\n");
                    List<FindingDecision> decisions = decisionRepository.findAllByFindingId(f.getId());
                    for (FindingDecision d : decisions) {
                        sb.append("      DECISION: ").append(d.getDecisionType())
                                .append(" - ").append(truncate(d.getRationale(), 80)).append("\n");
                    }
                }
            }
        }

        // Evidences from releases
        for (Release r : releases) {
            List<Evidence> evs = evidenceRepository.findAllByReleaseIdAndOrgId(r.getId(), orgId);
            if (!evs.isEmpty()) {
                sb.append("EVIDENCES (").append(r.getVersion()).append("):\n");
                for (Evidence e : evs) {
                    sb.append("  - [").append(e.getType().name()).append("] ")
                            .append(e.getFilename()).append(" (id=").append(e.getId()).append(")\n");
                }
            }
        }

        return sb.toString();
    }

    private String buildOrgContext(UUID orgId, UUID productId) {
        StringBuilder sb = new StringBuilder();

        if (productId != null) {
            productRepository.findByIdAndOrgId(productId, orgId).ifPresent(p -> {
                sb.append("PRODUCT: ").append(p.getName())
                        .append(" (type=").append(p.getType())
                        .append(", criticality=").append(p.getCriticality()).append(")\n");
            });

            List<Release> releases = releaseRepository.findAllByProductId(productId);
            for (Release r : releases.stream().limit(5).toList()) {
                sb.append("RELEASE: ").append(r.getVersion()).append(" (status=").append(r.getStatus()).append(")\n");
                List<Evidence> evs = evidenceRepository.findAllByReleaseIdAndOrgId(r.getId(), orgId);
                for (Evidence e : evs) {
                    sb.append("  EVIDENCE: [").append(e.getType().name()).append("] ")
                            .append(e.getFilename()).append(" (id=").append(e.getId()).append(")\n");
                }
            }
        }

        List<Product> products = productRepository.findAllByOrgId(orgId);
        sb.append("ORG_PRODUCTS: ").append(products.stream().map(Product::getName).collect(Collectors.joining(", "))).append("\n");

        return sb.toString();
    }

    // ── Prompt builders ────────────────────────────────────

    private String buildSrpPrompt(String context, String submissionType) {
        return """
                Generate a %s draft for the following CRA security event.
                Use ONLY the data provided below. Reference internal IDs.

                %s

                Output STRICT JSON matching the schema:
                {summary, affected_versions[], impact, mitigation, patch_status, timeline[], references[]}
                Each factual statement MUST have at least 1 reference.
                If information is missing, use "UNKNOWN" and note it.
                """.formatted(submissionType.replace('_', ' ').toLowerCase(), context);
    }

    private String buildCommPackPrompt(String context) {
        return """
                Generate a Customer Communication Pack for the following CRA security event.
                Include: security advisory (markdown), client email (subject+body), and security release notes (markdown).
                Use ONLY the data below. Reference internal IDs.

                %s

                Output STRICT JSON:
                {advisory_markdown, email_subject, email_body, release_notes_markdown, references[]}
                Each factual statement MUST reference at least 1 internal entity.
                """.formatted(context);
    }

    private String buildQuestionnairePrompt(String questionnaire, String orgContext) {
        return """
                Answer the following security questionnaire using ONLY the organization data provided.
                For each question, provide an answer, a confidence level, and internal references.

                ORGANIZATION DATA:
                %s

                QUESTIONNAIRE:
                %s

                Output STRICT JSON array:
                [{question_id, question, answer, confidence, references[]}...]
                Use confidence "UNKNOWN" if data is insufficient. Never invent facts.
                """.formatted(orgContext, truncate(questionnaire, 6000));
    }

    // ── Fallbacks ──────────────────────────────────────────

    private String srpFallback(CraEvent event, Product product, String type) {
        try {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("summary", "[BROUILLON] " + event.getTitle() + " - " + product.getName());
            fallback.put("affected_versions", List.of("UNKNOWN"));
            fallback.put("impact", "Impact en cours d'évaluation - " + event.getEventType());
            fallback.put("mitigation", "UNKNOWN - Validation humaine requise");
            fallback.put("patch_status", event.getPatchAvailableAt() != null ? "Correctif disponible" : "En cours");
            fallback.put("timeline", List.of(
                    Map.of("date", DATE_FMT.format(event.getDetectedAt()), "event", "Détection"),
                    Map.of("date", DATE_FMT.format(Instant.now()), "event", "Brouillon généré")
            ));
            fallback.put("references", List.of(
                    Map.of("type", "product", "id", product.getId().toString(), "label", product.getName())
            ));
            return objectMapper.writeValueAsString(fallback);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String commPackFallback(CraEvent event, Product product) {
        try {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("advisory_markdown", "# Avis de sécurité - " + product.getName() + "\n\n" + event.getTitle() + "\n\n*Brouillon IA à valider.*");
            fallback.put("email_subject", "[Sécurité] " + product.getName() + " - " + event.getTitle());
            fallback.put("email_body", "Bonjour,\n\nNous vous informons d'un événement de sécurité concernant " + product.getName() + ".\n\n" + event.getTitle() + "\n\n*Ce message est un brouillon généré par IA et nécessite une validation humaine.*");
            fallback.put("release_notes_markdown", "## Notes de version sécurité\n\n- " + event.getTitle() + "\n\n*Brouillon IA à valider.*");
            fallback.put("references", List.of(
                    Map.of("type", "product", "id", product.getId().toString(), "label", product.getName())
            ));
            return objectMapper.writeValueAsString(fallback);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String questionnaireFallback(String text) {
        try {
            String[] lines = text.split("\n");
            List<Map<String, Object>> answers = new ArrayList<>();
            int id = 1;
            for (String line : lines) {
                if (line.isBlank()) continue;
                Map<String, Object> qa = new LinkedHashMap<>();
                qa.put("question_id", "Q" + id++);
                qa.put("question", truncate(line.trim(), 200));
                qa.put("answer", "UNKNOWN - Validation humaine requise");
                qa.put("confidence", "UNKNOWN");
                qa.put("references", List.of());
                answers.add(qa);
                if (id > 50) break;
            }
            return objectMapper.writeValueAsString(answers);
        } catch (Exception e) {
            return "[]";
        }
    }

    // ── Utilities ──────────────────────────────────────────

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return "hash-error";
        }
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max) + "..." : text;
    }

    // ── System Prompts ─────────────────────────────────────

    private static final String SRP_SYSTEM_PROMPT = """
            You are a CRA (Cyber Resilience Act) compliance assistant for LexSecura.
            Your role is to draft SRP (Single Reporting Platform) submissions.
            Rules:
            1. Output ONLY valid JSON matching the required schema. No markdown, no explanation.
            2. Every factual statement must reference at least one internal entity (product, release, finding, evidence).
            3. If information is missing, use "UNKNOWN" and add a note.
            4. Never make legal decisions. You produce DRAFTS for human validation.
            5. Use professional, technical language suitable for EU regulatory submissions.
            6. Write in English (the regulatory language).
            """;

    private static final String COMM_SYSTEM_PROMPT = """
            You are a security communication specialist for LexSecura.
            Your role is to draft customer-facing security communications.
            Rules:
            1. Output ONLY valid JSON matching the required schema. No markdown wrapper.
            2. advisory_markdown: formal security advisory with severity, impact, affected versions, remediation.
            3. email_subject + email_body: professional, clear, actionable customer email.
            4. release_notes_markdown: concise security release notes for developers.
            5. Reference internal entities in the references array.
            6. Never disclose internal infrastructure details or secrets.
            7. All content is a DRAFT requiring human review.
            """;

    private static final String QUESTIONNAIRE_SYSTEM_PROMPT = """
            You are a security questionnaire assistant for LexSecura.
            Your role is to pre-fill security questionnaire answers using internal data.
            Rules:
            1. Output ONLY a valid JSON array. No markdown wrapper.
            2. Answer each question based ONLY on provided organization data.
            3. Set confidence to HIGH/MEDIUM/LOW/UNKNOWN based on data availability.
            4. Reference supporting internal entities (releases, evidences, etc.).
            5. If data is insufficient, answer "UNKNOWN" with confidence "UNKNOWN".
            6. Never fabricate information. All answers are DRAFTS for human validation.
            """;
}
