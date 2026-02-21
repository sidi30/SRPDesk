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

import java.security.MessageDigest;
import java.util.UUID;

@Service
@Transactional
public class GitLabWebhookService {

    private static final Logger log = LoggerFactory.getLogger(GitLabWebhookService.class);
    private static final String FORGE = "GITLAB";

    private final ProductRepoMappingRepository mappingRepository;
    private final ProcessedWebhookEventRepository processedEventRepository;
    private final ReleaseRepository releaseRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public GitLabWebhookService(
            ProductRepoMappingRepository mappingRepository,
            ProcessedWebhookEventRepository processedEventRepository,
            ReleaseRepository releaseRepository,
            AuditService auditService,
            ObjectMapper objectMapper,
            @Value("${app.gitlab.webhook-secret:}") String webhookSecret) {
        this.mappingRepository = mappingRepository;
        this.processedEventRepository = processedEventRepository;
        this.releaseRepository = releaseRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
    }

    public boolean verifySignature(String tokenHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("No webhook secret configured — rejecting all webhooks");
            return false;
        }
        return MessageDigest.isEqual(
                webhookSecret.getBytes(),
                (tokenHeader != null ? tokenHeader : "").getBytes());
    }

    public void handleWebhook(String eventType, String body) {
        try {
            JsonNode root = objectMapper.readTree(body);

            if ("tag_push".equals(eventType) || "Tag Push Hook".equals(eventType)) {
                handleTagPush(root);
            } else if ("release".equals(eventType) || "Release Hook".equals(eventType)) {
                handleRelease(root);
            } else {
                log.info("Ignoring unsupported GitLab event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error processing GitLab webhook", e);
            throw new RuntimeException("Failed to process webhook", e);
        }
    }

    private void handleTagPush(JsonNode root) {
        long gitlabProjectId = root.path("project_id").asLong(
                root.path("project").path("id").asLong(0));
        String ref = root.path("ref").asText("");
        String tag = ref.startsWith("refs/tags/") ? ref.substring("refs/tags/".length()) : ref;
        String checkoutSha = root.path("checkout_sha").asText(null);

        String eventId = "tag_push:" + gitlabProjectId + ":" + tag;
        createReleaseFromEvent(gitlabProjectId, tag, checkoutSha, null, eventId, "tag_push");
    }

    private void handleRelease(JsonNode root) {
        long gitlabProjectId = root.path("project").path("id").asLong(0);
        String tag = root.path("tag").asText(root.path("release").path("tag_name").asText(""));
        String action = root.path("action").asText("");

        if (!"create".equals(action)) {
            log.info("Ignoring release action '{}' for project {}", action, gitlabProjectId);
            return;
        }

        JsonNode commit = root.path("commit");
        String sha = commit.path("id").asText(null);

        String eventId = "release:" + gitlabProjectId + ":" + tag;
        createReleaseFromEvent(gitlabProjectId, tag, sha, null, eventId, "release");
    }

    private void createReleaseFromEvent(long gitlabProjectId, String version,
                                         String gitRef, String buildId,
                                         String eventId, String eventType) {
        if (processedEventRepository.existsByForgeAndEventId(FORGE, eventId)) {
            log.info("Webhook event already processed (idempotent skip): {}", eventId);
            return;
        }

        ProductRepoMapping mapping = mappingRepository.findByForgeAndProjectId(FORGE, gitlabProjectId)
                .orElse(null);

        if (mapping == null) {
            log.info("No product mapping for GitLab project {}, skipping", gitlabProjectId);
            processedEventRepository.save(FORGE, eventId, eventType);
            return;
        }

        Release release = new Release(mapping.getProductId(), version);
        release.setGitRef(gitRef);
        release.setBuildId(buildId);
        release = releaseRepository.save(release);

        processedEventRepository.save(FORGE, eventId, eventType);

        auditService.record(mapping.getOrgId(), "RELEASE", release.getId(),
                "CREATE_VIA_WEBHOOK", UUID.fromString("00000000-0000-0000-0000-000000000000"),
                java.util.Map.of("source", "gitlab", "projectId", String.valueOf(gitlabProjectId),
                        "version", version, "eventId", eventId));

        log.info("Release created from GitLab webhook: productId={}, version={}, releaseId={}",
                mapping.getProductId(), version, release.getId());
    }
}
