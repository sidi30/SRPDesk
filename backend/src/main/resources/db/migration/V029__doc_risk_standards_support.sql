-- V029: EU Declaration of Conformity, Conformity Assessments, Risk Assessments & Applied Standards
-- CRA Art. 28 (EU DoC), Annexe V (DoC content), Art. 32 (conformity assessment procedures),
-- Art. 13(1) (cybersecurity risk assessment), Art. 13(15) (harmonised standards presumption of conformity)

-- ============================================================================
-- 1. EU Declarations of Conformity (Annex V)
-- ============================================================================
CREATE TABLE eu_declarations_of_conformity (
    id                            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id                        UUID NOT NULL,
    product_id                    UUID NOT NULL REFERENCES products(id),
    declaration_number            VARCHAR(200) NOT NULL,
    -- Manufacturer identification (Annexe V §2)
    manufacturer_name             VARCHAR(500) NOT NULL,
    manufacturer_address          TEXT NOT NULL,
    -- Authorised representative (Annexe V §3)
    authorized_rep_name           VARCHAR(500),
    authorized_rep_address        TEXT,
    -- Product identification (Annexe V §4)
    product_name                  VARCHAR(500) NOT NULL,
    product_identification        TEXT NOT NULL,
    -- Conformity assessment module used (Annexe V §5, Art. 32)
    conformity_assessment_module  VARCHAR(50) NOT NULL DEFAULT 'MODULE_A',
    -- Notified body details if Module H (Annexe V §7)
    notified_body_name            VARCHAR(500),
    notified_body_number          VARCHAR(50),
    notified_body_certificate     VARCHAR(200),
    -- Applied harmonised standards (Annexe V §6)
    harmonised_standards          TEXT,
    additional_info               TEXT,
    -- The actual declaration statement (Annexe V §8)
    declaration_text              TEXT NOT NULL,
    -- Signature (Annexe V §9-10)
    signed_by                     VARCHAR(500) NOT NULL,
    signed_role                   VARCHAR(200) NOT NULL,
    signed_at                     TIMESTAMPTZ NOT NULL,
    -- Workflow
    status                        VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    published_at                  TIMESTAMPTZ,
    -- Audit
    created_by                    UUID NOT NULL,
    created_at                    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_doc_product UNIQUE (product_id, declaration_number)
);

CREATE INDEX idx_eu_doc_org ON eu_declarations_of_conformity(org_id);

COMMENT ON TABLE eu_declarations_of_conformity IS 'EU Declaration of Conformity per product (CRA Art. 28, Annexe V)';
COMMENT ON COLUMN eu_declarations_of_conformity.declaration_number IS 'Unique identifier for the DoC (Annexe V §1)';
COMMENT ON COLUMN eu_declarations_of_conformity.manufacturer_name IS 'Name of the manufacturer (Annexe V §2)';
COMMENT ON COLUMN eu_declarations_of_conformity.manufacturer_address IS 'Registered address of the manufacturer (Annexe V §2)';
COMMENT ON COLUMN eu_declarations_of_conformity.authorized_rep_name IS 'Authorised representative name, if applicable (Annexe V §3)';
COMMENT ON COLUMN eu_declarations_of_conformity.authorized_rep_address IS 'Authorised representative address (Annexe V §3)';
COMMENT ON COLUMN eu_declarations_of_conformity.product_identification IS 'Description allowing traceability: model, batch, serial range (Annexe V §4)';
COMMENT ON COLUMN eu_declarations_of_conformity.conformity_assessment_module IS 'Conformity assessment procedure used: MODULE_A (internal control) or MODULE_H (full QA) per Art. 32';
COMMENT ON COLUMN eu_declarations_of_conformity.notified_body_name IS 'Name of notified body involved in conformity assessment, if Module H (Annexe V §7)';
COMMENT ON COLUMN eu_declarations_of_conformity.notified_body_number IS 'Identification number of the notified body (Annexe V §7)';
COMMENT ON COLUMN eu_declarations_of_conformity.notified_body_certificate IS 'Certificate reference issued by the notified body (Annexe V §7)';
COMMENT ON COLUMN eu_declarations_of_conformity.harmonised_standards IS 'Comma-separated list of harmonised standards applied (Annexe V §6)';
COMMENT ON COLUMN eu_declarations_of_conformity.declaration_text IS 'The conformity declaration statement text (Annexe V §8)';
COMMENT ON COLUMN eu_declarations_of_conformity.signed_by IS 'Person who signed the declaration on behalf of the manufacturer (Annexe V §9)';
COMMENT ON COLUMN eu_declarations_of_conformity.signed_role IS 'Function/role of the signatory (Annexe V §10)';
COMMENT ON COLUMN eu_declarations_of_conformity.status IS 'Workflow status: DRAFT, SIGNED, PUBLISHED';

-- ============================================================================
-- 2. Conformity Assessments (Art. 32 - Module A / Module H)
-- ============================================================================
CREATE TABLE conformity_assessments (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id            UUID NOT NULL,
    product_id        UUID NOT NULL REFERENCES products(id),
    module            VARCHAR(50) NOT NULL,
    status            VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    current_step      INTEGER NOT NULL DEFAULT 0,
    total_steps       INTEGER NOT NULL,
    steps_data        JSONB NOT NULL DEFAULT '[]',
    started_at        TIMESTAMPTZ,
    completed_at      TIMESTAMPTZ,
    approved_by       UUID,
    approved_at       TIMESTAMPTZ,
    created_by        UUID NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_assessment_product UNIQUE (product_id, module)
);

