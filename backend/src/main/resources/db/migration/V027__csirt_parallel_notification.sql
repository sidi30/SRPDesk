-- V027: CSIRT Parallel Notification (CRA Art. 14 compliance)
-- Art. 14 requires manufacturers to notify BOTH ENISA (via SRP) AND the relevant national CSIRT
-- in parallel within 24 hours of becoming aware of an actively exploited vulnerability.

ALTER TABLE srp_submissions
    ADD COLUMN csirt_reference VARCHAR(500),
    ADD COLUMN csirt_submitted_at TIMESTAMPTZ,
    ADD COLUMN csirt_status VARCHAR(50),
    ADD COLUMN csirt_country_code VARCHAR(5);

COMMENT ON COLUMN srp_submissions.csirt_reference IS 'Reference returned by the national CSIRT after submission';
COMMENT ON COLUMN srp_submissions.csirt_submitted_at IS 'Timestamp of CSIRT notification';
COMMENT ON COLUMN srp_submissions.csirt_status IS 'Status of CSIRT notification: PENDING, SUBMITTED, ACKNOWLEDGED, FAILED';
COMMENT ON COLUMN srp_submissions.csirt_country_code IS 'ISO 3166-1 alpha-2 country code of the notified CSIRT';