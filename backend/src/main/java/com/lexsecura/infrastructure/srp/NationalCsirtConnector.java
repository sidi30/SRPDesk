package com.lexsecura.infrastructure.srp;

import com.lexsecura.application.port.CsirtConnector;
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

/**
 * Real CSIRT connector for parallel notification under CRA Art. 14.
 * Each EU member state designates one or more CSIRTs under NIS2 Directive (EU) 2022/2555.
 * This connector submits to the national CSIRT endpoint configured per country.
 */
@Component
@Primary
@ConditionalOnProperty(name = "app.csirt.enabled", havingValue = "true")
public class NationalCsirtConnector implements CsirtConnector {

    private static final Logger log = LoggerFactory.getLogger(NationalCsirtConnector.class);

    private final String baseUrl;
    private final String apiKey;
    private final int timeoutSeconds;

    public NationalCsirtConnector(
            @Value("${app.csirt.base-url}") String baseUrl,
            @Value("${app.csirt.api-key:}") String apiKey,
            @Value("${app.csirt.timeout-seconds:30}") int timeoutSeconds) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public String submit(SrpSubmission submission, byte[] bundleZip, String countryCode) {
        try {
            log.info("Submitting to national CSIRT ({}): type={}, eventId={}, size={}",
                    countryCode, submission.getSubmissionType(), submission.getCraEventId(), bundleZip.length);

            WebClient client = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .build();

            Map<?, ?> response = client.post()
                    .uri("/api/v1/notifications")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("X-Submission-Type", submission.getSubmissionType())
                    .header("X-Schema-Version", submission.getSchemaVersion())
                    .header("X-Country-Code", countryCode)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .bodyValue(bundleZip)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response != null && response.containsKey("reference")) {
                String reference = response.get("reference").toString();
                log.info("CSIRT ({}) notification accepted: reference={}", countryCode, reference);
                return reference;
            }

            log.warn("CSIRT ({}) response missing reference: {}", countryCode, response);
            return null;
        } catch (Exception e) {
            log.error("CSIRT ({}) notification failed: {}", countryCode, e.getMessage(), e);
            throw new RuntimeException("CSIRT notification failed for " + countryCode + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
