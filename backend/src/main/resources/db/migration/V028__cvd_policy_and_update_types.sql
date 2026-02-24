-- V028: CVD Policy & Security Update Types (CRA Annexe I §2 compliance)
-- Annexe I §2(5): Manufacturers shall have a coordinated vulnerability disclosure (CVD) policy.
-- Annexe I §2(7): Security updates must be classified and delivered appropriately.

-- CVD Policy per product (Annexe I §2(5))
CREATE TABLE cvd_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id),
    -- CVD contact point (required by Art. 13(6))
    contact_email VARCHAR(500) NOT NULL,
    contact_url VARCHAR(1000),
    pgp_key_url VARCHAR(1000),
    -- Policy details
    policy_url VARCHAR(1000),
    disclosure_timeline_days INTEGER NOT NULL DEFAULT 90,
    accepts_anonymous BOOLEAN NOT NULL DEFAULT TRUE,
    bug_bounty_url VARCHAR(1000),
    -- Languages accepted for vulnerability reports
    accepted_languages VARCHAR(500) DEFAULT 'en,fr',
    -- Scope description
    scope_description TEXT,
    -- Status tracking
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMPTZ,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cvd_policy_product UNIQUE (product_id)
);

CREATE INDEX idx_cvd_policies_org ON cvd_policies(org_id);
CREATE INDEX idx_cvd_policies_product ON cvd_policies(product_id);

COMMENT ON TABLE cvd_policies IS 'Coordinated Vulnerability Disclosure policies per product (CRA Annexe I §2(5))';
COMMENT ON COLUMN cvd_policies.disclosure_timeline_days IS 'Max days before public disclosure after vendor notification (default 90, ISO 29147)';
COMMENT ON COLUMN cvd_policies.contact_email IS 'Security contact email for vulnerability reports (Art. 13(6))';

-- Security update types on releases (Annexe I §2(7))
ALTER TABLE releases
    ADD COLUMN update_type VARCHAR(50),
    ADD COLUMN security_impact VARCHAR(50),
    ADD COLUMN cve_ids TEXT;

COMMENT ON COLUMN releases.update_type IS 'SECURITY_CRITICAL, SECURITY_HIGH, SECURITY, FUNCTIONALITY, MAINTENANCE';
COMMENT ON COLUMN releases.security_impact IS 'Brief description of security impact for security updates';
COMMENT ON COLUMN releases.cve_ids IS 'Comma-separated CVE IDs addressed by this release';
