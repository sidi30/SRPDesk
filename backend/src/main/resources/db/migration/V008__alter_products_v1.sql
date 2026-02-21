-- V008: Evolve products table for V1 domain model
-- Drop old columns, add new ones

ALTER TABLE products DROP COLUMN IF EXISTS version;
ALTER TABLE products DROP COLUMN IF EXISTS category;
ALTER TABLE products DROP COLUMN IF EXISTS description;
ALTER TABLE products DROP COLUMN IF EXISTS status;

ALTER TABLE products ADD COLUMN type VARCHAR(100) NOT NULL DEFAULT 'SOFTWARE';
ALTER TABLE products ADD COLUMN criticality VARCHAR(50) NOT NULL DEFAULT 'STANDARD';
ALTER TABLE products ADD COLUMN contacts JSONB NOT NULL DEFAULT '[]';
