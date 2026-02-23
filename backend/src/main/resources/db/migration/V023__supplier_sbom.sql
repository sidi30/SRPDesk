-- V023: Supplier SBOM tracking for supply chain management

CREATE TABLE IF NOT EXISTS supplier_sboms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    release_id UUID NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    supplier_name VARCHAR(255) NOT NULL,
    supplier_url VARCHAR(500),
    evidence_id UUID REFERENCES evidences(id),
    component_count INT NOT NULL DEFAULT 0,
    format VARCHAR(20) NOT NULL DEFAULT 'CYCLONEDX',
    imported_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    imported_by UUID
);

CREATE INDEX IF NOT EXISTS idx_supplier_sboms_org ON supplier_sboms(org_id);
CREATE INDEX IF NOT EXISTS idx_supplier_sboms_release ON supplier_sboms(release_id);
