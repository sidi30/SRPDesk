CREATE TABLE assessment_items (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id     UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    org_id            UUID NOT NULL,
    requirement_id    UUID NOT NULL REFERENCES requirements(id),
    compliance_status VARCHAR(30) NOT NULL DEFAULT 'NOT_ASSESSED',
    notes             TEXT,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(assessment_id, requirement_id)
);

CREATE INDEX idx_assessment_items_org_id ON assessment_items(org_id);
