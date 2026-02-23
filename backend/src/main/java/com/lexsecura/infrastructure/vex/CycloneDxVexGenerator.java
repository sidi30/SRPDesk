package com.lexsecura.infrastructure.vex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.model.vex.VexFormat;
import com.lexsecura.domain.model.vex.VexStatement;
import com.lexsecura.domain.model.vex.VexStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generates CycloneDX 1.6+ VEX documents (BOM with vulnerabilities[].analysis).
 */
@Component
public class CycloneDxVexGenerator implements VexDocumentGenerator {

    private static final Map<VexStatus, String> CDX_STATE_MAP = Map.of(
            VexStatus.not_affected, "not_affected",
            VexStatus.affected, "exploitable",
            VexStatus.fixed, "resolved",
            VexStatus.under_investigation, "in_triage"
    );

    private final ObjectMapper objectMapper;

    public CycloneDxVexGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public VexFormat supportedFormat() {
        return VexFormat.CYCLONEDX_VEX;
    }

    @Override
    public String generate(Product product, Release release, List<VexStatement> statements) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("bomFormat", "CycloneDX");
            root.put("specVersion", "1.6");
            root.put("serialNumber", "urn:uuid:" + UUID.randomUUID());
            root.put("version", 1);

            // Metadata
            ObjectNode metadata = root.putObject("metadata");
            metadata.put("timestamp", Instant.now().toString());
            ObjectNode tool = metadata.putObject("tools");
            ArrayNode components = tool.putArray("components");
            ObjectNode srpdesk = objectMapper.createObjectNode();
            srpdesk.put("type", "application");
            srpdesk.put("name", "SRPDesk");
            srpdesk.put("version", "1.0.0");
            components.add(srpdesk);

            ObjectNode component = metadata.putObject("component");
            component.put("type", "application");
            component.put("name", product.getName());
            component.put("version", release.getVersion());
            component.put("bom-ref", product.getName() + "@" + release.getVersion());

            // Vulnerabilities
            ArrayNode vulns = root.putArray("vulnerabilities");

            for (VexStatement stmt : statements) {
                ObjectNode vulnNode = objectMapper.createObjectNode();
                vulnNode.put("id", stmt.getVulnerabilityId());

                ArrayNode affects = vulnNode.putArray("affects");
                ObjectNode target = objectMapper.createObjectNode();
                target.put("ref", product.getName() + "@" + release.getVersion());
                affects.add(target);

                ObjectNode analysis = vulnNode.putObject("analysis");
                analysis.put("state", CDX_STATE_MAP.getOrDefault(stmt.getStatus(), "in_triage"));

                if (stmt.getJustification() != null) {
                    analysis.put("justification", stmt.getJustification().name());
                }
                if (stmt.getImpactStatement() != null || stmt.getActionStatement() != null) {
                    String detail = (stmt.getImpactStatement() != null ? stmt.getImpactStatement() : "")
                            + (stmt.getActionStatement() != null ? "\n" + stmt.getActionStatement() : "");
                    analysis.put("detail", detail.trim());
                }

                vulns.add(vulnNode);
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CycloneDX VEX document", e);
        }
    }
}
