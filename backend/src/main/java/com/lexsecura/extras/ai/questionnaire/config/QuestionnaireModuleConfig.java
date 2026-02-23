package com.lexsecura.extras.ai.questionnaire.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Activates the AI Questionnaire Parser module.
 * Enable with: app.extras.questionnaire.enabled=true
 */
@Configuration
@ConditionalOnProperty(name = "app.extras.questionnaire.enabled", havingValue = "true")
@ComponentScan(basePackages = {
        "com.lexsecura.extras.ai.questionnaire.controller",
        "com.lexsecura.extras.ai.questionnaire.service"
})
public class QuestionnaireModuleConfig {
}
