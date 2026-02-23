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

    @Bean
    public Counter vexDocumentsGeneratedCounter(MeterRegistry registry) {
        return Counter.builder("vex.documents.generated")
                .description("Number of VEX documents generated")
                .register(registry);
    }

    @Bean
    public Counter vulnerabilitiesEnrichedCounter(MeterRegistry registry) {
        return Counter.builder("vulnerabilities.enriched")
                .description("Number of vulnerabilities enriched by monitoring")
                .register(registry);
    }

    @Bean
    public Counter activelyExploitedDetectedCounter(MeterRegistry registry) {
        return Counter.builder("vulnerabilities.actively_exploited.detected")
                .description("Number of actively exploited vulnerabilities detected")
                .register(registry);
    }

    @Bean
    public Counter sbomShareLinksCreatedCounter(MeterRegistry registry) {
        return Counter.builder("sbom.share_links.created")
                .description("Number of SBOM share links created")
                .register(registry);
    }

    @Bean
    public Counter sbomShareDownloadsCounter(MeterRegistry registry) {
        return Counter.builder("sbom.share_links.downloads")
                .description("Number of SBOM downloads via share links")
                .register(registry);
    }

    @Bean
    public Counter sbomQualityScoresCounter(MeterRegistry registry) {
        return Counter.builder("sbom.quality.scored")
                .description("Number of SBOM quality scores computed")
                .register(registry);
    }
}
