-- API Keys for CI/CD integrations
CREATE TABLE api_keys (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID        NOT NULL,
    name        VARCHAR(255) NOT NULL,
    key_prefix  VARCHAR(12)  NOT NULL,
    key_hash    VARCHAR(64)  NOT NULL,
    scopes      VARCHAR(255) NOT NULL DEFAULT 'ci:sbom',
    created_by  UUID        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at TIMESTAMPTZ,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    revoked_at  TIMESTAMPTZ
);

CREATE INDEX idx_api_keys_key_hash ON api_keys (key_hash);
CREATE INDEX idx_api_keys_org_id ON api_keys (org_id);
