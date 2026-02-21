CREATE TABLE products (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id        UUID NOT NULL,
    name          VARCHAR(255) NOT NULL,
    version       VARCHAR(100),
    category      VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',
    description   TEXT,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_org_id ON products(org_id);
