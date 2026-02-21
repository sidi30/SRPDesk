package com.lexsecura.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    @Bean
    public Counter evidencesUploadedCounter(MeterRegistry registry) {
        return Counter.builder("evidences.uploaded")
                .description("Number of evidences uploaded")
                .register(registry);
    }
}
