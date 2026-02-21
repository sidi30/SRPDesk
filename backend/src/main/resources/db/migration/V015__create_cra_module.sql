-- CRA War Room & SRP Autopilot module
-- Tables: cra_events, cra_event_participants, cra_event_links, org_sla_settings, srp_submissions

-- Organisation-level SLA settings (overrides defaults)
CREATE TABLE org_sla_settings (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id                          UUID NOT NULL UNIQUE,
    early_warning_hours             INT  NOT NULL DEFAULT 24,
    notification_hours              INT  NOT NULL DEFAULT 72,
    final_report_days_after_patch   INT  NOT NULL DEFAULT 14,
    final_report_days_after_resolve INT  NOT NULL DEFAULT 30,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_org_sla_settings_org ON org_sla_settings(org_id);

-- CRA Events (security events to be declared to SRP)
CREATE TABLE cra_events (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id             UUID         NOT NULL,
    product_id         UUID         NOT NULL REFERENCES products(id),
    event_type         VARCHAR(50)  NOT NULL CHECK (event_type IN ('EXPLOITED_VULNERABILITY', 'SEVERE_INCIDENT')),
    title              VARCHAR(500) NOT NULL,
    description        TEXT,
    status             VARCHAR(50)  NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'IN_REVIEW', 'SUBMITTED', 'CLOSED')),
    started_at         TIMESTAMPTZ,
    detected_at        TIMESTAMPTZ  NOT NULL,
    patch_available_at TIMESTAMPTZ,
    resolved_at        TIMESTAMPTZ,
    created_by         UUID         NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_cra_events_org     ON cra_events(org_id);
CREATE INDEX idx_cra_events_product ON cra_events(product_id);
CREATE INDEX idx_cra_events_status  ON cra_events(org_id, status);

-- Participants on a CRA Event (OWNER, APPROVER, VIEWER)
CREATE TABLE cra_event_participants (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    cra_event_id UUID        NOT NULL REFERENCES cra_events(id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL,
    role         VARCHAR(50) NOT NULL CHECK (role IN ('OWNER', 'APPROVER', 'VIEWER')),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(cra_event_id, user_id)
);

CREATE INDEX idx_cra_participants_event ON cra_event_participants(cra_event_id);

-- Links between CRA Event and existing entities (releases, findings, evidences)
CREATE TABLE cra_event_links (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    cra_event_id UUID        NOT NULL REFERENCES cra_events(id) ON DELETE CASCADE,
    link_type    VARCHAR(50) NOT NULL CHECK (link_type IN ('RELEASE', 'FINDING', 'EVIDENCE')),
    target_id    UUID        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(cra_event_id, link_type, target_id)
);

CREATE INDEX idx_cra_links_event ON cra_event_links(cra_event_id);

-- SRP Submissions (Early Warning, Notification, Final Report)
CREATE TABLE srp_submissions (
    id                          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    cra_event_id                UUID         NOT NULL REFERENCES cra_events(id) ON DELETE CASCADE,
    submission_type             VARCHAR(50)  NOT NULL CHECK (submission_type IN ('EARLY_WARNING', 'NOTIFICATION', 'FINAL_REPORT')),
    status                      VARCHAR(50)  NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'READY', 'EXPORTED', 'SUBMITTED')),
    content_json                JSONB        NOT NULL DEFAULT '{}',
    schema_version              VARCHAR(20)  NOT NULL DEFAULT '1.0',
    validation_errors           JSONB,
    submitted_reference         VARCHAR(500),
    submitted_at                TIMESTAMPTZ,
    acknowledgment_evidence_id  UUID,
    generated_by                UUID         NOT NULL,
    generated_at                TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_srp_submissions_event ON srp_submissions(cra_event_id);
CREATE INDEX idx_srp_submissions_type  ON srp_submissions(cra_event_id, submission_type);