CREATE INDEX idx_conformity_assessments_org ON conformity_assessments(org_id);

COMMENT ON TABLE conformity_assessments IS 'Conformity assessment workflow per product and module (CRA Art. 32)';
COMMENT ON COLUMN conformity_assessments.module IS 'Assessment module: MODULE_A (internal control, Art. 32(3)) or MODULE_H (full quality assurance, Art. 32(4))';
COMMENT ON COLUMN conformity_assessments.status IS 'Workflow status: NOT_STARTED, IN_PROGRESS, COMPLETED, APPROVED';
COMMENT ON COLUMN conformity_assessments.steps_data IS 'JSON array of assessment steps: [{name, description, status, completedAt, notes, evidenceIds}]';
COMMENT ON COLUMN conformity_assessments.current_step IS 'Zero-based index of the current assessment step';
COMMENT ON COLUMN conformity_assessments.total_steps IS 'Total number of steps in the assessment procedure';

-- ============================================================================
-- 3. Risk Assessments (Art. 13(1) - Cybersecurity Risk Assessment)
-- ============================================================================
CREATE TABLE risk_assessments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id              UUID NOT NULL,
    product_id          UUID NOT NULL REFERENCES products(id),
    title               VARCHAR(500) NOT NULL,
    methodology         VARCHAR(100) NOT NULL DEFAULT 'STRIDE',
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    overall_risk_level  VARCHAR(50),
    summary             TEXT,
    approved_by         UUID,
    approved_at         TIMESTAMPTZ,
    created_by          UUID NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_risk_assessments_org ON risk_assessments(org_id);
CREATE INDEX idx_risk_assessments_product ON risk_assessments(product_id);

COMMENT ON TABLE risk_assessments IS 'Cybersecurity risk assessment per product (CRA Art. 13(1))';
COMMENT ON COLUMN risk_assessments.methodology IS 'Risk assessment methodology: STRIDE, DREAD, or CUSTOM';
COMMENT ON COLUMN risk_assessments.status IS 'Workflow status: DRAFT, IN_REVIEW, APPROVED';
COMMENT ON COLUMN risk_assessments.overall_risk_level IS 'Aggregated risk level: CRITICAL, HIGH, MEDIUM, LOW';
COMMENT ON COLUMN risk_assessments.summary IS 'Executive summary of risk assessment findings';

-- ============================================================================
-- 4. Risk Items (Individual risk entries within an assessment)
-- ============================================================================
CREATE TABLE risk_items (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    risk_assessment_id    UUID NOT NULL REFERENCES risk_assessments(id) ON DELETE CASCADE,
    threat_category       VARCHAR(100) NOT NULL,
    threat_description    TEXT NOT NULL,
    affected_asset        VARCHAR(500),
    likelihood            VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    impact                VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    risk_level            VARCHAR(50) NOT NULL,
    existing_controls     TEXT,
    mitigation_plan       TEXT,
    mitigation_status     VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    residual_risk_level   VARCHAR(50),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_risk_items_assessment ON risk_items(risk_assessment_id);

COMMENT ON TABLE risk_items IS 'Individual risk entries within a cybersecurity risk assessment (CRA Art. 13(1))';
COMMENT ON COLUMN risk_items.threat_category IS 'Threat category (e.g. SPOOFING, TAMPERING, REPUDIATION, INFO_DISCLOSURE, DOS, ELEVATION for STRIDE)';
COMMENT ON COLUMN risk_items.likelihood IS 'Likelihood rating: VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH';
COMMENT ON COLUMN risk_items.impact IS 'Impact rating: VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH';
COMMENT ON COLUMN risk_items.risk_level IS 'Computed risk level from likelihood x impact: CRITICAL, HIGH, MEDIUM, LOW';
COMMENT ON COLUMN risk_items.mitigation_status IS 'Mitigation progress: PENDING, IN_PROGRESS, IMPLEMENTED, ACCEPTED';
COMMENT ON COLUMN risk_items.residual_risk_level IS 'Risk level after mitigation controls are applied';

-- ============================================================================
-- 5. Applied Standards (Harmonised standards tracking per product)
-- ============================================================================
CREATE TABLE applied_standards (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id              UUID NOT NULL,
    product_id          UUID NOT NULL REFERENCES products(id),
    standard_code       VARCHAR(200) NOT NULL,
    standard_title      VARCHAR(500) NOT NULL,
    version             VARCHAR(100),
    compliance_status   VARCHAR(50) NOT NULL DEFAULT 'CLAIMED',
    notes               TEXT,
    evidence_ids        TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_standard_product UNIQUE (product_id, standard_code)
);

CREATE INDEX idx_applied_standards_org ON applied_standards(org_id);

COMMENT ON TABLE applied_standards IS 'Harmonised standards applied per product for presumption of conformity (CRA Art. 13(15), Art. 27)';
COMMENT ON COLUMN applied_standards.standard_code IS 'Standard reference code (e.g. EN 303 645, IEC 62443-4-1, ISO/IEC 27001)';
COMMENT ON COLUMN applied_standards.standard_title IS 'Full title of the standard';
COMMENT ON COLUMN applied_standards.compliance_status IS 'Compliance status: CLAIMED, PARTIAL, FULL, NOT_APPLICABLE';
COMMENT ON COLUMN applied_standards.evidence_ids IS 'Comma-separated UUIDs of evidence items supporting the compliance claim';
