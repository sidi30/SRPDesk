package com.lexsecura.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cvd_policies")
public class CvdPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_id", nullable = false)
    private UUID orgId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "contact_email", length = 500, nullable = false)
    private String contactEmail;

    @Column(name = "contact_url", length = 1000)
    private String contactUrl;

    @Column(name = "pgp_key_url", length = 1000)
    private String pgpKeyUrl;

    @Column(name = "policy_url", length = 1000)
    private String policyUrl;

    @Column(name = "disclosure_timeline_days", nullable = false)
    private int disclosureTimelineDays;

    @Column(name = "accepts_anonymous", nullable = false)
    private boolean acceptsAnonymous;

    @Column(name = "bug_bounty_url", length = 1000)
    private String bugBountyUrl;

    @Column(name = "accepted_languages", length = 500)
    private String acceptedLanguages;

    @Column(name = "scope_description", columnDefinition = "TEXT")
    private String scopeDescription;

    @Column(length = 50, nullable = false)
    private String status;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (status == null) status = "DRAFT";
        if (disclosureTimelineDays == 0) disclosureTimelineDays = 90;
        if (acceptedLanguages == null) acceptedLanguages = "en,fr";
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
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
