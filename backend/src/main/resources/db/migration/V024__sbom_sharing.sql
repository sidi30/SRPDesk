-- V024: SBOM secure sharing with temporary access links

CREATE TABLE IF NOT EXISTS sbom_share_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    release_id UUID NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    evidence_id UUID NOT NULL REFERENCES evidences(id),
    token VARCHAR(64) NOT NULL UNIQUE,
    recipient_email VARCHAR(255),
    recipient_org VARCHAR(255),
    expires_at TIMESTAMPTZ NOT NULL,
    max_downloads INT DEFAULT 0,
    download_count INT NOT NULL DEFAULT 0,
    include_vex BOOLEAN NOT NULL DEFAULT FALSE,
    include_quality_score BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_share_links_token ON sbom_share_links(token);
CREATE INDEX IF NOT EXISTS idx_share_links_org ON sbom_share_links(org_id);
CREATE INDEX IF NOT EXISTS idx_share_links_release ON sbom_share_links(release_id);
