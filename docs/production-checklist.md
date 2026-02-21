# Production Deployment Checklist

## Environment Variables

| Variable | Description | Required | Example |
|----------|-------------|----------|---------|
| `DB_HOST` | PostgreSQL host | Yes | `db.example.com` |
| `DB_PORT` | PostgreSQL port | No (default: 5432) | `5432` |
| `DB_NAME` | Database name | Yes | `lexsecura` |
| `DB_USER` | Database username | Yes | `lexsecura_prod` |
| `DB_PASSWORD` | Database password | Yes | (secret) |
| `KEYCLOAK_ISSUER_URI` | Keycloak realm issuer URI | Yes | `https://auth.example.com/realms/lexsecura` |
| `KEYCLOAK_JWK_URI` | Keycloak JWKS endpoint | Yes | `https://auth.example.com/realms/lexsecura/protocol/openid-connect/certs` |
| `S3_ENDPOINT` | S3-compatible storage endpoint | Yes | `https://s3.amazonaws.com` |
| `S3_ACCESS_KEY` | S3 access key | Yes | (secret) |
| `S3_SECRET_KEY` | S3 secret key | Yes | (secret) |
| `S3_BUCKET` | S3 bucket name | Yes | `lexsecura-evidences` |
| `S3_REGION` | S3 region | No (default: us-east-1) | `eu-west-1` |
| `CORS_ORIGINS` | Allowed CORS origins | Yes | `https://app.example.com` |
| `GITLAB_WEBHOOK_SECRET` | GitLab webhook secret | If using GitLab | (secret) |
| `RATE_LIMIT_ENABLED` | Enable rate limiting | No (default: false) | `true` |
| `RATE_LIMIT_RPM` | Requests per minute per IP | No (default: 120) | `60` |
| `SERVER_PORT` | Server port | No (default: 8080) | `8080` |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OpenTelemetry collector endpoint | If using tracing | `http://otel-collector:4318/v1/traces` |
| `OTEL_SAMPLING_PROBABILITY` | Trace sampling rate | No (default: 0.1) | `1.0` |

## Pre-Deployment Checklist

### Security
- [ ] All secrets stored in vault/secret manager (never in env files or code)
- [ ] `RATE_LIMIT_ENABLED=true` with appropriate RPM
- [ ] CORS origins restricted to production domain only
- [ ] Keycloak realm configured with strong password policies
- [ ] Database user has minimal required permissions (no superuser)
- [ ] S3 bucket has encryption at rest enabled
- [ ] TLS/HTTPS enabled on all endpoints (load balancer or ingress)
- [ ] Security headers verified (X-Frame-Options, HSTS, CSP)

### Database
- [ ] PostgreSQL 16+ with connection pooling (PgBouncer recommended)
- [ ] Automated daily backups configured
- [ ] Point-in-time recovery (PITR) enabled
- [ ] Connection pool sizing: max 30, min idle 10
- [ ] Flyway migrations applied successfully

### Observability
- [ ] Prometheus scraping `/actuator/prometheus`
- [ ] Health check monitoring on `/actuator/health`
- [ ] Log aggregation configured (ELK, Loki, CloudWatch)
- [ ] JSON log format active (non-local profile)
- [ ] Alert rules for: error rate > 1%, response time p99 > 2s, disk > 80%
- [ ] OpenTelemetry collector configured (if tracing enabled)

### Backup & Recovery
- [ ] Database: daily pg_dump + WAL archiving for PITR
- [ ] S3 evidences: versioning enabled on bucket
- [ ] Keycloak: realm export backed up
- [ ] Recovery procedure documented and tested

### Performance
- [ ] JVM heap sized: `-Xms512m -Xmx2g` (adjust per load)
- [ ] Tomcat max threads: 200 (adjust per CPU cores)
- [ ] Database connection pool: max 30
- [ ] S3 upload limit: 50MB
- [ ] Rate limit: 60 req/min per IP

### Deployment
- [ ] Rolling deployment strategy (zero downtime)
- [ ] Health check endpoint configured in orchestrator
- [ ] Graceful shutdown enabled (`server.shutdown=graceful`)
- [ ] Container resource limits set (CPU + memory)
- [ ] Log rotation configured (if file-based)

## Monitoring Endpoints

| Endpoint | Auth | Description |
|----------|------|-------------|
| `/actuator/health` | Public | Application health status |
| `/actuator/info` | Public | Application info |
| `/actuator/prometheus` | Public | Prometheus metrics |
| `/swagger-ui.html` | Public | API documentation |
| `/api/v1/audit/verify` | JWT | Audit trail integrity check |

## Log Rotation (if file-based)

For production deployments using file logging, configure logrotate:
```
/var/log/lexsecura/*.log {
    daily
    rotate 30
    compress
    delaycompress
    notifempty
    copytruncate
}
```

Standard deployments use stdout JSON logging collected by the container orchestrator.
