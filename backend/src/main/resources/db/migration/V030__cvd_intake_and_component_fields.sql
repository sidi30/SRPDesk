-- V030: CVD Intake (vulnerability report submission) + SBOM component license/supplier fields

-- ── 1. Add license and supplier to components ──────────────────────
ALTER TABLE components ADD COLUMN license VARCHAR(500);
ALTER TABLE components ADD COLUMN supplier VARCHAR(500);

-- ── 2. Vulnerability report tracking sequence ──────────────────────
-- Generates yearly tracking numbers: VR-YYYY-NNN
CREATE SEQUENCE vr_tracking_seq START WITH 1 INCREMENT BY 1;

-- ── 3. Vulnerability Reports table (CVD Intake — CRA Art. 13(6)) ──
CREATE TABLE vulnerability_reports (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID NOT NULL,
    product_id      UUID REFERENCES products(id),
    tracking_id     VARCHAR(20) NOT NULL UNIQUE,     -- VR-2026-001
    status          VARCHAR(30) NOT NULL DEFAULT 'NEW',
    -- Reporter info (public submission)
    reporter_name   VARCHAR(255),
    reporter_email  VARCHAR(500),
    reporter_pgp_fingerprint VARCHAR(100),
    is_anonymous    BOOLEAN NOT NULL DEFAULT false,
    -- Vulnerability details
    title           VARCHAR(500) NOT NULL,
    description     TEXT NOT NULL,
    severity_estimate VARCHAR(20),                    -- reporter's estimate
    affected_component VARCHAR(500),
    affected_versions  VARCHAR(500),
    steps_to_reproduce TEXT,
    proof_of_concept   TEXT,
    -- Internal triage
    assigned_to     UUID,
    internal_notes  TEXT,
    internal_severity VARCHAR(20),                    -- CVSS-based
    cvss_score      DECIMAL(3,1),
    cve_id          VARCHAR(30),
    -- Timestamps
    submitted_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    acknowledged_at TIMESTAMPTZ,
    triaged_at      TIMESTAMPTZ,
    fixed_at        TIMESTAMPTZ,
    disclosed_at    TIMESTAMPTZ,
    disclosure_deadline TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vuln_reports_org ON vulnerability_reports(org_id);
CREATE INDEX idx_vuln_reports_status ON vulnerability_reports(status);
CREATE INDEX idx_vuln_reports_tracking ON vulnerability_reports(tracking_id);
CREATE INDEX idx_vuln_reports_product ON vulnerability_reports(product_id);

COMMENT ON TABLE vulnerability_reports IS 'CVD intake — CRA Art. 13(6) coordinated vulnerability disclosure';
COMMENT ON COLUMN vulnerability_reports.tracking_id IS 'Public tracking ID: VR-YYYY-NNN';
COMMENT ON COLUMN vulnerability_reports.status IS 'Workflow: NEW → ACKNOWLEDGED → TRIAGING → CONFIRMED/REJECTED → FIXING → FIXED → DISCLOSED';
