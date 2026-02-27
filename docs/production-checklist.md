# Checklist de deploiement en production

## Variables d'environnement

SRPDesk utilise un fichier `.env` pour centraliser toute la configuration. Copiez `.env.example` en `.env` et adaptez les valeurs a votre infrastructure.

```bash
cp .env.example .env
# Editez .env avec vos valeurs
nano .env
```

### Secrets obligatoires (REQUIRED)

Ces variables n'ont pas de valeur par defaut sure — elles **doivent** etre modifiees :

| Variable | Description |
|----------|-------------|
| `POSTGRES_PASSWORD` | Mot de passe PostgreSQL |
| `KEYCLOAK_ADMIN_PASSWORD` | Mot de passe admin Keycloak |
| `MINIO_ROOT_PASSWORD` | Mot de passe MinIO (S3) |
| `DB_PASSWORD` | Mot de passe BDD backend (= POSTGRES_PASSWORD) |
| `S3_SECRET_KEY` | Secret S3 backend (= MINIO_ROOT_PASSWORD) |
| `MONITORING_PASSWORD_HASH` | Hash bcrypt du mot de passe monitoring Prometheus |

### Domaines (REQUIRED)

| Variable | Default | Description |
|----------|---------|-------------|
| `APP_DOMAIN` | `app.srpdesk.com` | Domaine de l'application |
| `AUTH_DOMAIN` | `auth.srpdesk.com` | Domaine Keycloak (OIDC) |
| `LANDING_DOMAIN` | `www.srpdesk.com` | Domaine du site vitrine |
| `MONITORING_DOMAIN` | `monitoring.srpdesk.com` | Domaine Prometheus (protege basicauth) |
| `ACME_EMAIL` | `admin@srpdesk.com` | Email Let's Encrypt (certificats TLS) |

### Infrastructure Docker

| Variable | Default | Description |
|----------|---------|-------------|
| `IMAGE_PREFIX` | `ghcr.io/your-org/srpdesk` | Prefixe des images Docker |
| `IMAGE_TAG` | `latest` | Tag des images |
| `POSTGRES_DB` | `srpdesk` | Nom de la base de donnees |
| `POSTGRES_USER` | `srpdesk` | Utilisateur PostgreSQL |
| `KEYCLOAK_ADMIN` | `admin` | Utilisateur admin Keycloak |
| `MINIO_ROOT_USER` | `minioadmin` | Utilisateur MinIO |

### Backend — Base de donnees

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `postgres` | Hote PostgreSQL (nom du service Docker) |
| `DB_PORT` | `5432` | Port PostgreSQL |
| `DB_NAME` | `srpdesk` | Nom de la base |
| `DB_USER` | `srpdesk` | Utilisateur BDD |
| `DB_POOL_MAX` | `30` | Taille max du pool Hikari |
| `DB_POOL_MIN` | `10` | Connexions idle minimum |
| `DB_POOL_CONNECTION_TIMEOUT` | `20000` | Timeout connexion (ms) |

### Backend — Authentification OIDC

| Variable | Default | Description |
|----------|---------|-------------|
| `KEYCLOAK_ISSUER_URI` | — | URI issuer du realm Keycloak |
| `KEYCLOAK_JWK_URI` | — | Endpoint JWKS Keycloak |

### Backend — Stockage S3

| Variable | Default | Description |
|----------|---------|-------------|
| `S3_ENDPOINT` | `http://minio:9000` | Endpoint S3 (MinIO ou AWS) |
| `S3_ACCESS_KEY` | `minioadmin` | Access key S3 |
| `S3_SECRET_KEY` | — | Secret key S3 |
| `S3_BUCKET` | `evidences` | Nom du bucket |
| `S3_REGION` | `us-east-1` | Region S3 |

### Backend — Serveur & Performance

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | Port du serveur backend |
| `CORS_ORIGINS` | — | Origines CORS autorisees |
| `APP_NAME` | `srpdesk` | Nom dans les metriques |
| `MAX_FILE_SIZE` | `50MB` | Taille max par fichier uploade |
| `MAX_REQUEST_SIZE` | `55MB` | Taille max requete multipart |
| `TOMCAT_MAX_THREADS` | `200` | Threads max Tomcat |
| `TOMCAT_ACCEPT_COUNT` | `100` | File d'attente Tomcat |
| `JAVA_OPTS` | *(vide)* | Options JVM (ex: `-Xms512m -Xmx1g`) |

### Backend — Rate limiting

| Variable | Default | Description |
|----------|---------|-------------|
| `RATE_LIMIT_ENABLED` | `true` | Activer le rate limiting |
| `RATE_LIMIT_RPM` | `60` | Requetes/min API generale |
| `RATE_LIMIT_CVD_RPM` | `5` | Requetes/min endpoint CVD public |

