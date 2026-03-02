-- V032: CI/CD Pipeline Enhancements
-- CI policies (per-org thresholds) + upload event tracking + GitHub webhook support

-- CI policies: per-org quality gates for CI/CD pipelines
CREATE TABLE ci_policies (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID NOT NULL UNIQUE,
    max_critical      INT NOT NULL DEFAULT 0,
    max_high          INT NOT NULL DEFAULT 5,
    min_quality_score INT NOT NULL DEFAULT 50,
    block_on_fail     BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ci_policies_org_id ON ci_policies(org_id);

-- CI upload events: tracking each SBOM upload from CI
CREATE TABLE ci_upload_events (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id            UUID NOT NULL,
    product_id        UUID NOT NULL,
    release_id        UUID,
    component_count   INT NOT NULL DEFAULT 0,
    new_components    INT NOT NULL DEFAULT 0,
    removed_components INT NOT NULL DEFAULT 0,
    quality_score     INT NOT NULL DEFAULT 0,
    quality_grade     VARCHAR(2),
    vuln_critical     INT NOT NULL DEFAULT 0,
    vuln_high         INT NOT NULL DEFAULT 0,
    vuln_medium       INT NOT NULL DEFAULT 0,
    vuln_low          INT NOT NULL DEFAULT 0,
    vuln_total        INT NOT NULL DEFAULT 0,
    new_vulnerabilities INT NOT NULL DEFAULT 0,
    policy_result     VARCHAR(10),
    git_ref           VARCHAR(255),
    sha256            VARCHAR(64),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ci_upload_events_org_id ON ci_upload_events(org_id);
CREATE INDEX idx_ci_upload_events_product_id ON ci_upload_events(product_id);
CREATE INDEX idx_ci_upload_events_created_at ON ci_upload_events(created_at DESC);

-- Add repo_full_name to product_repo_mappings for GitHub webhook lookups
ALTER TABLE product_repo_mappings ADD COLUMN IF NOT EXISTS repo_full_name VARCHAR(255);
CREATE INDEX idx_product_repo_mappings_forge_repo ON product_repo_mappings(forge, repo_full_name);
