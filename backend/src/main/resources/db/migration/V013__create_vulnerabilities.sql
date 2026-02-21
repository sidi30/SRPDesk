-- V013: Vulnerabilities and findings

CREATE TABLE vulnerabilities (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    osv_id      VARCHAR(255) NOT NULL UNIQUE,
    summary     TEXT,
    details     TEXT,
    severity    VARCHAR(50),
    published   TIMESTAMPTZ,
    modified    TIMESTAMPTZ,
    aliases     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE findings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    release_id          UUID NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    component_id        UUID NOT NULL REFERENCES components(id),
    vulnerability_id    UUID NOT NULL REFERENCES vulnerabilities(id),
    status              VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    detected_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    source              VARCHAR(50) NOT NULL DEFAULT 'OSV',
    UNIQUE(release_id, component_id, vulnerability_id)
);

CREATE INDEX idx_findings_release ON findings(release_id);
CREATE INDEX idx_findings_status ON findings(status);

CREATE TABLE finding_decisions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    finding_id      UUID NOT NULL REFERENCES findings(id) ON DELETE CASCADE,
    decision_type   VARCHAR(50) NOT NULL,
    rationale       TEXT NOT NULL,
    due_date        DATE,
    decided_by      UUID NOT NULL,
    fix_release_id  UUID REFERENCES releases(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_finding_decisions_finding ON finding_decisions(finding_id);
