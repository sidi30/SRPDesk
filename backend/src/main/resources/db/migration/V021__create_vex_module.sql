-- V021: VEX (Vulnerability Exploitability eXchange) lifecycle management

CREATE TABLE vex_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL REFERENCES organizations(id),
    release_id UUID NOT NULL REFERENCES releases(id),
    format VARCHAR(20) NOT NULL CHECK (format IN ('OPENVEX', 'CYCLONEDX_VEX', 'CSAF')),
    version INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PUBLISHED', 'SUPERSEDED')),
    document_json JSONB NOT NULL,
    sha256_hash VARCHAR(64) NOT NULL,
    generated_by VARCHAR(200),
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE vex_statements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vex_document_id UUID NOT NULL REFERENCES vex_documents(id) ON DELETE CASCADE,
    finding_id UUID NOT NULL REFERENCES findings(id),
    decision_id UUID REFERENCES finding_decisions(id),
    vulnerability_id VARCHAR(50) NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id),
    status VARCHAR(30) NOT NULL CHECK (status IN (
        'not_affected', 'affected', 'fixed', 'under_investigation'
    )),
    justification VARCHAR(80),
    impact_statement TEXT,
    action_statement TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vex_documents_release ON vex_documents(release_id);
CREATE INDEX idx_vex_documents_org ON vex_documents(org_id);
CREATE INDEX idx_vex_statements_finding ON vex_statements(finding_id);
CREATE INDEX idx_vex_statements_vuln ON vex_statements(vulnerability_id);
