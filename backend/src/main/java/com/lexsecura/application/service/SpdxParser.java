package com.lexsecura.application.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * SPDX 2.3 (ISO/IEC 5962:2021) parser.
 * Extracts packages from SPDX JSON documents, handling NOASSERTION/NONE values,
 * license fallback (concludedLicense -> declaredLicense), supplier prefix stripping,
 * and PURL extraction from externalRefs.
 */
public final class SpdxParser {

    private SpdxParser() {}

    /**
     * Parsed SPDX package data (before persistence).
     */
    public record ParsedPackage(
            String name,
            String version,
            String purl,
            String license,
            String supplier
    ) {}

    /**
     * Checks if a JSON document is a valid SPDX format.
     */
    public static boolean isSpdx(JsonNode root) {
        return root.has("spdxVersion")
                && root.path("spdxVersion").asText("").startsWith("SPDX-");
    }

    /**
     * Parses an SPDX JSON document and returns the list of parsed packages.
     * Skips SPDXRef-DOCUMENT entries. Handles NOASSERTION/NONE as null.
     */
    public static List<ParsedPackage> parse(JsonNode root) {
        JsonNode packagesNode = root.path("packages");
        if (!packagesNode.isArray()) {
            throw new IllegalArgumentException("SPDX SBOM has no 'packages' array");
        }

        List<ParsedPackage> result = new ArrayList<>();
        for (JsonNode pkg : packagesNode) {
            // Skip SPDXRef-DOCUMENT — it represents the document itself, not a software component
            String spdxId = pkg.path("SPDXID").asText("");
            if ("SPDXRef-DOCUMENT".equals(spdxId)) {
                continue;
            }

            String name = pkg.path("name").asText("");
            String version = nullable(pkg.path("versionInfo").asText(null));

            // Extract license: concludedLicense -> declaredLicense fallback
            String license = nullable(pkg.path("licenseConcluded").asText(null));
            if (license == null) {
                license = nullable(pkg.path("licenseDeclared").asText(null));
            }

            // Extract supplier — strip "Organization: " prefix
            String supplier = nullable(pkg.path("supplier").asText(null));
            if (supplier != null && supplier.startsWith("Organization: ")) {
                supplier = supplier.substring("Organization: ".length());
            }

            // Extract PURL from externalRefs (PACKAGE-MANAGER / purl)
            String purl = extractPurl(pkg);
            if (purl == null || purl.isBlank()) {
                purl = "pkg:generic/" + name + (version != null ? "@" + version : "");
            }

            result.add(new ParsedPackage(name, version, purl, license, supplier));
        }
        return result;
    }

    private static String extractPurl(JsonNode pkg) {
        JsonNode externalRefs = pkg.path("externalRefs");
        if (externalRefs.isArray()) {
            for (JsonNode ref : externalRefs) {
                String cat = ref.path("referenceCategory").asText("");
                if ("PACKAGE-MANAGER".equals(cat) || "PACKAGE_MANAGER".equals(cat)) {
                    if ("purl".equals(ref.path("referenceType").asText(""))) {
                        return ref.path("referenceLocator").asText(null);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Treats SPDX NOASSERTION and NONE as null (absent values).
     */
    static String nullable(String value) {
        if (value == null || "NOASSERTION".equals(value) || "NONE".equals(value)) {
            return null;
        }
        return value;
    }
}
