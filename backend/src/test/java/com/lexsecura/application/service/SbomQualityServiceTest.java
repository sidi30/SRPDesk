package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexsecura.application.dto.SbomQualityScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SbomQualityServiceTest {

    private SbomQualityService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Service's evaluate() method is package-private and doesn't need the repos
        service = new SbomQualityService(null, null, objectMapper);
    }

    @Test
    void evaluate_perfectCycloneDx_scoresHigh() throws Exception {
        String sbom = """
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.5",
                  "metadata": {
                    "timestamp": "2026-02-23T00:00:00Z",
                    "supplier": { "name": "Acme Corp" }
                  },
                  "components": [
                    {
                      "type": "library",
                      "name": "lodash",
                      "version": "4.17.21",
                      "purl": "pkg:npm/lodash@4.17.21",
                      "licenses": [{ "license": { "id": "MIT" } }],
                      "hashes": [{ "alg": "SHA-256", "content": "abc123" }]
                    }
                  ],
                  "dependencies": [
                    { "ref": "lodash", "dependsOn": [] }
                  ]
                }
                """;
        JsonNode root = objectMapper.readTree(sbom);
        SbomQualityScoreResponse result = service.evaluate(root);

        assertEquals(100, result.totalScore());
        assertEquals("A", result.grade());
        assertEquals(1, result.componentCount());
        assertEquals(8, result.criteria().size());
    }

    @Test
    void evaluate_minimalCycloneDx_scoresLow() throws Exception {
        String sbom = """
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.5",
                  "metadata": {},
                  "components": [
                    { "type": "library", "name": "foo" }
                  ]
                }
                """;
        JsonNode root = objectMapper.readTree(sbom);
        SbomQualityScoreResponse result = service.evaluate(root);

        assertTrue(result.totalScore() < 50);
        assertEquals(1, result.componentCount());
        // Should have name but no version, purl, license, hash, timestamp, author
        var names = result.criteria().stream()
                .filter(c -> "NAMES".equals(c.id()))
                .findFirst().orElseThrow();
        assertEquals(15, names.score());

        var versions = result.criteria().stream()
                .filter(c -> "VERSIONS".equals(c.id()))
                .findFirst().orElseThrow();
        assertEquals(0, versions.score());
    }

    @Test
    void evaluate_spdxFormat_worksCorrectly() throws Exception {
        String sbom = """
                {
                  "spdxVersion": "SPDX-2.3",
                  "creationInfo": {
                    "created": "2026-02-23T00:00:00Z",
                    "creators": ["Tool: syft"]
                  },
                  "packages": [
                    {
                      "name": "express",
                      "versionInfo": "4.18.2",
                      "licenseDeclared": "MIT",
                      "externalRefs": [
                        { "referenceCategory": "PACKAGE_MANAGER", "referenceType": "purl", "referenceLocator": "pkg:npm/express@4.18.2" }
                      ]
                    }
                  ],
                  "relationships": [
                    { "spdxElementId": "SPDXRef-DOCUMENT", "relatedSpdxElement": "SPDXRef-express", "relationshipType": "DESCRIBES" }
                  ]
                }
                """;
        JsonNode root = objectMapper.readTree(sbom);
        SbomQualityScoreResponse result = service.evaluate(root);

        assertTrue(result.totalScore() >= 75);
        assertEquals("A", result.grade());
        assertEquals(1, result.componentCount());
    }

    @Test
    void evaluate_emptyComponents_returnsZero() throws Exception {
        String sbom = """
                {
                  "bomFormat": "CycloneDX",
                  "components": []
                }
                """;
        JsonNode root = objectMapper.readTree(sbom);
        SbomQualityScoreResponse result = service.evaluate(root);

        assertEquals(0, result.totalScore());
        assertEquals("F", result.grade());
        assertEquals(0, result.componentCount());
    }

    @Test
    void evaluate_unknownFormat_returnsZero() throws Exception {
        String sbom = """
                {
                  "format": "unknown"
                }
                """;
        JsonNode root = objectMapper.readTree(sbom);
        SbomQualityScoreResponse result = service.evaluate(root);

        assertEquals(0, result.totalScore());
        assertEquals("F", result.grade());
    }

    @Test
    void evaluate_gradeThresholds() throws Exception {
        // 90+ = A, 75+ = B, 60+ = C, 40+ = D, <40 = F
        // This test checks coverage percentage effects on scoring
        String sbom = """
                {
                  "bomFormat": "CycloneDX",
                  "specVersion": "1.5",
                  "metadata": {
                    "timestamp": "2026-02-23T00:00:00Z",
                    "supplier": { "name": "Test" }
                  },
                  "components": [
                    { "type": "library", "name": "a", "version": "1.0", "purl": "pkg:npm/a@1.0" },
                    { "type": "library", "name": "b" }
                  ],
                  "dependencies": []
                }
                """;
        JsonNode root = objectMapper.readTree(sbom);
        SbomQualityScoreResponse result = service.evaluate(root);

        // Has: author(15) + names(15) + versions(~8) + purl(~8) + deps(10) + timestamp(10) = ~66
        assertTrue(result.totalScore() >= 50);
        assertEquals(2, result.componentCount());
    }
}