### Backend — Email / SMTP

| Variable | Default | Description |
|----------|---------|-------------|
| `EMAIL_ENABLED` | `false` | Activer l'envoi d'emails |
| `EMAIL_FROM` | `noreply@srpdesk.com` | Adresse expediteur |
| `SMTP_HOST` | *(vide)* | Serveur SMTP |
| `SMTP_PORT` | `587` | Port SMTP |
| `SMTP_USERNAME` | *(vide)* | Utilisateur SMTP |
| `SMTP_PASSWORD` | *(vide)* | Mot de passe SMTP |
| `SMTP_AUTH` | `false` | Authentification SMTP requise |
| `SMTP_STARTTLS` | `true` | Activer STARTTLS |

### Backend — CRA & Conformite

| Variable | Default | Description |
|----------|---------|-------------|
| `CRA_NOTIFICATION_CHECK_MS` | `900000` | Intervalle verif. notifications (15 min) |
| `CRA_ESCALATION_CHECK_MS` | `300000` | Intervalle verif. escalation (5 min) |
| `CRA_ALERT_EMAIL` | *(vide)* | Email alertes CRA |
| `CRA_ESCALATION_EMAIL` | *(vide)* | Email escalation CRA |
| `CVD_CONTACT_EMAIL` | `security@srpdesk.com` | Email contact securite (security.txt) |
| `SUPPORT_MONITOR_WARNING_DAYS` | `90` | Jours avant fin de support pour alerter |

### Backend — ENISA & CSIRT

| Variable | Default | Description |
|----------|---------|-------------|
| `ENISA_ENABLED` | `false` | Activer connecteur ENISA SRP |
| `ENISA_BASE_URL` | `https://srp.enisa.europa.eu` | URL API ENISA |
| `ENISA_API_KEY` | *(vide)* | Cle API ENISA |
| `ENISA_TIMEOUT` | `30` | Timeout (secondes) |
| `CSIRT_ENABLED` | `false` | Activer notification CSIRT parallele |
| `CSIRT_BASE_URL` | *(vide)* | URL API CSIRT nationale |
| `CSIRT_API_KEY` | *(vide)* | Cle API CSIRT |
| `CSIRT_TIMEOUT` | `30` | Timeout (secondes) |
| `CSIRT_COUNTRY_CODE` | `FR` | Code pays ISO |

### Backend — Monitoring vulnerabilites

| Variable | Default | Description |
|----------|---------|-------------|
| `MONITORING_INTERVAL_MS` | `21600000` | Intervalle scan (6h) |
| `OSV_BASE_URL` | `https://api.osv.dev` | URL API OSV |
| `OSV_BATCH_SIZE` | `100` | Taille des lots |
| `OSV_RATE_LIMIT_MS` | `1000` | Pause entre lots (ms) |

### Backend — IA / Ollama

| Variable | Default | Description |
|----------|---------|-------------|
| `AI_ENABLED` | `false` | Activer l'assistant IA |
| `OLLAMA_BASE_URL` | `http://ollama:11434` | URL serveur Ollama |
| `OLLAMA_MODEL` | `phi3.5` | Modele LLM |
| `OLLAMA_TEMPERATURE` | `0.2` | Temperature (0.0–1.0) |
| `OLLAMA_TOP_P` | `0.9` | Top-P sampling |
| `OLLAMA_NUM_CTX` | `4096` | Contexte (tokens) |
| `OLLAMA_TIMEOUT` | `120` | Timeout (secondes) |
| `AI_RATE_LIMIT_RPM` | `10` | Requetes IA/min |

### Backend — Integrations & API Keys

| Variable | Default | Description |
|----------|---------|-------------|
| `GITHUB_WEBHOOK_SECRET` | *(vide)* | Secret HMAC-SHA256 webhook GitHub |
| `GITLAB_WEBHOOK_SECRET` | *(vide)* | Secret webhook GitLab |
| `API_KEY_PREFIX` | `srpd_` | Prefixe cles API generees |
| `API_KEY_LENGTH` | `20` | Longueur aleatoire (octets) |
| `API_KEY_SCOPES` | `ci:sbom` | Scopes par defaut |
| `SBOM_MAX_SIZE_MB` | `10` | Taille max SBOM (MB) |

### Backend — Feature flags

| Variable | Default | Description |
|----------|---------|-------------|
| `EXTRAS_REQUIREMENTS_ENABLED` | `false` | Module CRA checklist |
| `EXTRAS_QUESTIONNAIRE_ENABLED` | `false` | Module questionnaire IA |

### Backend — Observabilite

