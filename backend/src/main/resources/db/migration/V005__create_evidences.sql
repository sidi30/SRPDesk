CREATE TABLE evidences (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id              UUID NOT NULL,
    assessment_item_id  UUID NOT NULL REFERENCES assessment_items(id) ON DELETE CASCADE,
    file_name           VARCHAR(500) NOT NULL,
    file_size           BIGINT NOT NULL,
    content_type        VARCHAR(255) NOT NULL,
    storage_key         VARCHAR(1000) NOT NULL,
    sha256              VARCHAR(64) NOT NULL,
    uploaded_by         UUID NOT NULL,
    uploaded_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_evidences_org_id ON evidences(org_id);
CREATE INDEX idx_evidences_item_id ON evidences(assessment_item_id);
