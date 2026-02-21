package com.lexsecura.infrastructure.osv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

@Component
public class OsvClient {

    private static final Logger log = LoggerFactory.getLogger(OsvClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final long rateLimitMs;
    private final Map<String, CacheEntry> cache = new LinkedHashMap<>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            return size() > 500 || (System.currentTimeMillis() - eldest.getValue().timestamp > 3600000);
        }
    };
    private long lastRequestTime = 0;

    public OsvClient(ObjectMapper objectMapper,
                     @Value("${app.osv.base-url:https://api.osv.dev}") String baseUrl,
                     @Value("${app.osv.rate-limit-ms:1000}") long rateLimitMs) {
        this.objectMapper = objectMapper;
        this.rateLimitMs = rateLimitMs;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public List<JsonNode> queryVulnerabilities(String purl) {
        CacheEntry cached = cache.get(purl);
        if (cached != null && System.currentTimeMillis() - cached.timestamp < 3600000) {
            return cached.vulns;
        }

        rateLimit();

        try {
            String requestBody = objectMapper.writeValueAsString(
                    Map.of("package", Map.of("purl", purl)));

            String response = webClient.post()
                    .uri("/v1/query")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .onErrorReturn("{}")
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode vulns = root.path("vulns");

            List<JsonNode> result = new ArrayList<>();
            if (vulns.isArray()) {
                vulns.forEach(result::add);
            }

            cache.put(purl, new CacheEntry(result, System.currentTimeMillis()));
            log.debug("OSV query for {}: {} vulnerabilities", purl, result.size());
            return result;

        } catch (Exception e) {
            log.warn("OSV query failed for {}: {}", purl, e.getMessage());
            return List.of();
        }
    }

    private synchronized void rateLimit() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        if (elapsed < rateLimitMs) {
            try {
                Thread.sleep(rateLimitMs - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    private record CacheEntry(List<JsonNode> vulns, long timestamp) {}
}
