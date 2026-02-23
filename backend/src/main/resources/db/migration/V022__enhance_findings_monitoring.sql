-- V022: Enhanced findings for continuous multi-source monitoring

ALTER TABLE vulnerabilities ADD COLUMN IF NOT EXISTS actively_exploited BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE vulnerabilities ADD COLUMN IF NOT EXISTS kev_date_added DATE;
ALTER TABLE vulnerabilities ADD COLUMN IF NOT EXISTS euvd_id VARCHAR(50);
ALTER TABLE vulnerabilities ADD COLUMN IF NOT EXISTS cvss_score DECIMAL(3,1);
ALTER TABLE vulnerabilities ADD COLUMN IF NOT EXISTS cvss_vector VARCHAR(100);

ALTER TABLE findings ADD COLUMN IF NOT EXISTS source VARCHAR(20) NOT NULL DEFAULT 'OSV';
ALTER TABLE findings ADD COLUMN IF NOT EXISTS last_checked_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_vuln_actively_exploited ON vulnerabilities(actively_exploited) WHERE actively_exploited = TRUE;
CREATE INDEX IF NOT EXISTS idx_findings_source ON findings(source);
