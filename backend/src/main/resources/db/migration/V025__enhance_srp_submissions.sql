-- V025: Enhanced SRP submissions with ENISA tracking and retry support

ALTER TABLE srp_submissions ADD COLUMN IF NOT EXISTS enisa_reference VARCHAR(500);
ALTER TABLE srp_submissions ADD COLUMN IF NOT EXISTS enisa_submitted_at TIMESTAMPTZ;
ALTER TABLE srp_submissions ADD COLUMN IF NOT EXISTS enisa_status VARCHAR(50);
ALTER TABLE srp_submissions ADD COLUMN IF NOT EXISTS retry_count INT NOT NULL DEFAULT 0;
ALTER TABLE srp_submissions ADD COLUMN IF NOT EXISTS last_error TEXT;
