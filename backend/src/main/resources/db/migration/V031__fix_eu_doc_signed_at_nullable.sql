-- V031: Fix signed_at to be nullable (null when DRAFT, set on signing)
ALTER TABLE eu_declarations_of_conformity ALTER COLUMN signed_at DROP NOT NULL;
