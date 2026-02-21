-- V010: Create audit_events table for append-only hash chain

CREATE TABLE audit_events (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id        UUID NOT NULL,
    entity_type   VARCHAR(100) NOT NULL,
    entity_id     UUID NOT NULL,
    action        VARCHAR(50) NOT NULL,
    actor         UUID NOT NULL,
    payload_json  TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    prev_hash     VARCHAR(64),
    hash          VARCHAR(64) NOT NULL
);

CREATE INDEX idx_audit_events_org_id ON audit_events(org_id);
CREATE INDEX idx_audit_events_entity ON audit_events(entity_type, entity_id);
CREATE INDEX idx_audit_events_created ON audit_events(org_id, created_at);
