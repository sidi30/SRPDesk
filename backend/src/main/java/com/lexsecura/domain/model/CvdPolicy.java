package com.lexsecura.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Coordinated Vulnerability Disclosure (CVD) policy per product.
 * CRA Annexe I §2(5): Manufacturers shall put in place and enforce a policy
 * on coordinated vulnerability disclosure.
 * Art. 13(6): Manufacturers shall provide a single point of contact for
 * vulnerability reporting.
 */
public class CvdPolicy {

    private UUID id;
    private UUID orgId;
    private UUID productId;
    private String contactEmail;
    private String contactUrl;
    private String pgpKeyUrl;
    private String policyUrl;
    private int disclosureTimelineDays;
    private boolean acceptsAnonymous;
    private String bugBountyUrl;
    private String acceptedLanguages;
    private String scopeDescription;
    private String status;
    private Instant publishedAt;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public CvdPolicy() {
        this.disclosureTimelineDays = 90;
        this.acceptsAnonymous = true;
        this.acceptedLanguages = "en,fr";
        this.status = "DRAFT";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrgId() { return orgId; }
    public void setOrgId(UUID orgId) { this.orgId = orgId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactUrl() { return contactUrl; }
    public void setContactUrl(String contactUrl) { this.contactUrl = contactUrl; }
    public String getPgpKeyUrl() { return pgpKeyUrl; }
    public void setPgpKeyUrl(String pgpKeyUrl) { this.pgpKeyUrl = pgpKeyUrl; }
    public String getPolicyUrl() { return policyUrl; }
    public void setPolicyUrl(String policyUrl) { this.policyUrl = policyUrl; }
    public int getDisclosureTimelineDays() { return disclosureTimelineDays; }
    public void setDisclosureTimelineDays(int disclosureTimelineDays) { this.disclosureTimelineDays = disclosureTimelineDays; }
    public boolean isAcceptsAnonymous() { return acceptsAnonymous; }
    public void setAcceptsAnonymous(boolean acceptsAnonymous) { this.acceptsAnonymous = acceptsAnonymous; }
    public String getBugBountyUrl() { return bugBountyUrl; }
    public void setBugBountyUrl(String bugBountyUrl) { this.bugBountyUrl = bugBountyUrl; }
    public String getAcceptedLanguages() { return acceptedLanguages; }
    public void setAcceptedLanguages(String acceptedLanguages) { this.acceptedLanguages = acceptedLanguages; }
    public String getScopeDescription() { return scopeDescription; }
    public void setScopeDescription(String scopeDescription) { this.scopeDescription = scopeDescription; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
