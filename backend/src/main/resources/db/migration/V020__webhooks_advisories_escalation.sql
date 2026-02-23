-- V020: Webhooks, Security Advisories, Escalation support for Art.14 CRA compliance

-- Outbound webhooks per organization
CREATE TABLE webhooks (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id         UUID NOT NULL,
    name           VARCHAR(255) NOT NULL,
    url            VARCHAR(2048) NOT NULL,
    secret         VARCHAR(512),
    event_types    VARCHAR(1024) NOT NULL DEFAULT '*',
    channel_type   VARCHAR(50) NOT NULL DEFAULT 'GENERIC',
    enabled        BOOLEAN NOT NULL DEFAULT TRUE,
    created_by     UUID NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_webhooks_org_id ON webhooks(org_id);

-- Webhook delivery log
CREATE TABLE webhook_deliveries (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    webhook_id     UUID NOT NULL REFERENCES webhooks(id) ON DELETE CASCADE,
    event_type     VARCHAR(100) NOT NULL,
    payload        JSONB NOT NULL,
    http_status    INT,
    response_body  TEXT,
    success        BOOLEAN NOT NULL DEFAULT FALSE,
    attempt        INT NOT NULL DEFAULT 1,
    delivered_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_webhook_deliveries_webhook_id ON webhook_deliveries(webhook_id);

-- Security advisories (Art.14.3 user notifications)
CREATE TABLE security_advisories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID NOT NULL,
    cra_event_id    UUID NOT NULL REFERENCES cra_events(id),
    product_id      UUID NOT NULL,
    title           VARCHAR(500) NOT NULL,
    severity        VARCHAR(20) NOT NULL DEFAULT 'HIGH',
    affected_versions TEXT,
    description     TEXT NOT NULL,
    remediation     TEXT,
    advisory_url    VARCHAR(2048),
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    published_at    TIMESTAMPTZ,
    notified_at     TIMESTAMPTZ,
    created_by      UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_security_advisories_org_id ON security_advisories(org_id);
CREATE INDEX idx_security_advisories_cra_event_id ON security_advisories(cra_event_id);

-- Add escalation tracking to CRA events
ALTER TABLE cra_events ADD COLUMN IF NOT EXISTS escalation_level VARCHAR(20) DEFAULT 'NONE';
ALTER TABLE cra_events ADD COLUMN IF NOT EXISTS escalated_at TIMESTAMPTZ;
ALTER TABLE cra_events ADD COLUMN IF NOT EXISTS auto_submitted BOOLEAN DEFAULT FALSE;

-- Notification log for email alerts
CREATE TABLE notification_log (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id         UUID NOT NULL,
    cra_event_id   UUID NOT NULL REFERENCES cra_events(id),
    channel        VARCHAR(30) NOT NULL,
    recipient      VARCHAR(500) NOT NULL,
    subject        VARCHAR(500) NOT NULL,
    deadline_type  VARCHAR(50) NOT NULL,
    alert_level    VARCHAR(20) NOT NULL,
    sent_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_log_event ON notification_log(cra_event_id);
CREATE INDEX idx_notification_log_org ON notification_log(org_id);

-- ENISA SRP submission tracking
ALTER TABLE srp_submissions ADD COLUMN IF NOT EXISTS enisa_reference VARCHAR(255);
ALTER TABLE srp_submissions ADD COLUMN IF NOT EXISTS enisa_submitted_at TIMESTAMPTZ;
ALTER TABLE srp_submissions ADD COLUMN IF NOT EXISTS enisa_status VARCHAR(50);
ALTER TABLE srp_submissions ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;
ALTER TABLE srp_submissions ADD COLUMN IF NOT EXISTS last_error TEXT;
