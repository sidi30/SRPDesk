-- Init script for PostgreSQL container
-- Database and user are already created by POSTGRES_DB/POSTGRES_USER env vars
-- This file is for any additional initialization

-- Ensure UUID extension is available
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
