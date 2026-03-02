package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

public class CiUploadEvent {

    private UUID id;
    private UUID orgId;
    private UUID productId;
    private UUID releaseId;
    private int componentCount;
    private int newComponents;
    private int removedComponents;
    private int qualityScore;
    private String qualityGrade;
    private int vulnCritical;
    private int vulnHigh;
    private int vulnMedium;
    private int vulnLow;
    private int vulnTotal;
    private int newVulnerabilities;
    private String policyResult;
    private String gitRef;
    private String sha256;
    private Instant createdAt;

    public CiUploadEvent() {}

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
}
