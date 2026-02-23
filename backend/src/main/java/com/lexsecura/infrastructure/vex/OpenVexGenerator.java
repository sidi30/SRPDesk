package com.lexsecura.infrastructure.vex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lexsecura.domain.model.Product;
import com.lexsecura.domain.model.Release;
import com.lexsecura.domain.model.vex.VexFormat;
import com.lexsecura.domain.model.vex.VexStatement;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Generates OpenVEX JSON documents (spec: https://github.com/openvex/spec).
 */
@Component
public class OpenVexGenerator implements VexDocumentGenerator {

    private final ObjectMapper objectMapper;

    public OpenVexGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public VexFormat supportedFormat() {
        return VexFormat.OPENVEX;
    }

    @Override
    public String generate(Product product, Release release, List<VexStatement> statements) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("@context", "https://openvex.dev/ns/v0.2.0");
            root.put("@id", "https://srpdesk.com/vex/" + UUID.randomUUID());
            root.put("author", "SRPDesk");
            root.put("role", "document_creator");
            root.put("timestamp", Instant.now().toString());
            root.put("version", 1);

            // Tooling
            root.put("tooling", "SRPDesk SBOM Ops Platform");

            ArrayNode statementsNode = root.putArray("statements");

            for (VexStatement stmt : statements) {
                ObjectNode stmtNode = objectMapper.createObjectNode();

                // Vulnerability
                ObjectNode vulnNode = stmtNode.putObject("vulnerability");
                vulnNode.put("name", stmt.getVulnerabilityId());

                // Products
                ArrayNode productsNode = stmtNode.putArray("products");
                ObjectNode prodNode = objectMapper.createObjectNode();
                prodNode.put("@id", "pkg:srpdesk/" + product.getName() + "@" + release.getVersion());
                productsNode.add(prodNode);

                stmtNode.put("status", stmt.getStatus().name());

                if (stmt.getJustification() != null) {
                    stmtNode.put("justification", stmt.getJustification().name());
                }
                if (stmt.getImpactStatement() != null) {
                    stmtNode.put("impact_statement", stmt.getImpactStatement());
                }
                if (stmt.getActionStatement() != null) {
                    stmtNode.put("action_statement", stmt.getActionStatement());
                }

                stmtNode.put("timestamp", Instant.now().toString());
                statementsNode.add(stmtNode);
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate OpenVEX document", e);
        }
    }
}
