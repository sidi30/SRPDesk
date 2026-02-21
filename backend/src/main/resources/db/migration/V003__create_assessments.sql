CREATE TABLE assessments (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id           UUID NOT NULL,
    product_id       UUID NOT NULL REFERENCES products(id),
    title            VARCHAR(500) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by       UUID NOT NULL,
    reviewed_by      UUID,
    reviewer_comment TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    submitted_at     TIMESTAMPTZ,
    reviewed_at      TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_assessments_org_id ON assessments(org_id);
CREATE INDEX idx_assessments_product_id ON assessments(product_id);
