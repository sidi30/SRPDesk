-- V011: GitLab integration - product to repo mapping + webhook idempotence

CREATE TABLE product_repo_mappings (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID NOT NULL,
    product_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    forge       VARCHAR(20) NOT NULL DEFAULT 'GITLAB',
    project_id  BIGINT NOT NULL,
    repo_url    VARCHAR(1000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(org_id, forge, project_id)
);

CREATE INDEX idx_product_repo_mappings_org ON product_repo_mappings(org_id);

CREATE TABLE processed_webhook_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    forge           VARCHAR(20) NOT NULL,
    event_id        VARCHAR(255) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(forge, event_id)
);
