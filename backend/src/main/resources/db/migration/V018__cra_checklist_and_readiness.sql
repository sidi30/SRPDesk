-- V018: CRA Compliance Checklist & Readiness Score tables

-- Checklist items: Annex I requirements per product
CREATE TABLE cra_checklist_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID NOT NULL,
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    requirement_ref VARCHAR(20) NOT NULL,
    category        VARCHAR(50) NOT NULL,
    title           VARCHAR(500) NOT NULL,
    description     TEXT,
    status          VARCHAR(30) NOT NULL DEFAULT 'NOT_ASSESSED',
    evidence_ids    UUID[] DEFAULT '{}',
    notes           TEXT,
    assessed_by     UUID,
    assessed_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_checklist_product_req UNIQUE (product_id, requirement_ref)
);

CREATE INDEX idx_checklist_org_id ON cra_checklist_items(org_id);
CREATE INDEX idx_checklist_product_id ON cra_checklist_items(product_id);
CREATE INDEX idx_checklist_status ON cra_checklist_items(status);

-- Readiness score snapshots
CREATE TABLE readiness_snapshots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID NOT NULL,
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    overall_score   INT NOT NULL,
    category_scores JSONB NOT NULL DEFAULT '{}',
    action_items    JSONB NOT NULL DEFAULT '[]',
    snapshot_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_readiness_org_id ON readiness_snapshots(org_id);
CREATE INDEX idx_readiness_product_id ON readiness_snapshots(product_id);
CREATE INDEX idx_readiness_snapshot_at ON readiness_snapshots(snapshot_at);

-- Add conformity_path to products
ALTER TABLE products ADD COLUMN conformity_path VARCHAR(100);
