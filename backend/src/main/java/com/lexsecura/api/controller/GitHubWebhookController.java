package com.lexsecura.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

/**
 * GitHub Actions webhook receiver.
 * Accepts workflow_run completed events to trigger SBOM processing.
 * Auth via HMAC-SHA256 signature (X-Hub-Signature-256 header).
 */
@RestController
@RequestMapping("/integrations/github")
public class GitHubWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GitHubWebhookController.class);

    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public GitHubWebhookController(ObjectMapper objectMapper,
                                   @Value("${app.github.webhook-secret:}") String webhookSecret) {
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "") String event,
            @RequestHeader(value = "X-Hub-Signature-256", defaultValue = "") String signature,
            @RequestBody String payload) {

        // Verify HMAC signature if secret is configured
        if (!webhookSecret.isBlank() && !verifySignature(payload, signature)) {
            log.warn("GitHub webhook signature verification failed");
            return ResponseEntity.status(401).body(Map.of("error", "Invalid signature"));
        }

        log.info("GitHub webhook received: event={}", event);

        return switch (event) {
            case "workflow_run" -> handleWorkflowRun(payload);
            case "release" -> handleRelease(payload);
            case "ping" -> ResponseEntity.ok(Map.of("status", "pong"));
            default -> {
                log.debug("Ignoring GitHub event: {}", event);
                yield ResponseEntity.ok(Map.of("status", "ignored", "event", event));
            }
        };
    }

    private ResponseEntity<Map<String, String>> handleWorkflowRun(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String action = root.path("action").asText("");
            if (!"completed".equals(action)) {
                return ResponseEntity.ok(Map.of("status", "ignored", "reason", "not completed"));
            }

            String conclusion = root.path("workflow_run").path("conclusion").asText("");
            String repoName = root.path("repository").path("full_name").asText("");
            String branch = root.path("workflow_run").path("head_branch").asText("");
            String sha = root.path("workflow_run").path("head_sha").asText("");

            log.info("Workflow completed: repo={}, branch={}, conclusion={}, sha={}",
                    repoName, branch, conclusion, sha);

            if ("success".equals(conclusion)) {
                // Signal that a new SBOM may be available via the CI upload endpoint
                return ResponseEntity.ok(Map.of(
                        "status", "acknowledged",
                        "message", "Use POST /integrations/ci/sbom with API key to upload SBOM artifact",
                        "repo", repoName,
                        "sha", sha
                ));
            }

            return ResponseEntity.ok(Map.of("status", "ignored", "reason", "workflow not successful"));
        } catch (Exception e) {
            log.error("Failed to process workflow_run webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payload"));
        }
    }

    private ResponseEntity<Map<String, String>> handleRelease(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String action = root.path("action").asText("");
            String tagName = root.path("release").path("tag_name").asText("");
            String repoName = root.path("repository").path("full_name").asText("");

            log.info("Release event: repo={}, tag={}, action={}", repoName, tagName, action);

            return ResponseEntity.ok(Map.of(
                    "status", "acknowledged",
                    "tag", tagName,
                    "repo", repoName
            ));
        } catch (Exception e) {
            log.error("Failed to process release webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payload"));
        }
    }

    private boolean verifySignature(String payload, String signature) {
        if (signature.isBlank() || !signature.startsWith("sha256=")) return false;
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
}