| Variable | Default | Description |
|----------|---------|-------------|
| `OTEL_SAMPLING_PROBABILITY` | `1.0` | Probabilite sampling traces |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318/v1/traces` | Endpoint OTLP collector |

### Frontend (variables build-time)

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_KEYCLOAK_URL` | — | URL publique Keycloak |
| `VITE_KEYCLOAK_REALM` | `srpdesk` | Realm Keycloak |
| `VITE_KEYCLOAK_CLIENT_ID` | `frontend` | Client ID OIDC |
| `VITE_API_URL` | `/api/v1` | URL de base API |
| `VITE_FEATURE_REQUIREMENTS` | `false` | Module CRA checklist (frontend) |
| `VITE_FEATURE_AI_QUESTIONNAIRE` | `false` | Module questionnaire IA (frontend) |

### Reverse proxy Caddy

| Variable | Default | Description |
|----------|---------|-------------|
| `MONITORING_USER` | `admin` | Utilisateur basicauth Prometheus |
| `MONITORING_PASSWORD_HASH` | — | Hash bcrypt du mot de passe |

### Docker Compose operationnel

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_MEMORY_LIMIT` | `512m` | Limite memoire PostgreSQL |
| `KEYCLOAK_MEMORY_LIMIT` | `768m` | Limite memoire Keycloak |
| `MINIO_MEMORY_LIMIT` | `256m` | Limite memoire MinIO |
| `BACKEND_MEMORY_LIMIT` | `768m` | Limite memoire backend |
| `CADDY_MEMORY_LIMIT` | `128m` | Limite memoire Caddy |
| `PROMETHEUS_MEMORY_LIMIT` | `256m` | Limite memoire Prometheus |
| `BACKUP_MEMORY_LIMIT` | `128m` | Limite memoire backup |

### Sauvegarde & Monitoring

| Variable | Default | Description |
|----------|---------|-------------|
| `BACKUP_CRON` | `0 2 * * *` | Cron sauvegarde PostgreSQL (defaut: 2h00) |
| `BACKUP_RETENTION_DAYS` | `30` | Retention des sauvegardes (jours) |
| `PROMETHEUS_RETENTION` | `15d` | Retention des metriques Prometheus |

---

## Deploiement rapide

```bash
# 1. Copier et configurer l'environnement
cp .env.example .env
nano .env    # Modifier les REQUIRED + domaines + CORS

# 2. Generer le hash bcrypt pour le monitoring
docker run --rm caddy:2-alpine caddy hash-password --plaintext 'votre-mot-de-passe'
# Copier le resultat dans MONITORING_PASSWORD_HASH du .env

# 3. Lancer la stack
docker compose -f docker-compose.prod.yml up -d

# 4. Verifier que tout est healthy
docker compose -f docker-compose.prod.yml ps
```

> Guide complet : voir `docs/deploiement-serveur-dedie.md`

---

## Checklist pre-deploiement

### Securite

- [ ] Tous les secrets generes avec `openssl rand -base64 32` (pas de `changeme`)
- [ ] `RATE_LIMIT_ENABLED=true` avec RPM adapte a votre charge
- [ ] `CORS_ORIGINS` restreint au domaine de production uniquement
- [ ] Keycloak configure avec des politiques de mots de passe strictes
- [ ] Utilisateur BDD avec permissions minimales (pas de superuser)
- [ ] Bucket S3 avec chiffrement au repos active
- [ ] HTTPS via Caddy (inclus dans docker-compose.prod.yml — certificats Let's Encrypt auto)
- [ ] Headers securite verifies (HSTS, X-Frame-Options, X-Content-Type-Options — configures dans Caddyfile)
- [ ] Webhook secrets configures (`GITHUB_WEBHOOK_SECRET` / `GITLAB_WEBHOOK_SECRET`)
- [ ] `CVD_CONTACT_EMAIL` configure avec l'email securite de l'organisation
- [ ] Firewall : seuls ports 22, 80, 443 ouverts

### Base de donnees

- [ ] PostgreSQL 16+ avec connection pooling Hikari (inclus)
- [ ] Sauvegardes quotidiennes automatisees (conteneur `backup` inclus dans docker-compose)
- [ ] `BACKUP_CRON` et `BACKUP_RETENTION_DAYS` configures
- [ ] Pool Hikari dimensionne : `DB_POOL_MAX` et `DB_POOL_MIN` adaptes a la charge
- [ ] Migrations Flyway appliquees avec succes

### Email

- [ ] `EMAIL_ENABLED=true` avec `SMTP_HOST` configure
- [ ] `CRA_ALERT_EMAIL` et `CRA_ESCALATION_EMAIL` configures pour les notifications CRA
- [ ] Email d'expediteur (`EMAIL_FROM`) avec SPF/DKIM configures

### Observabilite

- [ ] Prometheus scraping backend + PostgreSQL + Caddy (inclus dans docker-compose)
- [ ] Prometheus accessible sur `https://monitoring.votredomaine.com` (basicauth)
- [ ] Regles d'alerte configurees (`infra/prometheus/alert-rules.yml`)
- [ ] Health check monitoring sur `/actuator/health`
- [ ] Format JSON des logs actif (profil non-local)
- [ ] OpenTelemetry collector configure (`OTEL_EXPORTER_OTLP_ENDPOINT`) — optionnel

