package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ci_upload_events")
public class CiUploadEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "release_id")
    private UUID releaseId;

    @Column(name = "component_count")
    private int componentCount;

    @Column(name = "new_components")
    private int newComponents;

    @Column(name = "removed_components")
    private int removedComponents;

    @Column(name = "quality_score")
    private int qualityScore;

    @Column(name = "quality_grade", length = 2)
    private String qualityGrade;

    @Column(name = "vuln_critical")
    private int vulnCritical;

    @Column(name = "vuln_high")
    private int vulnHigh;

    @Column(name = "vuln_medium")
    private int vulnMedium;

    @Column(name = "vuln_low")
    private int vulnLow;

    @Column(name = "vuln_total")
    private int vulnTotal;

    @Column(name = "new_vulnerabilities")
    private int newVulnerabilities;

    @Column(name = "policy_result", length = 10)
    private String policyResult;

    @Column(name = "git_ref")
    private String gitRef;

    @Column(name = "sha256")
    private String sha256;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public UUID getReleaseId() { return releaseId; }
    public void setReleaseId(UUID releaseId) { this.releaseId = releaseId; }
    public int getComponentCount() { return componentCount; }
    public void setComponentCount(int componentCount) { this.componentCount = componentCount; }
    public int getNewComponents() { return newComponents; }
    public void setNewComponents(int newComponents) { this.newComponents = newComponents; }
    public int getRemovedComponents() { return removedComponents; }
    public void setRemovedComponents(int removedComponents) { this.removedComponents = removedComponents; }
    public int getQualityScore() { return qualityScore; }
    public void setQualityScore(int qualityScore) { this.qualityScore = qualityScore; }
    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }
    public int getVulnCritical() { return vulnCritical; }
    public void setVulnCritical(int vulnCritical) { this.vulnCritical = vulnCritical; }
    public int getVulnHigh() { return vulnHigh; }
    public void setVulnHigh(int vulnHigh) { this.vulnHigh = vulnHigh; }
    public int getVulnMedium() { return vulnMedium; }
    public void setVulnMedium(int vulnMedium) { this.vulnMedium = vulnMedium; }
    public int getVulnLow() { return vulnLow; }
    public void setVulnLow(int vulnLow) { this.vulnLow = vulnLow; }
    public int getVulnTotal() { return vulnTotal; }
    public void setVulnTotal(int vulnTotal) { this.vulnTotal = vulnTotal; }
    public int getNewVulnerabilities() { return newVulnerabilities; }
    public void setNewVulnerabilities(int newVulnerabilities) { this.newVulnerabilities = newVulnerabilities; }
    public String getPolicyResult() { return policyResult; }
    public void setPolicyResult(String policyResult) { this.policyResult = policyResult; }
    public String getGitRef() { return gitRef; }
    public void setGitRef(String gitRef) { this.gitRef = gitRef; }
    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
