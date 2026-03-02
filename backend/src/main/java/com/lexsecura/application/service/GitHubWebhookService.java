package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.domain.model.ProductRepoMapping;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.repository.ProcessedWebhookEventRepository;
import com.lexsecura.domain.repository.ProductRepoMappingRepository;
import com.lexsecura.domain.repository.ReleaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class GitHubWebhookService {

    private static final Logger log = LoggerFactory.getLogger(GitHubWebhookService.class);
    private static final String FORGE = "GITHUB";

    private final ProductRepoMappingRepository mappingRepository;
    private final ProcessedWebhookEventRepository processedEventRepository;
    private final ReleaseRepository releaseRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public GitHubWebhookService(
            ProductRepoMappingRepository mappingRepository,
            ProcessedWebhookEventRepository processedEventRepository,
            ReleaseRepository releaseRepository,
            AuditService auditService,
            ObjectMapper objectMapper,
            @Value("${app.github.webhook-secret:}") String webhookSecret) {
        this.mappingRepository = mappingRepository;
        this.processedEventRepository = processedEventRepository;
        this.releaseRepository = releaseRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
    }

    public boolean verifySignature(String payload, String signature) {
        if (webhookSecret.isBlank()) return true;
        if (signature == null || signature.isBlank() || !signature.startsWith("sha256=")) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(hash);
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("HMAC verification error: {}", e.getMessage());
            return false;
        }
    }

    public Map<String, String> handleEvent(String eventType, String payload) {
        try {
            return switch (eventType) {
                case "release" -> handleRelease(payload);
                case "workflow_run" -> handleWorkflowRun(payload);
                case "ping" -> Map.of("status", "pong");
                default -> {
                    log.debug("Ignoring GitHub event: {}", eventType);
                    yield Map.of("status", "ignored", "event", eventType);
                }
            };
        } catch (Exception e) {
            log.error("Error processing GitHub webhook event {}: {}", eventType, e.getMessage());
            throw new RuntimeException("Failed to process webhook", e);
        }
    }

    private Map<String, String> handleRelease(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        String action = root.path("action").asText("");
        if (!"published".equals(action) && !"created".equals(action)) {
            log.info("Ignoring release action '{}' ", action);
            return Map.of("status", "ignored", "reason", "action=" + action);
        }

        String tagName = root.path("release").path("tag_name").asText("");
        String repoFullName = root.path("repository").path("full_name").asText("");
        String sha = root.path("release").path("target_commitish").asText(null);

        if (tagName.isBlank() || repoFullName.isBlank()) {
            return Map.of("status", "ignored", "reason", "missing tag or repo");
        }

        String eventId = "release:" + repoFullName + ":" + tagName;
        return createReleaseFromEvent(repoFullName, tagName, sha, eventId, "release");
    }

    private Map<String, String> handleWorkflowRun(String payload) throws Exception {
        JsonNode root = objectMapper.readTree(payload);
        String action = root.path("action").asText("");
        if (!"completed".equals(action)) {
            return Map.of("status", "ignored", "reason", "not completed");
        }

        String conclusion = root.path("workflow_run").path("conclusion").asText("");
        if (!"success".equals(conclusion)) {
            return Map.of("status", "ignored", "reason", "workflow not successful");
        }

        String repoFullName = root.path("repository").path("full_name").asText("");
        String sha = root.path("workflow_run").path("head_sha").asText("");
        String branch = root.path("workflow_run").path("head_branch").asText("");

        log.info("Workflow completed: repo={}, branch={}, sha={}", repoFullName, branch, sha);

        return Map.of(
                "status", "acknowledged",
                "message", "Use POST /integrations/ci/sbom with API key to upload SBOM artifact",
                "repo", repoFullName,
                "sha", sha
        );
    }

    private Map<String, String> createReleaseFromEvent(String repoFullName, String version,
                                                        String gitRef, String eventId,
                                                        String eventType) {
        if (processedEventRepository.existsByForgeAndEventId(FORGE, eventId)) {
            log.info("Webhook event already processed (idempotent skip): {}", eventId);
            return Map.of("status", "skipped", "reason", "already processed");
        }

        ProductRepoMapping mapping = mappingRepository.findByForgeAndRepoFullName(FORGE, repoFullName)
                .orElse(null);

        if (mapping == null) {
            log.info("No product mapping for GitHub repo {}, skipping", repoFullName);
            processedEventRepository.save(FORGE, eventId, eventType);
            return Map.of("status", "skipped", "reason", "no product mapping for " + repoFullName);
        }

        // Strip leading 'v' from tag if present (v1.0.0 -> 1.0.0)
        String cleanVersion = version.startsWith("v") ? version.substring(1) : version;

        Release release = new Release(mapping.getProductId(), cleanVersion);
        release.setOrgId(mapping.getOrgId());
        release.setGitRef(gitRef);
        release = releaseRepository.save(release);

        processedEventRepository.save(FORGE, eventId, eventType);

        auditService.record(mapping.getOrgId(), "RELEASE", release.getId(),
                "CREATE_VIA_WEBHOOK", UUID.fromString("00000000-0000-0000-0000-000000000000"),
                Map.of("source", "github", "repo", repoFullName,
                        "version", version, "eventId", eventId));

        log.info("Release created from GitHub webhook: productId={}, version={}, releaseId={}",
                mapping.getProductId(), cleanVersion, release.getId());

        return Map.of("status", "created", "releaseId", release.getId().toString(),
                "version", cleanVersion);
    }
}
