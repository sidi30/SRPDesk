package com.lexsecura.infrastructure.ai;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Redacts PII and secrets from text before sending to the LLM.
 * Patterns: API keys, tokens, email addresses, IP addresses, JWTs, passwords.
 */
@Component
public class AiRedactor {

    private static final Pattern EMAIL = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern IP_V4 = Pattern.compile(
            "\\b(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\b");
    private static final Pattern JWT = Pattern.compile(
            "eyJ[A-Za-z0-9_-]{10,}\\.eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}");
    private static final Pattern API_KEY = Pattern.compile(
            "(?i)(?:api[_-]?key|token|secret|password|bearer|authorization)[\"']?\\s*[:=]\\s*[\"']?([A-Za-z0-9_\\-/+=]{16,})[\"']?");
    private static final Pattern GENERIC_SECRET = Pattern.compile(
            "(?i)(?:sk-|ghp_|gho_|glpat-|xoxb-|xoxp-)[A-Za-z0-9_\\-]{10,}");

    public String redact(String text) {
        if (text == null) return "";
        String result = text;
        result = JWT.matcher(result).replaceAll("[REDACTED_JWT]");
        result = GENERIC_SECRET.matcher(result).replaceAll("[REDACTED_SECRET]");
        result = API_KEY.matcher(result).replaceAll("[REDACTED_KEY]");
        result = EMAIL.matcher(result).replaceAll("[REDACTED_EMAIL]");
        result = IP_V4.matcher(result).replaceAll("[REDACTED_IP]");
        return result;
    }
}
