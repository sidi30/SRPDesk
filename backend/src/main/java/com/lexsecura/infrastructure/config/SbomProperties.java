package com.lexsecura.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.sbom")
public class SbomProperties {

    private int maxSizeMb = 10;

    public int getMaxSizeMb() { return maxSizeMb; }
    public void setMaxSizeMb(int maxSizeMb) { this.maxSizeMb = maxSizeMb; }

    public long getMaxSizeBytes() { return (long) maxSizeMb * 1024 * 1024; }
}
