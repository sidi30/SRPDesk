-- V007: Drop legacy assessment-based tables (clean break for V1 domain model)
-- Order matters: drop dependent tables first

DROP TABLE IF EXISTS evidences;
DROP TABLE IF EXISTS assessment_items;
DROP TABLE IF EXISTS assessments;
DROP TABLE IF EXISTS requirements;