### Sauvegarde & Restauration

- [ ] Conteneur `backup` operationnel (verifier `docker compose logs backup`)
- [ ] Copie des backups vers un stockage externe (hors serveur)
- [ ] S3 evidences : versioning active sur le bucket
- [ ] Procedure de restauration testee (voir `docs/deploiement-serveur-dedie.md`)

### Performance

- [ ] JVM dimensionnee via `JAVA_OPTS` (ex: `-Xms512m -Xmx2g`)
- [ ] `TOMCAT_MAX_THREADS` ajuste selon les CPU (200 par defaut)
- [ ] `DB_POOL_MAX` ajuste selon la charge (30 par defaut)
- [ ] `MAX_FILE_SIZE` adapte a vos besoins (50MB par defaut)
- [ ] `RATE_LIMIT_RPM` adapte a la charge attendue (60/min par defaut)
- [ ] Limites memoire Docker configurees (`BACKEND_MEMORY_LIMIT`, etc.)

### Deploiement

- [ ] Strategie de deploiement rolling (zero downtime)
- [ ] Health check configure dans l'orchestrateur
- [ ] Graceful shutdown active (`server.shutdown=graceful`)
- [ ] Limites de ressources conteneurs configurees (CPU + memoire)
- [ ] Rotation des logs configuree (si file-based)

### CRA & Conformite

- [ ] `ENISA_ENABLED=true` avec cle API si connexion ENISA requise
- [ ] `CSIRT_ENABLED=true` avec configuration CSIRT nationale si Art. 14 applicable
- [ ] `SUPPORT_MONITOR_WARNING_DAYS` configure (90 jours par defaut)
- [ ] `MONITORING_INTERVAL_MS` adapte a la frequence souhaitee (6h par defaut)

---

## Services Docker en production

| Service | Image | Role |
|---------|-------|------|
| `caddy` | `caddy:2-alpine` | Reverse proxy HTTPS, TLS auto (Let's Encrypt), HTTP/3 |
| `postgres` | `postgres:16` | Base de donnees principale |
| `keycloak` | `quay.io/keycloak/keycloak:24.0` | Authentification OIDC |
| `minio` | `minio/minio:latest` | Stockage S3 (evidences) |
| `backend` | `ghcr.io/.../srpdesk-backend` | API Spring Boot |
| `frontend` | `ghcr.io/.../srpdesk-frontend` | Application React + nginx |
| `landing` | `ghcr.io/.../srpdesk-landing` | Site vitrine Next.js + nginx |
| `prometheus` | `prom/prometheus:latest` | Collecte metriques + alertes |
| `postgres-exporter` | `prometheuscommunity/postgres-exporter` | Metriques PostgreSQL |
| `backup` | `postgres:16` | Sauvegarde PostgreSQL automatisee (cron) |

## Endpoints de monitoring

| Endpoint | Auth | Description |
|----------|------|-------------|
| `/actuator/health` | Public | Etat de sante de l'application |
| `/actuator/info` | Public | Informations de l'application |
| `/actuator/prometheus` | Public | Metriques Prometheus |
| `/swagger-ui.html` | Public | Documentation API |
| `/api/v1/audit/verify` | JWT | Verification d'integrite de la piste d'audit |
| `/.well-known/security.txt` | Public | Contact securite CVD |
| `monitoring.votredomaine.com` | BasicAuth | Interface Prometheus |

---

## Rotation des logs (si file-based)

Pour les deploiements avec journalisation fichier :

```
/var/log/srpdesk/*.log {
    daily
    rotate 30
    compress
    delaycompress
    notifempty
    copytruncate
}
```

Les deploiements standard utilisent la journalisation stdout JSON collectee par l'orchestrateur de conteneurs.

---

## Dimensionnement recommande

| Charge | CPU | RAM | DB_POOL_MAX | TOMCAT_MAX_THREADS |
|--------|-----|-----|-------------|-------------------|
| < 10 utilisateurs | 2 vCPU | 4 Go | 10 | 50 |
| 10-50 utilisateurs | 4 vCPU | 8 Go | 30 | 200 |
| 50-200 utilisateurs | 8 vCPU | 16 Go | 50 | 400 |
| > 200 utilisateurs | 16 vCPU | 32 Go | 80 | 600 |

Exemple de `JAVA_OPTS` pour 50 utilisateurs :
```
JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```
