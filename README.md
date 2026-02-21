# LexSecura - CRA Compliance & Evidence Manager

> SaaS B2B pour la gestion de conformite au **EU Cyber Resilience Act** (Reglement 2024/2847).
> Permet aux organisations de : enregistrer leurs produits numeriques, gerer les releases, collecter les preuves de conformite, scanner les vulnerabilites, et exporter des packs de conformite pour les auditeurs.

---

## Table des matieres

1. [Fonctionnalites](#fonctionnalites)
2. [Architecture technique](#architecture-technique)
3. [Prerequis](#prerequis)
4. [Installation et demarrage rapide](#installation-et-demarrage-rapide)
5. [Points d'acces](#points-dacces)
6. [Comptes de test](#comptes-de-test)
7. [Guide d'utilisation](#guide-dutilisation)
8. [Endpoints API complets](#endpoints-api-complets)
9. [Roles et permissions](#roles-et-permissions)
10. [Structure du projet](#structure-du-projet)
11. [Commandes utiles](#commandes-utiles)
12. [Configuration](#configuration)
13. [Migrations de base de donnees](#migrations-de-base-de-donnees)
14. [Tests](#tests)
15. [Deploiement Docker complet](#deploiement-docker-complet)
16. [Depannage](#depannage)

---

## Fonctionnalites

| Fonctionnalite | Description |
|---|---|
| **Gestion de produits** | Enregistrer les produits numeriques soumis au CRA (type, criticite, contacts) |
| **Suivi des releases** | Versioning avec git ref, build ID, et statut de cycle de vie |
| **Collecte de preuves** | Upload de fichiers de conformite vers S3 avec verification d'integrite SHA-256 |
| **Ingestion SBOM** | Parser les SBOM CycloneDX, suivre les composants via Package URL (purl) |
| **Scan de vulnerabilites** | Scan automatise via l'API OSV avec decisions (NOT_AFFECTED, PATCH_PLANNED, MITIGATED, FIXED) |
| **Export Compliance Pack** | Generer un ZIP contenant un rapport PDF + donnees JSON pour les auditeurs |
| **Webhook GitLab** | Creation automatique de releases sur tag push ou evenements GitLab release |
| **Piste d'audit** | Chaine de hash append-only (SHA-256) avec detection de falsification et verification |
| **Multi-tenant** | Isolation des donnees par organisation via contexte JWT (org_id) |
| **RBAC** | 3 roles via Keycloak OIDC : Admin, Compliance Manager, Contributor |

---

## Architecture technique

```
                    +------------------+
                    |    Frontend      |
                    |  React 18 + TS   |
                    |  Vite + Tailwind |
                    +--------+---------+
                             |
                       OIDC (Keycloak)
                             |
                    +--------v---------+
                    |    Backend API   |
                    | Spring Boot 3.3  |
                    |    Java 21       |
                    +--+-----+-----+--+
                       |     |     |
              +--------+  +--+--+  +--------+
              |           |     |           |
        +-----v----+ +---v---+ +---v------+ +----v-----+
        | PostgreSQL| |Keycloak| |  MinIO   | | OSV API  |
        |    16     | |   24   | | (S3)     | |(externe) |
        +-----------+ +--------+ +----------+ +----------+
```

| Composant | Technologie |
|-----------|-------------|
| Backend | Java 21, Spring Boot 3.3, Maven, Architecture Hexagonale |
| Frontend | React 18, TypeScript, Vite 5, Tailwind CSS, TanStack Query |
| Base de donnees | PostgreSQL 16, Flyway (migrations V001-V014) |
| Authentification | Keycloak 24 (OIDC + PKCE) |
| Stockage fichiers | S3 (MinIO en local) |
| API | REST, OpenAPI 3.1, Problem+JSON (RFC 9457) |
| Observabilite | Micrometer, Prometheus, OpenTelemetry |

---

## Prerequis

- **Java 21+** (Eclipse Temurin recommande)
- **Node.js 20+**
- **Docker Desktop** (ou Docker Engine + Docker Compose)

---

## Installation et demarrage rapide

### Option 1 : Setup automatise

```bash
make init
```

Puis dans deux terminaux :

```bash
# Terminal 1 - Backend
make backend-run

# Terminal 2 - Frontend
make frontend-dev
```

### Option 2 : Setup manuel

```bash
# 1. Demarrer l'infrastructure (PostgreSQL, Keycloak, MinIO)
cd infra && docker compose up -d postgres keycloak minio minio-init

# 2. Attendre que les services soient healthy
docker compose ps   # verifier que tout est "healthy"

# 3. Demarrer le backend (terminal 1)
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# 4. Demarrer le frontend (terminal 2)
cd frontend && npm install && npm run dev
```

### Option 3 : Tout en Docker

```bash
cd infra && docker compose up -d --build
```

Cela demarre : PostgreSQL, Keycloak, MinIO, Backend (port 8080), Frontend (port 3000).

---

## Points d'acces

| Service | URL | Identifiants |
|---------|-----|-------------|
| **Frontend** | http://localhost:5173 (dev) / http://localhost:3000 (Docker) | Login Keycloak |
| **Backend API** | http://localhost:8080 | JWT requis |
| **Swagger UI** | http://localhost:8080/swagger-ui/index.html | - |
| **Keycloak Admin** | http://localhost:8180 | `admin` / `admin` |
| **MinIO Console** | http://localhost:9001 | `minioadmin` / `minioadmin` |
| **Health Check** | http://localhost:8080/actuator/health | - |
| **Metrics Prometheus** | http://localhost:8080/actuator/prometheus | - |

---

## Comptes de test

Les comptes suivants sont pre-configures dans Keycloak (realm `lexsecura`) :

| Email | Mot de passe | Role | Organisation |
|-------|-------------|------|-------------|
| `admin@lexsecura.com` | `admin123` | ADMIN | org-1 (`00000000-0000-0000-0000-000000000001`) |
| `manager@lexsecura.com` | `manager123` | COMPLIANCE_MANAGER | org-1 |
| `contributor@lexsecura.com` | `contrib123` | CONTRIBUTOR | org-1 |
| `admin-org2@lexsecura.com` | `admin123` | ADMIN | org-2 (`00000000-0000-0000-0000-000000000002`) |

> L'utilisateur `admin-org2` est dans une organisation differente et ne voit pas les donnees de org-1 (isolation multi-tenant).

---

## Guide d'utilisation

### Flux de travail complet

Le workflow typique CRA suit ces etapes :

```
1. Creer un Produit
       |
2. Creer une Release
       |
3. Uploader les Preuves (evidences)
       |
4. Uploader un SBOM (CycloneDX)
       |
5. Lancer un Scan de Vulnerabilites
       |
6. Traiter les Findings (decisions)
       |
7. Exporter le Compliance Pack (ZIP)
       |
8. Verifier la Piste d'Audit
```

### Etape 1 : Creer un produit

1. Se connecter au frontend avec un compte ADMIN
2. Aller dans **Products** > **Add Product**
3. Remplir :
   - **Name** : nom du produit (ex: "IoT Gateway v2")
   - **Type** : classification CRA du produit
     - `DEFAULT` - Produit standard
     - `CLASS_I` - Classe I (important)
     - `CLASS_II` - Classe II (important)
     - `IMPORTANT_CLASS_I` - Important Classe I
     - `IMPORTANT_CLASS_II` - Important Classe II
     - `CRITICAL` - Produit critique
   - **Criticality** : niveau de criticite (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
4. Cliquer **Create**

### Etape 2 : Creer une release

1. Cliquer sur un produit pour voir ses details
2. Dans la section **Releases**, cliquer **New Release**
3. Remplir :
   - **Version** (obligatoire) : ex. `1.0.0`
   - **Git Ref** (optionnel) : hash de commit ou tag, ex. `v1.0.0`
   - **Build ID** (optionnel) : identifiant de build, ex. `build-42`
4. Cliquer **Create**

### Etape 3 : Uploader des preuves (evidences)

1. Dans le detail d'une release, onglet **Evidences**
2. Selectionner le **type d'evidence** :
   - `SBOM` - Software Bill of Materials
   - `TEST_REPORT` - Rapport de tests
   - `VULNERABILITY_SCAN` - Rapport de scan
   - `PENTEST_REPORT` - Rapport de pentest
   - `DESIGN_DOC` - Documentation de conception
   - `INCIDENT_RESPONSE_PLAN` - Plan de reponse aux incidents
   - `UPDATE_POLICY` - Politique de mise a jour
   - `CONFORMITY_DECLARATION` - Declaration de conformite
   - `OTHER` - Autre
3. Glisser-deposer ou selectionner le fichier (max 50 MB)
4. Le fichier est stocke dans S3 (MinIO) avec un hash SHA-256 pour verification d'integrite

### Etape 4 : Uploader un SBOM

1. Dans le detail d'une release, section **Upload SBOM**
2. Uploader un fichier SBOM au format **CycloneDX** (JSON ou XML)
3. Les composants sont extraits automatiquement et visibles dans l'onglet **Components**
4. Chaque composant est identifie par son **PURL** (Package URL)

### Etape 5 : Scanner les vulnerabilites

1. Dans le detail d'une release, onglet **Findings**
2. Cliquer **Trigger Vulnerability Scan**
3. Le backend interroge l'**API OSV** (Open Source Vulnerabilities) pour chaque composant SBOM
4. Les vulnerabilites detectees apparaissent comme **Findings** avec :
   - ID de vulnerabilite (CVE, GHSA, etc.)
   - Severite (LOW, MEDIUM, HIGH, CRITICAL)
   - Composant affecte
   - Statut : `OPEN` par defaut

### Etape 6 : Traiter les findings (decisions)

Pour chaque finding, un ADMIN ou COMPLIANCE_MANAGER peut ajouter une **decision** :

1. Cliquer **Add Decision** sur un finding
2. Choisir le type de decision :
   - `NOT_AFFECTED` - Le produit n'est pas affecte
   - `PATCH_PLANNED` - Un patch est planifie (avec date optionnelle)
   - `MITIGATED` - La vulnerabilite est mitigee
   - `FIXED` - La vulnerabilite est corrigee
3. Expliquer la **rationale** (justification obligatoire)
4. Optionnel : definir une **date d'echeance**

Le statut du finding est mis a jour automatiquement.

### Etape 7 : Exporter le Compliance Pack

1. Dans le detail d'un produit, cliquer **Export** sur une release
2. Un fichier **ZIP** est telecharge contenant :
   - Rapport PDF de conformite
   - Donnees JSON structurees
   - Liste des evidences, composants et findings
3. Ce pack est destine aux auditeurs et autorites de marche

### Etape 8 : Verifier la piste d'audit

Le **Dashboard** affiche en permanence l'etat de la piste d'audit :
- **Vert** : "Audit Trail: Verified" - Toutes les entrees sont integres
- **Rouge** : "Integrity Issue Detected" - Une falsification a ete detectee

L'audit fonctionne comme une **blockchain simplifiee** :
- Chaque evenement est lie au precedent par un hash SHA-256
- Si un enregistrement est modifie en base, la chaine est rompue
- L'endpoint `GET /api/v1/audit/verify` verifie toute la chaine

---

## Endpoints API complets

Tous les endpoints necessitent un JWT valide sauf indication contraire.

### Produits (`/api/v1/products`)

| Methode | Path | Role requis | Description |
|---------|------|-------------|-------------|
| `GET` | `/api/v1/products` | Tout role | Lister les produits de l'organisation |
| `POST` | `/api/v1/products` | ADMIN | Creer un produit |
| `GET` | `/api/v1/products/{id}` | Tout role | Detail d'un produit |
| `PUT` | `/api/v1/products/{id}` | ADMIN | Modifier un produit |
| `DELETE` | `/api/v1/products/{id}` | ADMIN | Supprimer un produit |

### Releases

| Methode | Path | Role requis | Description |
|---------|------|-------------|-------------|
| `GET` | `/api/v1/products/{productId}/releases` | Tout role | Lister les releases d'un produit |
| `POST` | `/api/v1/products/{productId}/releases` | ADMIN, CM | Creer une release |

### Evidences

| Methode | Path | Role requis | Description |
|---------|------|-------------|-------------|
| `POST` | `/api/v1/releases/{releaseId}/evidences` | Tout role | Uploader un fichier de preuve |
| `GET` | `/api/v1/releases/{releaseId}/evidences` | Tout role | Lister les preuves d'une release |
| `GET` | `/api/v1/evidences/{id}/download` | Tout role | Telecharger un fichier de preuve |
| `DELETE` | `/api/v1/evidences/{id}` | ADMIN, CM | Supprimer une preuve |

### SBOM & Composants

| Methode | Path | Role requis | Description |
|---------|------|-------------|-------------|
| `POST` | `/api/v1/releases/{releaseId}/sbom` | Tout role | Uploader un SBOM CycloneDX |
| `GET` | `/api/v1/releases/{releaseId}/components` | Tout role | Lister les composants SBOM |

### Vulnerabilites & Findings

| Methode | Path | Role requis | Description |
|---------|------|-------------|-------------|
| `POST` | `/api/v1/releases/{releaseId}/scan` | ADMIN, CM | Lancer un scan de vulnerabilites |
| `GET` | `/api/v1/releases/{releaseId}/findings` | Tout role | Lister les findings d'une release |
| `GET` | `/api/v1/products/{productId}/findings` | Tout role | Lister les findings d'un produit |
| `POST` | `/api/v1/findings/{findingId}/decisions` | ADMIN, CM | Ajouter une decision a un finding |

### Conformite & Audit

| Methode | Path | Role requis | Description |
|---------|------|-------------|-------------|
| `GET` | `/api/v1/releases/{releaseId}/export` | ADMIN, CM | Exporter le compliance pack (ZIP) |
| `GET` | `/api/v1/audit/verify` | ADMIN, CM | Verifier l'integrite de la piste d'audit |
| `GET` | `/api/v1/audit/events` | ADMIN, CM | Consulter les evenements d'audit |

### IAM (Identity & Access Management)

| Methode | Path | Role requis | Description |
|---------|------|-------------|-------------|
| `POST` | `/api/v1/orgs` | ADMIN | Creer une organisation |
| `GET` | `/api/v1/orgs` | Tout role | Lister les organisations |
| `POST` | `/api/v1/orgs/{orgId}/members` | ADMIN | Ajouter un membre a une organisation |
| `GET` | `/api/v1/me` | Tout role | Infos de l'utilisateur connecte |

### Integrations

| Methode | Path | Auth | Description |
|---------|------|------|-------------|
| `POST` | `/integrations/gitlab/webhook` | Secret header | Webhook GitLab (tag_push, release) |

### Endpoints publics (sans JWT)

| Path | Description |
|------|-------------|
| `/actuator/health` | Health check |
| `/actuator/info` | Infos application |
| `/actuator/prometheus` | Metriques Prometheus |
| `/swagger-ui/**` | Interface Swagger |
| `/v3/api-docs/**` | Spec OpenAPI |

---

## Roles et permissions

| Action | ADMIN | COMPLIANCE_MANAGER | CONTRIBUTOR |
|--------|:-----:|:------------------:|:-----------:|
| Voir les produits | Oui | Oui | Oui |
| Creer/Modifier/Supprimer un produit | Oui | Non | Non |
| Creer une release | Oui | Oui | Non |
| Uploader des preuves | Oui | Oui | Oui |
| Uploader un SBOM | Oui | Oui | Oui |
| Lancer un scan de vulnerabilites | Oui | Oui | Non |
| Ajouter une decision (finding) | Oui | Oui | Non |
| Exporter le compliance pack | Oui | Oui | Non |
| Verifier la piste d'audit | Oui | Oui | Non |
| Consulter les evenements d'audit | Oui | Oui | Non |
| Creer une organisation | Oui | Non | Non |
| Ajouter un membre | Oui | Non | Non |
| Supprimer une preuve | Oui | Oui | Non |

---

## Structure du projet

```
lexsecura/
├── backend/                         # API Spring Boot (Architecture Hexagonale)
│   ├── src/main/java/com/lexsecura/
│   │   ├── domain/                  # Modeles, ports (interfaces repo), services domaine
│   │   │   ├── model/               #   Product, Release, Evidence, Component, Finding, AuditEvent, etc.
│   │   │   └── repository/          #   Interfaces des repositories (ports)
│   │   ├── application/             # DTOs, services applicatifs, ports applicatifs
│   │   │   ├── dto/                 #   Request/Response DTOs
│   │   │   └── service/             #   ProductService, ReleaseService, EvidenceService, etc.
│   │   ├── infrastructure/          # Adaptateurs (implementations)
│   │   │   ├── persistence/         #   JPA entities, Spring Data repositories
│   │   │   ├── security/            #   SecurityConfig, JwtTenantFilter, RateLimitFilter
│   │   │   ├── storage/             #   S3StorageAdapter (MinIO)
│   │   │   └── osv/                 #   OsvApiClient (scan de vulnerabilites)
│   │   └── api/                     # Couche presentation
│   │       ├── controller/          #   REST controllers
│   │       └── error/               #   GlobalExceptionHandler (Problem+JSON)
│   └── src/main/resources/
│       ├── application.yml          # Config principale
│       ├── application-local.yml    # Config dev local (port 5433)
│       ├── application-docker.yml   # Config Docker
│       └── db/migration/            # Migrations Flyway (V001-V014)
│
├── frontend/                        # SPA React 18
│   └── src/
│       ├── api/                     # Clients API (axios) : products, releases, evidences, audit, findings
│       ├── auth/                    # Keycloak config + AuthProvider
│       ├── hooks/                   # TanStack Query hooks (useProducts, useReleases, etc.)
│       ├── pages/                   # Pages : Dashboard, Products, ProductDetail, ReleaseDetail, Findings
│       ├── components/              # Composants partages : DataTable, StatusBadge, FileUpload, etc.
│       └── types/                   # Types TypeScript
│
├── infra/                           # Infrastructure
│   ├── docker-compose.yml           # Docker Compose (postgres, keycloak, minio, backend, frontend)
│   ├── keycloak/realm-export.json   # Config realm Keycloak (roles, users, scopes)
│   └── postgres/init.sql            # Script d'init PostgreSQL
│
├── docs/                            # Documentation
│   ├── local-setup.md               # Guide d'installation locale
│   ├── production-checklist.md      # Checklist de production
│   ├── gitlab-webhook-setup.md      # Guide webhook GitLab
│   └── adr/                         # Architecture Decision Records
│       ├── 001-hexagonal-architecture.md
│       ├── 002-multi-tenant-by-column.md
│       ├── 003-s3-storage-sha256.md
│       └── 004-no-itext-license.md
│
├── .github/workflows/               # Pipeline CI (GitHub Actions)
├── Makefile                         # Commandes de dev
└── CLAUDE.md                        # Instructions pour Claude Code
```

---

## Commandes utiles

### Makefile

```bash
make help            # Afficher toutes les commandes
make init            # Premier setup (npm install + demarrer infra)
make up              # Demarrer l'infra (postgres, keycloak, minio)
make up-all          # Demarrer tout (y compris backend et frontend Docker)
make down            # Arreter les services
make down-volumes    # Arreter et supprimer les volumes (reset complet)
make ps              # Voir les services en cours
make logs            # Suivre les logs de tous les services
make logs-backend    # Suivre les logs du backend
make backend-run     # Lancer le backend en local
make backend-test    # Lancer les tests backend
make backend-verify  # Tests + tests d'integration
make frontend-dev    # Lancer le frontend en dev
make frontend-build  # Build le frontend
make test            # Lancer tous les tests
make build           # Build les images Docker
make clean           # Nettoyer les artefacts de build
```

### Commandes directes

```bash
# Backend
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
cd backend && ./mvnw test
cd backend && ./mvnw verify

# Frontend
cd frontend && npm install
cd frontend && npm run dev
cd frontend && npm run build
cd frontend && npx tsc --noEmit    # Type check

# Docker
cd infra && docker compose up -d postgres keycloak minio minio-init
cd infra && docker compose up -d --build    # Tout rebuilder
cd infra && docker compose down -v          # Reset complet
cd infra && docker compose logs -f backend  # Logs backend
```

---

## Configuration

### Variables d'environnement (Backend)

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | Hote PostgreSQL |
| `DB_PORT` | `5432` | Port PostgreSQL |
| `DB_NAME` | `lexsecura` | Nom de la base |
| `DB_USER` | `lexsecura` | Utilisateur DB |
| `DB_PASSWORD` | `lexsecura` | Mot de passe DB |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8180/realms/lexsecura` | Issuer URI Keycloak |
| `KEYCLOAK_JWK_URI` | `http://localhost:8180/realms/lexsecura/protocol/openid-connect/certs` | JWK Set URI |
| `S3_ENDPOINT` | `http://localhost:9000` | Endpoint S3/MinIO |
| `S3_ACCESS_KEY` | `minioadmin` | Access key S3 |
| `S3_SECRET_KEY` | `minioadmin` | Secret key S3 |
| `S3_BUCKET` | `evidences` | Nom du bucket S3 |
| `S3_REGION` | `us-east-1` | Region S3 |
| `CORS_ORIGINS` | `http://localhost:3000` | Origines CORS autorisees (separees par `,`) |
| `RATE_LIMIT_ENABLED` | `false` | Activer le rate limiting |
| `RATE_LIMIT_RPM` | `120` | Requetes par minute |
| `GITLAB_WEBHOOK_SECRET` | *(vide)* | Secret pour le webhook GitLab |
| `OSV_BASE_URL` | `https://api.osv.dev` | URL de l'API OSV |
| `OSV_BATCH_SIZE` | `100` | Taille des batches OSV |

### Variables d'environnement (Frontend)

| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_KEYCLOAK_URL` | `http://localhost:8180` | URL Keycloak |
| `VITE_KEYCLOAK_REALM` | `lexsecura` | Realm Keycloak |
| `VITE_KEYCLOAK_CLIENT_ID` | `frontend` | Client ID Keycloak |

### Profils Spring Boot

| Profil | Usage |
|--------|-------|
| `local` | Dev local - PostgreSQL sur port 5433, CORS multi-origines, SQL visible |
| `docker` | Docker Compose - services internes (postgres:5432, keycloak:8180, minio:9000) |
| *(default)* | Config par defaut |

---

## Migrations de base de donnees

Les migrations Flyway sont appliquees automatiquement au demarrage du backend.

| Migration | Description |
|-----------|-------------|
| `V001__create_products.sql` | Table `products` initiale |
| `V002__create_requirements.sql` | Table `requirements` (exigences CRA) |
| `V003__create_assessments.sql` | Table `assessments` (evaluations) |
| `V004__create_assessment_items.sql` | Table `assessment_items` |
| `V005__create_evidences.sql` | Table `evidences` (v0) |
| `V006__seed_requirements.sql` | Donnees de base : 24 exigences CRA (Art.13, Art.14, Annex I, II) |
| `V007__drop_assessment_tables.sql` | Suppression des tables legacy (clean break pour V1) |
| `V008__alter_products_v1.sql` | Evolution produits : ajout type, criticality, contacts JSONB |
| `V009__create_releases_and_evidences.sql` | Tables `releases` et `evidences` V1 |
| `V010__create_audit_events.sql` | Table `audit_events` (piste d'audit hash chain) |
| `V011__create_product_repo_mappings.sql` | Table `product_repo_mappings` (lien GitLab) |
| `V012__create_components.sql` | Table `components` (composants SBOM) |
| `V013__create_vulnerabilities.sql` | Tables `vulnerabilities` et `finding_decisions` |
| `V014__create_organizations_and_members.sql` | Tables `organizations` et `org_members` |

---

## Tests

```bash
# Tests backend (unitaires)
cd backend && ./mvnw test

# Verification types frontend
cd frontend && npx tsc --noEmit

# Tous les tests
make test
```

---

## Deploiement Docker complet

Pour deployer l'ensemble de la stack en Docker :

```bash
cd infra && docker compose up -d --build
```

Cela demarre 5 services :

| Service | Container | Port | Dependances |
|---------|-----------|------|-------------|
| PostgreSQL 16 | `lexsecura-postgres` | 5433:5432 | - |
| Keycloak 24 | `lexsecura-keycloak` | 8180:8180 | - |
| MinIO | `lexsecura-minio` | 9000, 9001 | - |
| Backend | `lexsecura-backend` | 8080:8080 | postgres, keycloak, minio |
| Frontend | `lexsecura-frontend` | 3000:80 | backend |

Un service `minio-init` cree automatiquement le bucket `evidences` au demarrage.

---

## Depannage

### Keycloak met du temps a demarrer
C'est normal au premier lancement (30-60s). Verifier avec :
```bash
docker logs lexsecura-keycloak
```

### Erreur "An error occurred, please login again"
- Verifier que Keycloak est bien demarre : http://localhost:8180
- Verifier que le realm `lexsecura` existe dans la console Keycloak
- Si le realm n'est pas importe, redemarrer le container Keycloak :
  ```bash
  cd infra && docker compose restart keycloak
  ```

### Erreur 409 sur les endpoints API
- Cause : le filtre de tenant (`JwtTenantFilter`) n'arrive pas a extraire `org_id` du JWT
- Verifier que l'attribut `org_id` est bien configure sur l'utilisateur Keycloak
- Verifier que le token contient le claim `org_id` (decoder le JWT sur jwt.io)

### Erreur 500 sur POST /products
- Si l'erreur mentionne "jsonb" : la colonne `contacts` necessite l'annotation `@JdbcTypeCode(SqlTypes.JSON)` sur l'entite JPA
- Cette correction est deja appliquee dans le code actuel

### Le backend ne se connecte pas a PostgreSQL
- Verifier que PostgreSQL est healthy : `docker compose ps`
- En mode local, le port est **5433** (pas 5432) pour eviter les conflits

### Conflits de ports
Les ports suivants doivent etre disponibles :

| Port | Service |
|------|---------|
| 5433 | PostgreSQL |
| 8080 | Backend |
| 8180 | Keycloak |
| 9000 | MinIO S3 |
| 9001 | MinIO Console |
| 5173 | Frontend (dev) |
| 3000 | Frontend (Docker) |

### Reset complet
Pour repartir de zero (supprime toutes les donnees) :
```bash
make down-volumes
make up
```

---

## Licence

Proprietary - All rights reserved.
