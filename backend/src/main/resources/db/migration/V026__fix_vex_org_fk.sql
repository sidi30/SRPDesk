-- V026: Remove FK constraint on vex_documents.org_id -> organizations
-- The organizations table uses a different pattern (org_id comes from JWT, not a local table)

ALTER TABLE vex_documents DROP CONSTRAINT IF EXISTS vex_documents_org_id_fkey;
