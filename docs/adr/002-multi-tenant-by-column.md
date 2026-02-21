# ADR 002: Multi-Tenancy via org_id Column

## Status
Accepted

## Context
Multiple organizations use the same application. Data isolation is critical for security and compliance.

## Decision
Use column-level multi-tenancy with `org_id` on every tenant-scoped table:
- `org_id` extracted from JWT (Keycloak custom claim)
- Stored in `TenantContext` (ThreadLocal) via `JwtTenantFilter`
- Every repository query filters by `org_id`
- `requirements` table is global (shared reference data)

## Consequences
- Simple to implement, no schema-per-tenant complexity
- Every query MUST include org_id filter (risk of data leak if forgotten)
- Tests must verify cross-tenant isolation
- Good enough for MVP scale
