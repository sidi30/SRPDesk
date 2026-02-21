-- V009: Create releases table and new evidences table

CREATE TABLE releases (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    version         VARCHAR(100) NOT NULL,
    git_ref         VARCHAR(255),
    build_id        VARCHAR(255),
    released_at     TIMESTAMPTZ,
    supported_until TIMESTAMPTZ,
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_releases_product_id ON releases(product_id);

CREATE TABLE evidences (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    release_id      UUID NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    org_id          UUID NOT NULL,
    type            VARCHAR(100) NOT NULL,
    filename        VARCHAR(500) NOT NULL,
    content_type    VARCHAR(255) NOT NULL,
    size            BIGINT NOT NULL,
    sha256          VARCHAR(64) NOT NULL,
    storage_uri     VARCHAR(1000) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID NOT NULL
);

CREATE INDEX idx_evidences_release_id ON evidences(release_id);
CREATE INDEX idx_evidences_org_id ON evidences(org_id);
