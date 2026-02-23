package com.lexsecura.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.api-key")
public class ApiKeyProperties {

    private String prefix = "srpd_";
    private int randomBytes = 20;
    private String defaultScopes = "ci:sbom";

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public int getRandomBytes() { return randomBytes; }
    public void setRandomBytes(int randomBytes) { this.randomBytes = randomBytes; }

    public String getDefaultScopes() { return defaultScopes; }
    public void setDefaultScopes(String defaultScopes) { this.defaultScopes = defaultScopes; }
}
