# ADR 001: Hexagonal Architecture

## Status
Accepted

## Context
We need a clean separation between business logic and infrastructure concerns (database, S3, Keycloak) to enable testability and future adaptability.

## Decision
Adopt Hexagonal Architecture (Ports & Adapters):
- **Domain**: Pure business models, repository interfaces (ports), domain services
- **Application**: Use cases, DTOs, orchestration
- **Infrastructure**: JPA adapters, S3 adapter, security config
- **API**: REST controllers (adapters in)

## Consequences
- Domain layer has zero framework dependencies
- Repository interfaces in domain, implementations in infrastructure
- Domain services can be unit tested without Spring context
- Slightly more files due to port/adapter separation
