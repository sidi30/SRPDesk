package com.lexsecura.infrastructure.srp;

import com.lexsecura.application.port.SrpConnector;
import com.lexsecura.domain.model.SrpSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Component
@Primary
@ConditionalOnProperty(name = "app.enisa.enabled", havingValue = "true")
public class EnisaSrpConnector implements SrpConnector {

    private static final Logger log = LoggerFactory.getLogger(EnisaSrpConnector.class);

    private final WebClient webClient;
    private final String apiKey;
    private final int timeoutSeconds;

    public EnisaSrpConnector(
            @Value("${app.enisa.base-url}") String baseUrl,
            @Value("${app.enisa.api-key:}") String apiKey,
            @Value("${app.enisa.timeout-seconds:30}") int timeoutSeconds) {
        this.apiKey = apiKey;
        this.timeoutSeconds = timeoutSeconds;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .build();
    }

    @Override
    public String submit(SrpSubmission submission, byte[] bundleZip) {
        try {
            log.info("Submitting to ENISA SRP: type={}, eventId={}, size={}",
                    submission.getSubmissionType(), submission.getCraEventId(), bundleZip.length);

            Map<?, ?> response = webClient.post()
                    .uri("/api/v1/submissions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("X-Submission-Type", submission.getSubmissionType())
                    .header("X-Schema-Version", submission.getSchemaVersion())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .bodyValue(bundleZip)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response != null && response.containsKey("reference")) {
                String reference = response.get("reference").toString();
                log.info("ENISA SRP submission accepted: reference={}", reference);
                return reference;
            }

            log.warn("ENISA SRP response missing reference: {}", response);
            return null;
        } catch (Exception e) {
            log.error("ENISA SRP submission failed: {}", e.getMessage(), e);
            throw new RuntimeException("ENISA SRP submission failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
