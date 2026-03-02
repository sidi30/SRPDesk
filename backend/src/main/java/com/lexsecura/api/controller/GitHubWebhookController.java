package com.lexsecura.api.controller;

import com.lexsecura.application.service.GitHubWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/integrations/github")
public class GitHubWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GitHubWebhookController.class);

    private final GitHubWebhookService gitHubWebhookService;

    public GitHubWebhookController(GitHubWebhookService gitHubWebhookService) {
        this.gitHubWebhookService = gitHubWebhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "") String event,
            @RequestHeader(value = "X-Hub-Signature-256", defaultValue = "") String signature,
            @RequestBody String payload) {

        if (!gitHubWebhookService.verifySignature(payload, signature)) {
            log.warn("GitHub webhook signature verification failed");
            return ResponseEntity.status(401).body(Map.of("error", "Invalid signature"));
        }

        log.info("GitHub webhook received: event={}", event);

        try {
            Map<String, String> result = gitHubWebhookService.handleEvent(event, payload);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to process GitHub webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Processing failed"));
        }
    }
}
