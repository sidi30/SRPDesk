# ADR 003: S3 Storage with SHA-256 Integrity

## Status
Accepted

## Context
Evidence files (SBOM, test reports, vulnerability scans) must be stored with verifiable integrity for compliance auditing.

## Decision
- Store files in S3-compatible storage (MinIO for local dev)
- Compute SHA-256 hash during upload using `DigestInputStream` (streaming, no full-file buffering)
- Store hash in database alongside file metadata
- Storage key format: `{org_id}/{assessment_id}/{uuid}_{filename}`

## Consequences
- File integrity can be verified at any time
- Streaming hash computation avoids memory issues with large files
- S3 provides durability and scalability
- MinIO provides S3-compatible API for local development
