# Guide de deploiement SRPDesk — Serveur dedie (100% Docker)

Deploiement complet de SRPDesk sur un serveur Linux dedie. Tout tourne dans Docker :
HTTPS automatique (Caddy + Let's Encrypt), monitoring (Prometheus), backups automatises.

---

## Table des matieres

1. [Pre-requis serveur](#1-pre-requis-serveur)
2. [Preparation du serveur](#2-preparation-du-serveur)
3. [Installation Docker](#3-installation-docker)
4. [Deploiement initial](#4-deploiement-initial)
5. [Configuration DNS](#5-configuration-dns)
6. [Configuration Keycloak](#6-configuration-keycloak)
7. [Sauvegardes](#7-sauvegardes)
8. [Mise a jour / Nouveau deploiement](#8-mise-a-jour--nouveau-deploiement)
9. [Monitoring & Logs](#9-monitoring--logs)
10. [Rollback](#10-rollback)
11. [Troubleshooting](#11-troubleshooting)
12. [Checklist finale](#12-checklist-finale)

---

## 1. Pre-requis serveur

### Materiel minimum

| Charge attendue | CPU | RAM | Disque |
|-----------------|-----|-----|--------|
| < 10 utilisateurs | 2 vCPU | 4 Go | 40 Go SSD |
| 10-50 utilisateurs | 4 vCPU | 8 Go | 80 Go SSD |
| 50-200 utilisateurs | 8 vCPU | 16 Go | 160 Go SSD |

### Logiciels requis

- **OS** : Ubuntu 22.04/24.04 LTS ou Debian 12
- **Docker** : 24+ avec Docker Compose v2
- **Git** : 2.x
- Un nom de domaine pointe vers le serveur (3 sous-domaines)

### Ports a ouvrir (firewall)

| Port | Protocole | Usage |
|------|-----------|-------|
| 22 | TCP | SSH |
| 80 | TCP | HTTP (redirect auto vers HTTPS par Caddy) |
| 443 | TCP + UDP | HTTPS + HTTP/3 |

> Tous les autres ports (5432, 8080, 9000, 9090, etc.) restent **internes au reseau Docker**. Ne PAS les exposer publiquement.

---

## 2. Preparation du serveur

```bash
# Mettre a jour le systeme
sudo apt update && sudo apt upgrade -y

# Creer un utilisateur dedie
sudo adduser srpdesk
sudo usermod -aG sudo srpdesk
su - srpdesk

# Creer le repertoire de deploiement
sudo mkdir -p /opt/srpdesk
sudo chown srpdesk:srpdesk /opt/srpdesk

# Configurer le firewall
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 443/udp    # HTTP/3
sudo ufw enable
```

---

## 3. Installation Docker

```bash
# Installer Docker
curl -fsSL https://get.docker.com | sudo sh

# Ajouter l'utilisateur au groupe docker
sudo usermod -aG docker srpdesk
newgrp docker

# Verifier
docker --version        # Docker 24+
docker compose version  # Docker Compose v2
```

---

## 4. Deploiement initial

### 4.1 Recuperer les fichiers

```bash
cd /opt/srpdesk
git clone https://github.com/VOTRE-ORG/srpdesk.git .
```

Structure necessaire sur le serveur :

```
/opt/srpdesk/
  docker-compose.prod.yml
  .env
  infra/
    caddy/
      Caddyfile              # Reverse proxy HTTPS
    postgres/
      init.sql               # Extension pgcrypto
    keycloak/
      realm-export.json      # Realm Keycloak pre-configure
    prometheus/
      prometheus.yml         # Config scraping metriques
      alert-rules.yml        # Regles d'alerte
    backup/
      backup.sh              # Script sauvegarde PostgreSQL
```

### 4.2 Configurer l'environnement

```bash
cp .env.example .env
nano .env
```

**Etape 1 — Generer les secrets :**

```bash
# Generer 3 mots de passe forts
echo "POSTGRES_PASSWORD=$(openssl rand -base64 32)"
echo "KEYCLOAK_ADMIN_PASSWORD=$(openssl rand -base64 32)"
echo "MINIO_ROOT_PASSWORD=$(openssl rand -base64 32)"

# Generer le hash bcrypt pour le monitoring Prometheus
docker run --rm caddy:2-alpine caddy hash-password --plaintext 'votre-mot-de-passe-monitoring'
# Copier le resultat dans MONITORING_PASSWORD_HASH
```

**Etape 2 — Variables OBLIGATOIRES a modifier dans `.env` :**

```bash
# --- Images Docker ---
IMAGE_PREFIX=ghcr.io/VOTRE-ORG/srpdesk
IMAGE_TAG=v1.0.0                              # JAMAIS "latest" en prod

# --- Domaines ---
APP_DOMAIN=app.votredomaine.com               # Domaine de l'application
AUTH_DOMAIN=auth.votredomaine.com             # Domaine Keycloak
LANDING_DOMAIN=www.votredomaine.com           # Domaine site vitrine
MONITORING_DOMAIN=monitoring.votredomaine.com # Domaine Prometheus
ACME_EMAIL=admin@votredomaine.com             # Email Let's Encrypt

# --- Secrets (coller les valeurs generees ci-dessus) ---
POSTGRES_PASSWORD=<secret-genere>
KEYCLOAK_ADMIN_PASSWORD=<secret-genere>
MINIO_ROOT_PASSWORD=<secret-genere>
DB_PASSWORD=<meme-que-POSTGRES_PASSWORD>
S3_SECRET_KEY=<meme-que-MINIO_ROOT_PASSWORD>

# --- Monitoring ---
MONITORING_PASSWORD_HASH=<hash-bcrypt-genere>

# --- OIDC (adapter aux domaines) ---
KEYCLOAK_ISSUER_URI=https://auth.votredomaine.com/realms/srpdesk
KEYCLOAK_JWK_URI=http://keycloak:8080/realms/srpdesk/protocol/openid-connect/certs
CORS_ORIGINS=https://app.votredomaine.com

# --- Frontend ---
VITE_KEYCLOAK_URL=https://auth.votredomaine.com
```

### 4.3 Se connecter au registry

```bash
# Si les images sont sur GitHub Container Registry (GHCR)
echo "VOTRE_GITHUB_TOKEN" | docker login ghcr.io -u VOTRE_USERNAME --password-stdin
```

### 4.4 Lancer la stack

```bash
cd /opt/srpdesk

# Tirer les images
docker compose -f docker-compose.prod.yml pull

# Demarrer en arriere-plan
docker compose -f docker-compose.prod.yml up -d

# Suivre les logs du demarrage (Ctrl+C pour quitter)
docker compose -f docker-compose.prod.yml logs -f
```

### 4.5 Verifier que tout est healthy

```bash
docker compose -f docker-compose.prod.yml ps
```

Resultat attendu :

```
NAME                STATUS
caddy               running (healthy)
postgres            running (healthy)
keycloak            running (healthy)
minio               running (healthy)
backend             running (healthy)
frontend            running
landing             running
prometheus          running
postgres-exporter   running
backup              running
```

Tester les endpoints :

```bash
# Health check backend (via Caddy HTTPS)
curl -s https://app.votredomaine.com/actuator/health

# Keycloak accessible
curl -s https://auth.votredomaine.com/realms/srpdesk/.well-known/openid-configuration | head -1

# Prometheus (avec basicauth)
curl -su admin:votre-mot-de-passe https://monitoring.votredomaine.com/api/v1/targets
```

---

## 5. Configuration DNS

Configurez ces enregistrements DNS chez votre registrar **avant** de lancer la stack (Caddy a besoin que les DNS pointent vers le serveur pour obtenir les certificats) :

| Type | Nom | Valeur |
|------|-----|--------|
| A | `app.votredomaine.com` | IP du serveur |
| A | `auth.votredomaine.com` | IP du serveur |
| A | `www.votredomaine.com` | IP du serveur |
| A | `monitoring.votredomaine.com` | IP du serveur |

> Caddy obtient et renouvelle automatiquement les certificats Let's Encrypt. Aucune action manuelle pour le TLS.

---

## 6. Configuration Keycloak

### Premiere connexion

1. Acceder a `https://auth.votredomaine.com`
2. Se connecter avec `admin` / le mot de passe defini dans `KEYCLOAK_ADMIN_PASSWORD`
3. Le realm `srpdesk` est importe automatiquement depuis `realm-export.json`

### Verifications post-import

- [ ] Le realm `srpdesk` existe
- [ ] Le client `frontend` existe avec les bonnes redirect URIs
- [ ] Les roles `ADMIN`, `COMPLIANCE_MANAGER`, `CONTRIBUTOR` existent
- [ ] Les scopes `openid`, `profile`, `email` sont definis

### Adapter les URLs du client frontend

Dans Keycloak > Realm `srpdesk` > Clients > `frontend` :

- **Root URL** : `https://app.votredomaine.com`
- **Valid Redirect URIs** : `https://app.votredomaine.com/*`
- **Web Origins** : `https://app.votredomaine.com`

### Creer le premier utilisateur

Dans le realm `srpdesk` > Users > Add user :
1. Creer l'utilisateur avec email
2. Onglet Credentials : definir un mot de passe
3. Onglet Role Mappings : assigner le role `ADMIN`

---

## 7. Sauvegardes

### Sauvegarde automatique (incluse dans Docker)

Le conteneur `backup` execute automatiquement un `pg_dump` selon le cron defini dans `.env` :

```bash
BACKUP_CRON=0 2 * * *         # Tous les jours a 2h00
BACKUP_RETENTION_DAYS=30       # Garde les 30 derniers jours
```

Les dumps sont stockes dans le volume Docker `srpdesk-backups`.

### Verifier les sauvegardes

```bash
# Lister les backups
docker compose -f docker-compose.prod.yml exec backup ls -lh /backups/

# Voir les logs du backup
docker compose -f docker-compose.prod.yml logs backup
```

### Copier un backup hors du conteneur

```bash
# Copier le dernier backup sur l'hote
docker compose -f docker-compose.prod.yml cp backup:/backups/ /opt/srpdesk/backups-local/
```

### Restauration

```bash
# 1. Stopper le backend pendant la restauration
docker compose -f docker-compose.prod.yml stop backend

# 2. Copier le dump dans le conteneur postgres
docker compose -f docker-compose.prod.yml cp \
    /opt/srpdesk/backups-local/srpdesk_XXXXXXXX_XXXXXX.dump postgres:/tmp/restore.dump

# 3. Restaurer
docker compose -f docker-compose.prod.yml exec postgres \
    pg_restore -U srpdesk -d srpdesk --clean --if-exists /tmp/restore.dump

# 4. Redemarrer le backend
docker compose -f docker-compose.prod.yml start backend
```

### Sauvegarde externe (recommande)

Pour copier les backups vers un stockage externe (S3, FTP, etc.) :

```bash
# Ajouter un cron sur l'hote
crontab -e
```

```cron
# Copier les backups Docker vers /opt/srpdesk/backups puis sync vers S3
30 2 * * * docker compose -f /opt/srpdesk/docker-compose.prod.yml cp backup:/backups/ /opt/srpdesk/backups-external/ 2>&1 | logger -t srpdesk-backup
```

---

## 8. Mise a jour / Nouveau deploiement

### 8.1 Via GitHub Actions (recommande)

Le pipeline CI/CD est deja configure :

1. **Creer un tag de release** sur votre machine de dev :
   ```bash
   git tag v1.2.0
   git push origin v1.2.0
   ```

2. Le workflow **Release** se declenche automatiquement :
   - Execute les tests backend + frontend
   - Build les 3 images Docker (backend, frontend, landing)
   - Push vers GHCR avec les tags `v1.2.0` + `latest`
   - Cree une GitHub Release avec changelog

3. **Declencher le deploiement** :
   - GitHub > Actions > Deploy > Run workflow
   - Entrer la version : `v1.2.0`
   - Le workflow SSH dans le serveur, pull les images, et redemarre

### 8.2 Mise a jour manuelle

```bash
cd /opt/srpdesk

# 1. Mettre a jour le tag dans .env
sed -i 's/IMAGE_TAG=.*/IMAGE_TAG=v1.2.0/' .env

# 2. Tirer les nouvelles images
docker compose -f docker-compose.prod.yml pull backend frontend landing

# 3. Redemarrer uniquement les services applicatifs (zero downtime BDD)
docker compose -f docker-compose.prod.yml up -d --no-deps backend frontend landing

# 4. Verifier le deploiement
docker compose -f docker-compose.prod.yml ps
curl -s https://app.votredomaine.com/actuator/health

# 5. Nettoyer les anciennes images
docker image prune -f
```

### 8.3 Migrations de base de donnees

Les migrations Flyway s'executent **automatiquement** au demarrage du backend. Verifier :

```bash
docker compose -f docker-compose.prod.yml logs backend | grep -i flyway
# Attendu : "Successfully applied X migrations"
```

> **Conseil** : faire un backup avant toute mise a jour.

---

## 9. Monitoring & Logs

### 9.1 Architecture monitoring

```
Backend ──────> Prometheus ──────> Caddy (basicauth)
  :8080           :9090         monitoring.votredomaine.com
  /actuator/
  prometheus

PostgreSQL ──> pg-exporter ──> Prometheus
  :5432          :9187

Caddy ─────────────────────> Prometheus
  :2019 (admin API)
```

### 9.2 Acceder a Prometheus

Ouvrir `https://monitoring.votredomaine.com` (login/password definis dans `.env`).

Requetes utiles :
- **Taux d'erreur HTTP** : `sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))`
- **Latence p99** : `histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))`
- **Heap JVM** : `jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}`
- **Pool Hikari** : `hikaricp_connections_active / hikaricp_connections_max`
- **Connexions PostgreSQL** : `pg_stat_activity_count`

### 9.3 Alertes

Les regles d'alerte sont definies dans `infra/prometheus/alert-rules.yml` :

| Alerte | Condition | Severite |
|--------|-----------|----------|
| BackendDown | Backend injoignable > 1 min | critical |
| HighErrorRate | Erreurs 5xx > 5% pendant 2 min | warning |
| HighLatency | p99 > 2s pendant 5 min | warning |
| HighJvmHeapUsage | Heap > 85% pendant 5 min | warning |
| HikariPoolExhausted | Connexions BDD > 90% du max | critical |
| PostgresDown | PostgreSQL injoignable > 1 min | critical |
| PostgresConnectionsHigh | Connexions PG > 80% du max | warning |
| CaddyDown | Caddy injoignable > 1 min | critical |

### 9.4 Endpoints de monitoring

| Endpoint | Auth | Description |
|----------|------|-------------|
| `https://app.votredomaine.com/actuator/health` | Public | Etat de sante |
| `https://app.votredomaine.com/actuator/info` | Public | Info application |
| `https://app.votredomaine.com/actuator/prometheus` | Public | Metriques brutes |
| `https://app.votredomaine.com/swagger-ui.html` | Public | Documentation API |
| `https://app.votredomaine.com/.well-known/security.txt` | Public | Contact securite CVD |
| `https://monitoring.votredomaine.com` | BasicAuth | Interface Prometheus |

### 9.5 Consulter les logs

```bash
cd /opt/srpdesk

# Tous les logs en temps reel
docker compose -f docker-compose.prod.yml logs -f

# Logs d'un service specifique
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml logs -f keycloak
docker compose -f docker-compose.prod.yml logs -f caddy
docker compose -f docker-compose.prod.yml logs -f postgres

# Les 100 dernieres lignes avec horodatage
docker compose -f docker-compose.prod.yml logs --tail=100 -t backend

# Logs du backup
docker compose -f docker-compose.prod.yml logs backup
```

> En production, les logs backend sont en **JSON structure** (logback). Les logs Caddy sont aussi en JSON.

---

## 10. Rollback

En cas de probleme apres un deploiement :

```bash
cd /opt/srpdesk

# 1. Identifier la version precedente
docker images | grep srpdesk

# 2. Revenir a la version precedente
sed -i 's/IMAGE_TAG=.*/IMAGE_TAG=v1.1.0/' .env

# 3. Redemarrer
docker compose -f docker-compose.prod.yml up -d --no-deps backend frontend landing

# 4. Verifier
docker compose -f docker-compose.prod.yml ps
curl -s https://app.votredomaine.com/actuator/health
```

> **Attention** : les migrations Flyway sont irreversibles. Si une migration pose probleme, restaurer la base depuis un backup (voir section 7).

---

## 11. Troubleshooting

### Le backend ne demarre pas

```bash
docker compose -f docker-compose.prod.yml logs backend

# Causes frequentes :
# - PostgreSQL pas pret -> docker compose ps (verifier healthy)
# - Keycloak pas pret -> verifier healthcheck keycloak
# - Variable .env manquante -> comparer .env avec .env.example
# - Memoire insuffisante -> augmenter BACKEND_MEMORY_LIMIT
```

### Caddy n'obtient pas les certificats

```bash
docker compose -f docker-compose.prod.yml logs caddy

# Causes frequentes :
# - DNS pas encore propage -> nslookup app.votredomaine.com
# - Port 80/443 pas ouvert -> sudo ufw status
# - ACME_EMAIL vide -> verifier dans .env
# - Rate limit Let's Encrypt -> attendre 1h et reessayer
```

### Erreur 502 Bad Gateway

```bash
# Caddy ne peut pas joindre le conteneur cible
docker compose -f docker-compose.prod.yml ps

# Verifier que le backend est healthy
docker compose -f docker-compose.prod.yml logs backend --tail=50
```

### Keycloak "health/ready" retourne 404

```bash
# Normal en mode Keycloak non-optimise.
# Le docker-compose utilise un healthcheck TCP qui fonctionne.
# Alternative : /realms/master (toujours disponible)
```

### Erreur 409 Conflict sur les requetes API

```bash
# Probleme de filtre multi-tenant :
# 1. Le JWT contient bien le claim org_id ?
# 2. Le header Authorization est transmis par Caddy ?  (oui par defaut)
# 3. CORS_ORIGINS correspond au domaine exact du frontend ?
```

### Espace disque plein

```bash
# Voir l'usage Docker
docker system df -v

# Nettoyer les images inutilisees
docker image prune -a

# Nettoyer aussi les volumes orphelins (ATTENTION : destructif)
docker volume prune
```

### Performance de la BDD

```bash
# Connexions actives
docker compose -f docker-compose.prod.yml exec postgres \
    psql -U srpdesk -c "SELECT count(*) FROM pg_stat_activity;"

# Taille de la base
docker compose -f docker-compose.prod.yml exec postgres \
    psql -U srpdesk -c "SELECT pg_size_pretty(pg_database_size('srpdesk'));"

# Requetes les plus lentes (si pg_stat_statements active)
docker compose -f docker-compose.prod.yml exec postgres \
    psql -U srpdesk -c "SELECT query, mean_exec_time, calls FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT 10;"
```

### Backup echoue

```bash
# Verifier les logs du conteneur backup
docker compose -f docker-compose.prod.yml logs backup

# Tester un backup manuel
docker compose -f docker-compose.prod.yml exec backup /scripts/backup.sh

# Verifier l'espace dans le volume
docker compose -f docker-compose.prod.yml exec backup du -sh /backups/
```

---

## 12. Checklist finale

### Obligatoire avant mise en production

- [ ] **DNS** : les 4 sous-domaines (`app.`, `auth.`, `www.`, `monitoring.`) pointent vers le serveur
- [ ] **Secrets** : tous les `changeme` remplaces par des secrets forts (openssl rand)
- [ ] **Firewall** : seuls les ports 22, 80, 443 sont ouverts
- [ ] **HTTPS** : `curl https://app.votredomaine.com` retourne du contenu (certificat valide)
- [ ] **Backend** : `curl https://app.votredomaine.com/actuator/health` retourne `{"status":"UP"}`
- [ ] **Keycloak** : realm `srpdesk` importe, client `frontend` configure avec les bonnes URLs
- [ ] **Keycloak** : premier utilisateur admin cree dans le realm `srpdesk`
- [ ] **CORS** : `CORS_ORIGINS` = URL exacte du frontend (`https://app.votredomaine.com`)
- [ ] **Frontend** : l'app charge, la connexion Keycloak fonctionne
- [ ] **Backups** : `docker compose logs backup` montre un backup initial reussi
- [ ] **Monitoring** : `https://monitoring.votredomaine.com` accessible (basicauth)
- [ ] **Logs** : `docker compose logs` affiche des logs propres (pas d'erreurs)
- [ ] **Images versionnees** : `IMAGE_TAG` est un tag versionne (pas `latest`)

### Recommande

- [ ] **Email** : `EMAIL_ENABLED=true` avec SMTP configure
- [ ] **JVM tuning** : `JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC`
- [ ] **ENISA/CSIRT** : connecteurs actives si requis par votre pays
- [ ] **Backup externe** : cron qui copie les dumps hors du serveur

---

## Architecture de deploiement

```
                    Internet
                       │
                  Ports 80/443
                       │
                       ▼
              ┌────────────────┐
              │     Caddy      │  HTTPS auto (Let's Encrypt)
              │  reverse proxy │  HTTP/3, security headers
              └───────┬────────┘
                      │
       ┌──────────────┼──────────────┬──────────────┐
       ▼              ▼              ▼              ▼
   app.*          auth.*         www.*       monitoring.*
       │              │              │              │
       ▼              ▼              ▼              ▼
  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
  │ Frontend │  │ Keycloak │  │ Landing  │  │Prometheus│
  │  (nginx) │  │   :8080  │  │ (nginx)  │  │  :9090   │
  └─────┬────┘  └──────────┘  └──────────┘  └─────┬────┘
        │ /api/                                     │
        ▼                                          │
  ┌──────────┐                              ┌──────┴────┐
  │ Backend  │  Spring Boot :8080           │pg-exporter│
  │ (Java 21)│  Flyway migrations auto      │   :9187   │
  └─────┬────┘                              └───────────┘
        │
  ┌─────┼──────────────┐
  ▼     ▼              ▼
┌─────┐ ┌──────────┐ ┌──────────┐
│ PG  │ │  MinIO   │ │  Backup  │
│ 16  │ │   (S3)   │ │ (cron    │
│     │ │          │ │  pg_dump)│
└─────┘ └──────────┘ └──────────┘

Volumes Docker persistants :
  pgdata, minio-data, caddy-data, caddy-config,
  prometheus-data, srpdesk-backups
```

---

## Contacts & References

- **Template .env complet** : `.env.example`
- **Checklist detaillee des variables** : `docs/production-checklist.md`
- **Pipeline CI/CD** : `.github/workflows/release.yml` + `deploy.yml`
- **Documentation API** : `https://app.votredomaine.com/swagger-ui.html`
- **Contact securite** : `https://app.votredomaine.com/.well-known/security.txt`
