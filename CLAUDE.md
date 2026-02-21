# LexSecura - CRA Reporting & Evidence Manager

## Project Overview
SaaS B2B for EU Cyber Resilience Act (2024/2847) compliance management.
Allows organizations to: manage digital products, assess CRA compliance, collect evidences, track conformity.

## Architecture
- **Backend**: Java 21 + Spring Boot 3.3 + Maven, Hexagonal (Ports & Adapters)
- **Frontend**: React 18 + TypeScript + Vite 5 + Tailwind CSS + TanStack Query
- **Database**: PostgreSQL 16 with Flyway migrations
- **Auth**: Keycloak 24 (OIDC), 3 roles: ADMIN, COMPLIANCE_MANAGER, CONTRIBUTOR
- **Storage**: S3 (MinIO local) for evidence files, SHA-256 integrity
- **Multi-tenancy**: Column-based (org_id) extracted from JWT

## Key Directories
- `backend/src/main/java/com/lexsecura/domain/` - Domain models, repos (ports), domain services
- `backend/src/main/java/com/lexsecura/application/` - DTOs, application services, ports
- `backend/src/main/java/com/lexsecura/infrastructure/` - JPA, S3, security adapters
- `backend/src/main/java/com/lexsecura/api/` - REST controllers, error handling
- `frontend/src/` - React application

## Commands
```bash
# Backend
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd backend && ./mvnw test

# Frontend
cd frontend && npm install && npm run dev

# Infrastructure
cd infra && docker-compose up -d postgres keycloak minio
```

## Conventions
- REST API: `/api/v1/*`, Problem+JSON errors (RFC 9457)
- Flyway migrations: `V001__description.sql`
- Multi-tenant: every query MUST filter by org_id
- Domain logic in domain layer, not controllers
- Assessment workflow: DRAFT -> IN_REVIEW -> APPROVED/REJECTED
