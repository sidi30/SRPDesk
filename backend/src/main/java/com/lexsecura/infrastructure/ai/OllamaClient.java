package com.lexsecura.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final double temperature;
    private final double topP;
    private final int numCtx;
    private final Timer latencyTimer;
    private final Counter failureCounter;
    private final Counter retryCounter;

    public OllamaClient(
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry,
            @Value("${app.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${app.ai.ollama.model:phi3.5}") String model,
            @Value("${app.ai.ollama.temperature:0.2}") double temperature,
            @Value("${app.ai.ollama.top-p:0.9}") double topP,
            @Value("${app.ai.ollama.num-ctx:4096}") int numCtx,
            @Value("${app.ai.ollama.timeout-seconds:120}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.model = model;
        this.temperature = temperature;
        this.topP = topP;
        this.numCtx = numCtx;

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        this.latencyTimer = Timer.builder("ai_job_latency")
                .description("Ollama generation latency")
                .register(meterRegistry);
        this.failureCounter = Counter.builder("ai_failures_total")
                .description("AI generation failures")
                .register(meterRegistry);
        this.retryCounter = Counter.builder("ai_retries_total")
                .description("AI generation retries")
                .register(meterRegistry);
    }

    public String generate(String systemPrompt, String userPrompt) {
        return latencyTimer.record(() -> {
            try {
                Map<String, Object> body = Map.of(
                        "model", model,
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt),
                                Map.of("role", "user", "content", userPrompt)
                        ),
                        "stream", false,
                        "options", Map.of(
                                "temperature", temperature,
                                "top_p", topP,
                                "num_ctx", numCtx
                        )
                );

                String responseBody = webClient.post()
                        .uri("/api/chat")
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                                .doBeforeRetry(signal -> {
                                    retryCounter.increment();
                                    log.warn("Retrying Ollama call, attempt {}", signal.totalRetries() + 1);
                                }))
                        .block(Duration.ofSeconds(180));

                JsonNode root = objectMapper.readTree(responseBody);
                return root.path("message").path("content").asText();
            } catch (Exception e) {
                failureCounter.increment();
                log.error("Ollama generation failed", e);
                throw new AiGenerationException("Ollama generation failed: " + e.getMessage(), e);
            }
        });
    }

    public String getModel() {
        return model;
    }

    public static class AiGenerationException extends RuntimeException {
        public AiGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
