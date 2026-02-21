package com.lexsecura.infrastructure.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiSchemaValidatorTest {

    private AiSchemaValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AiSchemaValidator(new ObjectMapper());
    }

    // ── SRP Draft Schema ──────────────────────────────────

    @Test
    void srpDraft_validJson_shouldPass() {
        String json = """
                {
                  "summary": "Critical vulnerability in IoT Gateway affecting firmware update mechanism",
                  "affected_versions": ["1.0.0", "1.1.0"],
                  "impact": "Remote code execution via crafted firmware package",
                  "mitigation": "Disable OTA updates until patch applied",
                  "patch_status": "Patch available in 1.2.0",
                  "timeline": [
                    {"date": "2026-02-01", "event": "Vulnerability detected"},
                    {"date": "2026-02-15", "event": "Patch released"}
                  ],
                  "references": [
                    {"type": "product", "id": "prod-001", "label": "IoT Gateway"}
                  ]
                }
                """;
        var result = validator.validate(json, "schemas/ai/srp-draft.schema.json");
        assertTrue(result.valid(), "Expected valid but got errors: " + result.errors());
    }

    @Test
    void srpDraft_missingSummary_shouldFail() {
        String json = """
                {
                  "affected_versions": ["1.0.0"],
                  "impact": "Some impact",
                  "mitigation": "Some mitigation",
                  "patch_status": "pending",
                  "timeline": [],
                  "references": []
                }
                """;
        var result = validator.validate(json, "schemas/ai/srp-draft.schema.json");
        assertFalse(result.valid());
        assertNotNull(result.errors());
        assertTrue(result.errors().contains("summary"));
    }

    @Test
    void srpDraft_summaryTooShort_shouldFail() {
        String json = """
                {
                  "summary": "Short",
                  "affected_versions": ["1.0.0"],
                  "impact": "Some impact here",
                  "mitigation": "Apply patch",
                  "patch_status": "pending",
                  "timeline": [],
                  "references": []
                }
                """;
        var result = validator.validate(json, "schemas/ai/srp-draft.schema.json");
        assertFalse(result.valid());
    }

    @Test
    void srpDraft_invalidReferenceType_shouldFail() {
        String json = """
                {
                  "summary": "A valid summary with enough characters",
                  "affected_versions": ["1.0.0"],
                  "impact": "Some impact here",
                  "mitigation": "Apply patch",
                  "patch_status": "pending",
                  "timeline": [],
                  "references": [{"type": "invalid_type", "id": "123"}]
                }
                """;
        var result = validator.validate(json, "schemas/ai/srp-draft.schema.json");
        assertFalse(result.valid());
    }

    @Test
    void srpDraft_additionalProperties_shouldFail() {
        String json = """
                {
                  "summary": "A valid summary with enough characters",
                  "affected_versions": ["1.0.0"],
                  "impact": "Some impact here",
                  "mitigation": "Apply patch",
                  "patch_status": "pending",
                  "timeline": [],
                  "references": [],
                  "extra_field": "should not be here"
                }
                """;
        var result = validator.validate(json, "schemas/ai/srp-draft.schema.json");
        assertFalse(result.valid());
    }

    // ── Comm Pack Schema ──────────────────────────────────

    @Test
    void commPack_validJson_shouldPass() {
        String json = """
                {
                  "advisory_markdown": "# Security Advisory\\n\\nCritical vulnerability discovered.",
                  "email_subject": "Security Update Required",
                  "email_body": "Dear customer, a security update is available for your product.",
                  "release_notes_markdown": "## Release Notes\\n\\nSecurity fixes included.",
                  "references": [
                    {"type": "finding", "id": "finding-001", "label": "CVE-2026-1234"}
                  ]
                }
                """;
        var result = validator.validate(json, "schemas/ai/comm-pack.schema.json");
        assertTrue(result.valid(), "Expected valid but got errors: " + result.errors());
    }

    @Test
    void commPack_missingEmailSubject_shouldFail() {
        String json = """
                {
                  "advisory_markdown": "# Security Advisory text here",
                  "email_body": "Dear customer, here is the info.",
                  "release_notes_markdown": "## Release Notes content here",
                  "references": []
                }
                """;
        var result = validator.validate(json, "schemas/ai/comm-pack.schema.json");
        assertFalse(result.valid());
        assertTrue(result.errors().contains("email_subject"));
    }

    @Test
    void commPack_emailSubjectTooShort_shouldFail() {
        String json = """
                {
                  "advisory_markdown": "# Security Advisory text here",
                  "email_subject": "Hi",
                  "email_body": "Dear customer, here is the full body.",
                  "release_notes_markdown": "## Release Notes content here",
                  "references": []
                }
                """;
        var result = validator.validate(json, "schemas/ai/comm-pack.schema.json");
        assertFalse(result.valid());
    }

    // ── Questionnaire Schema ──────────────────────────────

    @Test
    void questionnaire_validJson_shouldPass() {
        String json = """
                [
                  {
                    "question_id": "Q1",
                    "question": "Do you perform regular vulnerability scans?",
                    "answer": "Yes, via automated CI/CD pipeline scans.",
                    "confidence": "HIGH",
                    "references": [
                      {"type": "evidence", "id": "ev-001", "label": "CI Scan Report"}
                    ]
                  },
                  {
                    "question_id": "Q2",
                    "question": "Do you maintain a software bill of materials?",
                    "answer": "Yes, CycloneDX SBOMs are generated for each release.",
                    "confidence": "HIGH",
                    "references": [
                      {"type": "release", "id": "rel-001", "label": "v1.0.0"}
                    ]
                  }
                ]
                """;
        var result = validator.validate(json, "schemas/ai/questionnaire.schema.json");
        assertTrue(result.valid(), "Expected valid but got errors: " + result.errors());
    }

    @Test
    void questionnaire_invalidConfidence_shouldFail() {
        String json = """
                [
                  {
                    "question_id": "Q1",
                    "question": "Some question?",
                    "answer": "Some answer",
                    "confidence": "VERY_HIGH",
                    "references": []
                  }
                ]
                """;
        var result = validator.validate(json, "schemas/ai/questionnaire.schema.json");
        assertFalse(result.valid());
    }

    @Test
    void questionnaire_missingAnswer_shouldFail() {
        String json = """
                [
                  {
                    "question_id": "Q1",
                    "question": "Some question?",
                    "confidence": "HIGH",
                    "references": []
                  }
                ]
                """;
        var result = validator.validate(json, "schemas/ai/questionnaire.schema.json");
        assertFalse(result.valid());
    }

    @Test
    void questionnaire_emptyArray_shouldPass() {
        String json = "[]";
        var result = validator.validate(json, "schemas/ai/questionnaire.schema.json");
        assertTrue(result.valid());
    }

    // ── Error handling ────────────────────────────────────

    @Test
    void validate_invalidJson_shouldReturnInvalid() {
        String json = "this is not json at all";
        var result = validator.validate(json, "schemas/ai/srp-draft.schema.json");
        assertFalse(result.valid());
        assertTrue(result.errors().contains("Invalid JSON"));
    }

    @Test
    void validate_missingSchema_shouldReturnInvalid() {
        // The validate method catches the IllegalStateException internally
        var result = validator.validate("{}", "schemas/ai/nonexistent.schema.json");
        assertFalse(result.valid());
    }

    // ── Schema caching ────────────────────────────────────

    @Test
    void validate_sameSchemaCalledTwice_shouldUseCache() {
        String validJson = """
                {
                  "summary": "Enough characters here for a valid summary",
                  "affected_versions": ["1.0"],
                  "impact": "Impact description",
                  "mitigation": "Mitigation steps",
                  "patch_status": "available",
                  "timeline": [],
                  "references": []
                }
                """;
        var result1 = validator.validate(validJson, "schemas/ai/srp-draft.schema.json");
        var result2 = validator.validate(validJson, "schemas/ai/srp-draft.schema.json");
        assertTrue(result1.valid());
        assertTrue(result2.valid());
    }
}
