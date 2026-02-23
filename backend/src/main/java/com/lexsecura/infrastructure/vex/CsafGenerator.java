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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates CSAF 2.0 (Common Security Advisory Framework) documents.
 * Spec: https://docs.oasis-open.org/csaf/csaf/v2.0/csaf-v2.0.html
 */
@Component
public class CsafGenerator implements VexDocumentGenerator {

    private static final Map<VexStatus, String> CSAF_STATUS_MAP = Map.of(
            VexStatus.not_affected, "known_not_affected",
            VexStatus.affected, "known_affected",
            VexStatus.fixed, "fixed",
            VexStatus.under_investigation, "under_investigation"
    );

    private final ObjectMapper objectMapper;

    public CsafGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public VexFormat supportedFormat() {
        return VexFormat.CSAF;
    }

    @Override
    public String generate(Product product, Release release, List<VexStatement> statements) {
        try {
            String now = Instant.now().atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            ObjectNode root = objectMapper.createObjectNode();

            // Document metadata
            ObjectNode document = root.putObject("document");
            document.put("category", "csaf_vex");
            document.put("csaf_version", "2.0");
            document.put("title", "VEX for " + product.getName() + " " + release.getVersion());

            ObjectNode distribution = document.putObject("distribution");
            distribution.put("text", "Distribution authorized per CRA Article 14");

            ObjectNode publisher = document.putObject("publisher");
            publisher.put("category", "vendor");
            publisher.put("name", "SRPDesk");
            publisher.put("namespace", "https://srpdesk.com");

            ObjectNode tracking = document.putObject("tracking");
            tracking.put("current_release_date", now);
            tracking.put("id", "CSAF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            tracking.put("initial_release_date", now);

            ObjectNode revision = objectMapper.createObjectNode();
            revision.put("date", now);
            revision.put("number", "1");
            revision.put("summary", "Initial VEX document");
            ArrayNode revisionHistory = tracking.putArray("revision_history");
            revisionHistory.add(revision);
            tracking.put("status", "draft");
            tracking.put("version", "1");

            // Product tree
            ObjectNode productTree = root.putObject("product_tree");
            ArrayNode branches = productTree.putArray("branches");
            ObjectNode branch = objectMapper.createObjectNode();
            branch.put("category", "vendor");
            branch.put("name", "SRPDesk");

            ArrayNode subBranches = branch.putArray("branches");
            ObjectNode productBranch = objectMapper.createObjectNode();
            productBranch.put("category", "product_name");
            productBranch.put("name", product.getName());

            ArrayNode versionBranches = productBranch.putArray("branches");
            ObjectNode versionBranch = objectMapper.createObjectNode();
            versionBranch.put("category", "product_version");
            versionBranch.put("name", release.getVersion());

            ObjectNode productNode = versionBranch.putObject("product");
            String productFullName = product.getName() + " " + release.getVersion();
            String productRefId = "CSAFPID-" + product.getId().toString().substring(0, 8);
            productNode.put("name", productFullName);
            productNode.put("product_id", productRefId);

            versionBranches.add(versionBranch);
            subBranches.add(productBranch);
            branches.add(branch);

            // Vulnerabilities
            ArrayNode vulns = root.putArray("vulnerabilities");

            for (VexStatement stmt : statements) {
                ObjectNode vulnNode = objectMapper.createObjectNode();
                vulnNode.put("cve", stmt.getVulnerabilityId());

                ArrayNode productStatus = vulnNode.putArray("product_status");
                // CSAF uses product_status as an object, not array — fix
                ObjectNode status = vulnNode.putObject("product_status");
                String csafStatus = CSAF_STATUS_MAP.getOrDefault(stmt.getStatus(), "under_investigation");
                ArrayNode statusProducts = status.putArray(csafStatus);
                statusProducts.add(productRefId);

                // Threats
                if (stmt.getImpactStatement() != null) {
                    ArrayNode threats = vulnNode.putArray("threats");
                    ObjectNode threat = objectMapper.createObjectNode();
                    threat.put("category", "impact");
                    threat.put("details", stmt.getImpactStatement());
                    threats.add(threat);
                }

                // Remediations
                if (stmt.getActionStatement() != null) {
                    ArrayNode remediations = vulnNode.putArray("remediations");
                    ObjectNode remediation = objectMapper.createObjectNode();
                    remediation.put("category", stmt.getStatus() == VexStatus.fixed ? "vendor_fix" : "workaround");
                    remediation.put("details", stmt.getActionStatement());
                    ArrayNode remProducts = remediation.putArray("product_ids");
                    remProducts.add(productRefId);
                    remediations.add(remediation);
                }

                vulns.add(vulnNode);
            }
            // Remove the erroneous product_status array we created
            // (it was overridden by the object version)

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSAF 2.0 document", e);
        }
    }
}
