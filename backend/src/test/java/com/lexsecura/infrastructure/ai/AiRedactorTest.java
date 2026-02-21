package com.lexsecura.infrastructure.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiRedactorTest {

    private AiRedactor redactor;

    @BeforeEach
    void setUp() {
        redactor = new AiRedactor();
    }

    // ── Null / Empty ──────────────────────────────────────

    @Test
    void redact_null_returnsEmpty() {
        assertEquals("", redactor.redact(null));
    }

    @Test
    void redact_emptyString_returnsEmpty() {
        assertEquals("", redactor.redact(""));
    }

    @Test
    void redact_noSensitiveData_returnsUnchanged() {
        String clean = "This is a normal description of a vulnerability.";
        assertEquals(clean, redactor.redact(clean));
    }

    // ── Email ─────────────────────────────────────────────

    @Test
    void redact_email_isRedacted() {
        String input = "Contact admin@lexsecura.com for details.";
        String result = redactor.redact(input);
        assertFalse(result.contains("admin@lexsecura.com"));
        assertTrue(result.contains("[REDACTED_EMAIL]"));
    }

    @Test
    void redact_multipleEmails_allRedacted() {
        String input = "Send to user1@example.com and user2@corp.io";
        String result = redactor.redact(input);
        assertFalse(result.contains("user1@example.com"));
        assertFalse(result.contains("user2@corp.io"));
        assertEquals(2, countOccurrences(result, "[REDACTED_EMAIL]"));
    }

    // ── IPv4 ──────────────────────────────────────────────

    @Test
    void redact_ipv4_isRedacted() {
        String input = "Server at 192.168.1.100 was compromised.";
        String result = redactor.redact(input);
        assertFalse(result.contains("192.168.1.100"));
        assertTrue(result.contains("[REDACTED_IP]"));
    }

    @Test
    void redact_multipleIps_allRedacted() {
        String input = "Hosts 10.0.0.1 and 172.16.0.254 affected.";
        String result = redactor.redact(input);
        assertFalse(result.contains("10.0.0.1"));
        assertFalse(result.contains("172.16.0.254"));
    }

    // ── JWT ───────────────────────────────────────────────

    @Test
    void redact_jwt_isRedacted() {
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String input = "Token: " + jwt;
        String result = redactor.redact(input);
        assertFalse(result.contains("eyJ"));
        assertTrue(result.contains("[REDACTED_JWT]"));
    }

    // ── API Key / Token ───────────────────────────────────

    @Test
    void redact_apiKey_isRedacted() {
        String input = "api_key=sk_live_12345678901234567890";
        String result = redactor.redact(input);
        assertFalse(result.contains("sk_live_12345678901234567890"));
        assertTrue(result.contains("[REDACTED_KEY]") || result.contains("[REDACTED_SECRET]"));
    }

    @Test
    void redact_bearerTokenHeader_isRedacted() {
        String input = "Authorization: \"ABCDEFGHIJKLMNOP1234\"";
        String result = redactor.redact(input);
        assertFalse(result.contains("ABCDEFGHIJKLMNOP1234"));
    }

    @Test
    void redact_passwordField_isRedacted() {
        String input = "password = \"SuperSecret12345678\"";
        String result = redactor.redact(input);
        assertFalse(result.contains("SuperSecret12345678"));
    }

    // ── Generic Secrets ───────────────────────────────────

    @Test
    void redact_githubPat_isRedacted() {
        String input = "Token: ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdef01";
        String result = redactor.redact(input);
        assertFalse(result.contains("ghp_"));
        assertTrue(result.contains("[REDACTED_SECRET]"));
    }

    @Test
    void redact_gitlabToken_isRedacted() {
        String input = "gitlab: glpat-abcdefghijklmnopqrst";
        String result = redactor.redact(input);
        assertFalse(result.contains("glpat-"));
        assertTrue(result.contains("[REDACTED_SECRET]"));
    }

    @Test
    void redact_slackToken_isRedacted() {
        String input = "slack_token=xoxb-1234567890-abcdefgh";
        String result = redactor.redact(input);
        assertFalse(result.contains("xoxb-"));
    }

    @Test
    void redact_skPrefix_isRedacted() {
        String input = "key: sk-abcdefghijklmnopqrst";
        String result = redactor.redact(input);
        assertFalse(result.contains("sk-abcdefghijklmnopqrst"));
        assertTrue(result.contains("[REDACTED_SECRET]"));
    }

    // ── Mixed content ─────────────────────────────────────

    @Test
    void redact_mixedContent_allRedacted() {
        String input = """
                Server 10.0.0.1 contacted by admin@evil.com.
                JWT found: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxMDAwfQ.ABC123DEF456GHI789JKL012MNO345P
                api_key = "ABCDEFGHIJKLMNOP1234"
                ghp_TestToken1234567890abcdefghij
                """;
        String result = redactor.redact(input);
        assertFalse(result.contains("10.0.0.1"));
        assertFalse(result.contains("admin@evil.com"));
        assertFalse(result.contains("eyJ"));
        assertFalse(result.contains("ghp_"));
    }

    // ── Helpers ───────────────────────────────────────────

    private int countOccurrences(String text, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
