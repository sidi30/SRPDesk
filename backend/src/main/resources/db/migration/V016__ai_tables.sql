-- AI module: jobs and artifacts for LLM-assisted drafting
-- Each AI generation is tracked as a job with input/output hashes for audit

CREATE TABLE ai_jobs (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID         NOT NULL,
    job_type        VARCHAR(50)  NOT NULL CHECK (job_type IN ('SRP_DRAFT', 'COMM_PACK', 'QUESTIONNAIRE_FILL')),
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    model           VARCHAR(100) NOT NULL,
    params_json     JSONB        NOT NULL DEFAULT '{}',
    input_hash      VARCHAR(128) NOT NULL,
    output_hash     VARCHAR(128),
    error           TEXT,
    created_by      UUID         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at    TIMESTAMPTZ
);

CREATE INDEX idx_ai_jobs_org        ON ai_jobs(org_id);
CREATE INDEX idx_ai_jobs_status     ON ai_jobs(org_id, status);
CREATE INDEX idx_ai_jobs_type       ON ai_jobs(org_id, job_type);

CREATE TABLE ai_artifacts (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    ai_job_id       UUID         NOT NULL REFERENCES ai_jobs(id) ON DELETE CASCADE,
    kind            VARCHAR(50)  NOT NULL CHECK (kind IN ('SRP_DRAFT', 'ADVISORY', 'EMAIL', 'RELEASE_NOTES', 'QUESTIONNAIRE_ANSWERS')),
    content_json    JSONB        NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_ai_artifacts_job ON ai_artifacts(ai_job_id);
