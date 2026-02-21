package com.lexsecura.api.controller;

import com.lexsecura.application.service.GitLabWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/integrations/gitlab")
@Tag(name = "GitLab Integration", description = "GitLab webhook receiver")
public class GitLabWebhookController {

    private static final Logger log = LoggerFactory.getLogger(GitLabWebhookController.class);

    private final GitLabWebhookService webhookService;

    public GitLabWebhookController(GitLabWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/webhook")
    @Operation(summary = "Receive GitLab webhook events (tag_push, release)")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "X-Gitlab-Token", required = false) String token,
            @RequestHeader(value = "X-Gitlab-Event", required = false) String eventType,
            @RequestBody String body) {

        if (!webhookService.verifySignature(token)) {
            log.warn("GitLab webhook signature verification failed");
            return ResponseEntity.status(401).build();
        }

        log.info("Received GitLab webhook: event={}", eventType);
        webhookService.handleWebhook(eventType, body);

        return ResponseEntity.ok().build();
    }
}
