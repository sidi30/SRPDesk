# Run LexSecura Locally in 5 Minutes

## Prerequisites

- Docker Desktop (or Docker Engine + Docker Compose)
- Java 21 (e.g., Eclipse Temurin)
- Node.js 20+

## Quick Start

```bash
# 1. Clone and enter project
git clone <repo-url> && cd lexsecura

# 2. First-time setup
make init

# 3. Start backend (terminal 1)
make backend-run

# 4. Start frontend (terminal 2)
make frontend-dev
```

Or manually:

```bash
# 1. Start infrastructure
cd infra && docker compose up -d postgres keycloak minio minio-init

# 2. Wait for services to be healthy
cd infra && docker compose ps

# 3. Start backend (terminal 1)
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 4. Start frontend (terminal 2)
cd frontend && npm install && npm run dev
```

## Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| Frontend | http://localhost:5173 | Keycloak login (see below) |
| Backend API | http://localhost:8080 | JWT required |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | - |
| Keycloak Admin | http://localhost:8180 | admin / admin |
| MinIO Console | http://localhost:9001 | minioadmin / minioadmin |
| PostgreSQL | localhost:5433 | lexsecura / lexsecura |
| Prometheus metrics | http://localhost:8080/actuator/prometheus | - |

## Test Accounts

The following users are pre-configured in the `lexsecura` Keycloak realm:

| Email | Password | Role |
|-------|----------|------|
| admin@lexsecura.com | admin123 | ADMIN |
| manager@lexsecura.com | manager123 | COMPLIANCE_MANAGER |
| contributor@lexsecura.com | contrib123 | CONTRIBUTOR |
| admin-org2@lexsecura.com | admin123 | ADMIN (different org) |

## Creating Additional Users

1. Go to http://localhost:8180
2. Login: admin / admin
3. Select realm `lexsecura`
4. Users > Add user
5. Set username, email
6. Credentials tab > Set password (temporary: off)
7. Role mapping > Assign role (ADMIN, COMPLIANCE_MANAGER, or CONTRIBUTOR)
8. Add custom attribute: `org_id` = any UUID (e.g. `00000000-0000-0000-0000-000000000001`)

## Common Commands

```bash
make up             # Start infra only
make down           # Stop everything
make down-volumes   # Stop and delete all data
make test           # Run all tests
make backend-test   # Backend tests only
make logs           # Follow all logs
make ps             # Show running services
```

## Troubleshooting

**Keycloak takes long to start**: Normal on first run (30-60s). Check: `docker logs lexsecura-keycloak`

**Port conflicts**: Ensure 5433, 8080, 8180, 9000, 9001, 5173 are available.

**Backend can't connect to DB**: Wait for postgres healthcheck. Run `make ps` to verify. Note: local profile uses port **5433** (not 5432).

**Login error "please login again"**: Ensure Keycloak is fully started and the `lexsecura` realm is imported. Try restarting Keycloak: `cd infra && docker compose restart keycloak`.
