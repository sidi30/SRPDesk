package com.lexsecura.extras.requirements.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Activates the CRA Requirements / Checklist module.
 * Enabled by default. Disable with: app.extras.requirements.enabled=false
 */
@Configuration
@ConditionalOnProperty(name = "app.extras.requirements.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = {
        "com.lexsecura.extras.requirements.controller",
        "com.lexsecura.extras.requirements.service"
})
public class RequirementsModuleConfig {
}
