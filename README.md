# SRPDesk - Centre de Controle CRA pour Editeurs Logiciels & Fabricants IoT

SRPDesk est une plateforme tout-en-un qui aide les entreprises a se mettre en conformite avec le **Cyber Resilience Act europeen** (Reglement 2024/2847).

Au lieu de gerer la conformite avec des tableurs, des mails et de la panique, SRPDesk offre un **centre de controle automatise** qui couvre l'ensemble du cycle de vie : de l'inventaire logiciel jusqu'a la declaration de conformite EU.

---

## Ce que fait SRPDesk

### 1. Inventaire et suivi de vos produits numeriques

Chaque produit logiciel ou IoT est enregistre avec sa classification CRA (Default, Classe I, Classe II, Critique), son niveau de criticite, et ses contacts responsables. Pour chaque produit, on suit les **versions** (releases) avec leur cycle de vie complet : brouillon, publiee, obsolete, revoquee.

### 2. Nomenclature logicielle (SBOM)

SRPDesk sait lire et analyser les **SBOM** (Software Bill of Materials) aux formats **CycloneDX** et **SPDX**. Pour chaque version de votre produit, vous savez exactement quels composants open source sont utilises, avec leur licence et leur fournisseur.

Un **score de qualite SBOM** (sur 100 points, base sur les criteres BSI TR-03183-2 et NTIA) vous dit si votre nomenclature est suffisamment complete pour un auditeur. Les SBOM peuvent etre importees manuellement, par vos fournisseurs, ou **automatiquement depuis votre pipeline CI/CD** (GitHub Actions, GitLab).

Vous pouvez aussi **partager vos SBOM** avec vos clients ou partenaires via des liens temporaires securises.

### 3. Surveillance des vulnerabilites

SRPDesk scanne automatiquement vos composants contre les bases de vulnerabilites publiques (**OSV**, **NVD**, **CISA KEV**, **EUVD**). Quand une faille est detectee, un "finding" est cree avec sa severite, le composant touche, et un statut de suivi.

Pour chaque vulnerabilite, le responsable peut prendre une **decision tracee** : non concerne, correctif prevu, risque attenue, ou corrige. Ces decisions constituent votre dossier de preuve.

**Nouveaute** : les vulnerabilites activement exploitees (CISA KEV) declenchent automatiquement un evenement CRA.

### 4. Documents VEX (Vulnerability Exploitability eXchange)

SRPDesk genere des documents **VEX** dans trois formats standards :
- **OpenVEX** (format CISA)
- **CycloneDX VEX** (format OWASP)
- **CSAF 2.0** (format europeen)

Ces documents communiquent a vos clients quelles vulnerabilites vous affectent reellement et lesquelles ne vous concernent pas.

### 5. Salle de crise CRA (War Room)

Quand une vulnerabilite exploitee ou un incident severe touche votre produit, le CRA impose des delais stricts de notification :
- **24 heures** : alerte precoce a l'ENISA
- **72 heures** : notification detaillee
- **14 jours** : rapport final

SRPDesk organise cette reponse d'urgence :
- Creation d'un evenement avec les personnes concernees (responsable, approbateur, observateur)
- **Comptes a rebours SLA en temps reel** configures par organisation
- Liaison automatique avec les releases, findings et preuves
- Systeme d'**escalade automatique** quand les delais approchent
- **Notification parallele au CSIRT national** (Art. 14 CRA)

### 6. Soumissions a la plateforme ENISA (SRP Autopilot)

Pour chaque notification (alerte precoce, notification, rapport final), SRPDesk :
- **Pre-remplit automatiquement** le formulaire a partir des donnees existantes (produit, vulnerabilite, patch, etc.)
- Valide le contenu contre le **schema officiel JSON**
- Genere un **bundle ZIP exportable** (JSON + PDF lisible + chaine d'audit) pret a soumettre
- Enregistre la reference de soumission et la preuve d'acquittement

Le connecteur ENISA est prepare pour la future API officielle.

### 7. Pipeline CI/CD automatise

SRPDesk s'integre directement dans votre pipeline de developpement :
- **Webhook GitHub / GitLab** : creation automatique de releases sur tag push
- **Upload SBOM automatise** depuis GitHub Actions avec score de qualite
- **Politiques de securite (gates)** : bloquer un deploiement si trop de vulnerabilites critiques ou si le score SBOM est trop bas
- **Fraicheur SBOM** : alerte automatique si le SBOM n'a pas ete mis a jour depuis plus de 30 jours

Tout cela transforme SRPDesk d'un classeur manuel en un **centre de controle automatise**.

### 8. Evaluation de conformite (Art. 32 CRA)

SRPDesk guide vos equipes dans le processus d'evaluation de conformite :
- **Module A** (auto-evaluation) : 8 etapes avec suivi de progression
- **Module H** (assurance qualite totale) : 10 etapes
- Chaque etape peut etre documentee avec des notes et des preuves attachees
- Workflow : Non demarre → En cours → Termine → Approuve

### 9. Analyse de risques (Art. 13 CRA)

Chaque produit peut avoir une ou plusieurs analyses de risques :
- Methodologies supportees : **STRIDE**, **DREAD**, ou personnalisee
- Matrice de risque 5x5 (probabilite x impact)
- Suivi des controles existants et des plans d'attenuation
- Niveau de risque residuel
- Workflow : Brouillon → En revue → Approuve

### 10. Normes appliquees (Art. 27 CRA)

Suivez quelles normes europeennes s'appliquent a vos produits :
- EN 303 645 (IoT), IEC 62443 (industriel), ISO 27001, etc.
- Statut par norme : revendiquee, partielle, ou complete
- Lien avec les preuves de conformite

### 11. Declaration de conformite UE (Annexe V CRA)

SRPDesk genere la **declaration de conformite UE** officielle avec tous les champs requis par l'Annexe V :
- Informations fabricant et representant autorise
- Identification du produit et module d'evaluation
- Organisme notifie (si applicable)
- Normes harmonisees appliquees
- Signature electronique
- Workflow : Brouillon → Signe → Publie

### 12. Politique de divulgation coordonnee (CVD)

Conformement a l'Art. 13(6) du CRA, SRPDesk permet de :
- Publier votre **politique de divulgation** (email de contact, PGP, perimetre, bug bounty, delai)
- Recevoir des **rapports de vulnerabilite** du public via un formulaire dedie
- Suivre chaque rapport avec un workflow complet : Nouveau → Accepte → Triage → Confirme/Rejete → En correction → Corrige → Divulgue
- Generer automatiquement le fichier **security.txt** (RFC 9116)

### 13. Surveillance de fin de support

SRPDesk surveille automatiquement les dates de fin de support de vos releases et **alerte 90 jours avant** l'expiration. Un produit en fin de vie sans mise a jour est une violation du CRA.

### 14. Avis de securite (Art. 14.3)

Publiez et notifiez des **avis de securite** a vos utilisateurs directement depuis la plateforme, avec suivi du statut (brouillon, publie, notifie).

### 15. Centre de controle (Dashboard)

Le tableau de bord offre une **vue en temps reel** de toute votre conformite :

- **Score d'automatisation** : pourcentage de produits avec SBOM a jour et CI fonctionnel
- **Produits conformes** : combien ont termine l'evaluation de conformite, l'analyse de risque et la declaration EU
- **Alertes intelligentes** : vulnerabilites critiques, SLA en retard, fin de support imminente, pipeline CI en echec, SBOM obsolete, conformite bloquee
- **Cartes produit enrichies** : chaque produit affiche son score de preparation, ses badges de conformite/risque/EU DoC, ses vulnerabilites ouvertes, sa derniere version
- **Tri et filtrage** : par score, par alertes, par nom, ou filtrer uniquement les produits problematiques

### 16. Checklist CRA (Annexe I)

Une checklist interactive couvrant les exigences de l'Annexe I :
- **Partie I** : Secure by Design (securite des la conception)
- **Partie II** : Gestion des vulnerabilites
- Chaque point peut etre evalue : conforme, partiellement conforme, non conforme, non applicable
- Lien avec les preuves et notes d'evaluation

### 17. Score de preparation CRA

Un **score sur 100** calcule automatiquement a partir de 5 categories :
- Secure by Design
- Gestion des vulnerabilites
- Gestion SBOM
- Reporting d'incidents
- Documentation

Ce score vous indique ou vous en etes et quelles actions restent a mener.

### 18. Assistant IA local

Un assistant IA (LLM local via Ollama) aide a :
- **Generer des brouillons de soumission SRP** a partir des donnees existantes
- **Creer des packs de communication** (advisory, email client, notes de version)
- **Pre-remplir des questionnaires** d'audit ou de certification

L'IA tourne **localement** — aucune donnee ne quitte votre infrastructure. Les informations personnelles sont automatiquement masquees avant traitement.

### 19. Export SBOM multi-format

Exportez vos SBOM dans les deux standards internationaux :
- **SPDX 2.3** (ISO 5962)
- **CycloneDX 1.6**

### 20. Piste d'audit infalsifiable

Chaque action dans SRPDesk est enregistree dans une **chaine de hash SHA-256** (type blockchain). Si un enregistrement est modifie en base de donnees, la chaine est rompue et l'integrite est signalee sur le dashboard.

### 21. Collecte de preuves

Chaque version de produit peut recevoir des **fichiers de preuve** stockes de maniere securisee (S3/MinIO) avec verification d'integrite SHA-256 : SBOM, rapports de test, scans de vulnerabilites, rapports de pentest, plans de reponse aux incidents, etc.

### 22. Cles d'API

Des cles d'API dediees permettent d'integrer SRPDesk avec vos outils existants (CI/CD, scripts, outils internes) sans passer par l'authentification utilisateur.

### 23. Webhooks sortants

Configurez des webhooks pour etre notifie en temps reel des evenements importants (nouvelles vulnerabilites, changements de statut, etc.) dans vos canaux de communication (Slack, Teams, email).

---

## Couverture CRA

SRPDesk couvre les principaux articles du Cyber Resilience Act :

| Article CRA | Exigence | Couverture SRPDesk |
|-------------|----------|-------------------|
| **Art. 13** | Obligations des fabricants | Checklist Annexe I, score de preparation, analyse de risques |
| **Art. 13(1)** | Evaluation des risques | Matrice STRIDE/DREAD, niveau de risque residuel |
| **Art. 13(6)** | Divulgation coordonnee (CVD) | Politique CVD, formulaire public, workflow 8 etats, security.txt |
| **Art. 14** | Notification ENISA + CSIRT | War Room, SLA 24h/72h, SRP Autopilot, notification CSIRT parallele |
| **Art. 14(3)** | Avis de securite | Publication et notification d'advisories |
| **Art. 27** | Normes harmonisees | Suivi des normes appliquees par produit |
| **Art. 32** | Evaluation de conformite | Modules A et H avec suivi par etapes |
| **Annexe I** | Exigences essentielles | Checklist interactive, score par categorie |
| **Annexe V** | Declaration de conformite UE | Generateur avec tous les champs requis |
| **Annexe VII** | Information technique | SBOM, rapports de scan, preuves de conformite |

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
                    +--+--+--+--+--+--+
                       |  |  |  |  |
           +-----------+  |  |  |  +-----------+
           |     +--------+  |  +--------+     |
     +-----v--+ +---v---+ +-v----+ +---v----+ +v--------+
     |Postgres | |Keycloak| |MinIO | |OSV/NVD | | Ollama  |
     |   16    | |   24   | | (S3) | |KEV/EUVD| | (IA)    |
     +---------+ +--------+ +------+ +--------+ +---------+
```

| Composant | Technologie |
|-----------|-------------|
| Backend | Java 21, Spring Boot 3.3, Maven, Architecture Hexagonale |
| Frontend | React 18, TypeScript, Vite 5, Tailwind CSS, TanStack Query |
| Base de donnees | PostgreSQL 16, Flyway (migrations V001-V032) |
| Authentification | Keycloak 24 (OIDC + PKCE), 3 roles |
| Stockage fichiers | S3 (MinIO en local) avec hash SHA-256 |
| API | REST, Problem+JSON (RFC 9457) |
| IA locale | Ollama (Phi-3.5), redaction PII, validation JSON Schema |
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
docker compose ps

# 3. Demarrer le backend (terminal 1)
cd backend && ./mvnw spring-boot:run

# 4. Demarrer le frontend (terminal 2)
cd frontend && npm install && npm run dev
```

### Option 3 : Tout en Docker

```bash
cd infra && docker compose up -d --build
```

---

## Points d'acces

| Service | URL |
|---------|-----|
| **Frontend** | http://localhost:5173 (dev) / http://localhost:3000 (Docker) |
| **Backend API** | http://localhost:8080/api/v1 |
| **Keycloak Admin** | http://localhost:8180 (dev) |
| **MinIO Console** | http://localhost:9001 |
| **Health Check** | http://localhost:8080/actuator/health |
| **Metriques** | http://localhost:8080/actuator/prometheus |

---

## Comptes de test

| Email | Mot de passe | Role | Organisation |
|-------|-------------|------|-------------|
| `admin@lexsecura.com` | `admin123` | ADMIN | org-1 |
| `manager@lexsecura.com` | `manager123` | COMPLIANCE_MANAGER | org-1 |
| `contributor@lexsecura.com` | `contrib123` | CONTRIBUTOR | org-1 |
| `admin-org2@lexsecura.com` | `admin123` | ADMIN | org-2 |

> L'utilisateur org-2 ne voit pas les donnees de org-1 (isolation multi-tenant).

---

## Guide d'utilisation

### Flux de travail typique

```
1. Creer un produit (nom, type CRA, criticite)
       |
2. Creer une release (version, ref git)
       |
3. Uploader un SBOM (CycloneDX ou SPDX)
       |
4. Scanner les vulnerabilites (automatique via OSV)
       |
5. Traiter les findings (decisions tracees)
       |
6. Completer la checklist CRA (Annexe I)
       |
7. Realiser l'analyse de risques (STRIDE/DREAD)
       |
8. Demarrer l'evaluation de conformite (Module A ou H)
       |
9. Generer la declaration de conformite UE
       |
10. Exporter le dossier de conformite (ZIP)
```

### En cas d'incident CRA

```
1. Creer un evenement CRA (vulnerabilite exploitee ou incident severe)
       |
2. Le compte a rebours SLA demarre automatiquement
       |
3. Generer la soumission SRP (pre-remplie automatiquement)
       |
4. Valider et exporter le bundle ZIP
       |
5. Soumettre a l'ENISA + notification CSIRT parallele
       |
6. Publier un avis de securite pour les utilisateurs
       |
7. Generer le rapport final une fois le correctif deploye
```

### Pipeline CI/CD automatise

```
1. Configurer le webhook GitHub/GitLab sur votre repo
       |
2. A chaque tag push : release creee automatiquement
       |
3. GitHub Action uploade le SBOM + score qualite
       |
4. Politique de securite evaluee (pass/warn/fail)
       |
5. Le dashboard affiche la fraicheur SBOM en temps reel
```

---

## Roles et permissions

| Action | ADMIN | COMPLIANCE_MANAGER | CONTRIBUTOR |
|--------|:-----:|:------------------:|:-----------:|
| Voir les produits et releases | Oui | Oui | Oui |
| Creer/Modifier/Supprimer un produit | Oui | Non | Non |
| Creer une release | Oui | Oui | Non |
| Uploader des preuves et SBOM | Oui | Oui | Oui |
| Scanner les vulnerabilites | Oui | Oui | Non |
| Prendre des decisions sur les findings | Oui | Oui | Non |
| Gerer les evenements CRA | Oui | Oui | Non |
| Generer les soumissions SRP | Oui | Oui | Non |
| Gerer la conformite et les risques | Oui | Oui | Non |
| Generer la declaration EU | Oui | Oui | Non |
| Utiliser l'assistant IA | Oui | Oui | Non |
| Creer une organisation | Oui | Non | Non |
| Gerer les cles d'API | Oui | Non | Non |

---

## Endpoints API

Tous les endpoints necessitent un JWT valide sauf indication contraire.

### Produits et Releases

| Methode | Path | Description |
|---------|------|-------------|
| `GET/POST` | `/api/v1/products` | Lister / Creer des produits |
| `GET/PUT/DELETE` | `/api/v1/products/{id}` | Detail / Modifier / Supprimer un produit |
| `GET/POST` | `/api/v1/products/{id}/releases` | Lister / Creer des releases |
| `GET/PUT/DELETE` | `/api/v1/releases/{id}` | Detail / Modifier / Supprimer une release |

### Preuves et SBOM

| Methode | Path | Description |
|---------|------|-------------|
| `POST` | `/api/v1/releases/{id}/evidences` | Uploader un fichier de preuve |
| `GET` | `/api/v1/releases/{id}/evidences` | Lister les preuves |
| `POST` | `/api/v1/releases/{id}/sbom` | Uploader un SBOM (CycloneDX/SPDX) |
| `GET` | `/api/v1/releases/{id}/components` | Lister les composants SBOM |
| `GET` | `/api/v1/releases/{id}/sbom/export?format=spdx\|cyclonedx` | Exporter le SBOM |
| `POST` | `/api/v1/releases/{id}/sbom/share` | Creer un lien de partage SBOM |

### Vulnerabilites

| Methode | Path | Description |
|---------|------|-------------|
| `POST` | `/api/v1/releases/{id}/scan` | Lancer un scan de vulnerabilites |
| `GET` | `/api/v1/releases/{id}/findings` | Lister les findings d'une release |
| `GET` | `/api/v1/products/{id}/findings` | Lister les findings d'un produit |
| `POST` | `/api/v1/findings/{id}/decisions` | Ajouter une decision a un finding |

### War Room CRA et SRP

| Methode | Path | Description |
|---------|------|-------------|
| `POST/GET` | `/api/v1/cra-events` | Creer / Lister les evenements CRA |
| `GET/PATCH` | `/api/v1/cra-events/{id}` | Detail / Modifier un evenement |
| `POST` | `/api/v1/cra-events/{id}/close` | Cloturer un evenement |
| `GET` | `/api/v1/cra-events/{id}/sla` | Consulter les SLA |
| `POST/GET` | `/api/v1/cra-events/{id}/submissions` | Creer / Lister les soumissions SRP |
| `POST` | `.../submissions/{subId}/validate` | Valider contre le schema |
| `GET` | `.../submissions/{subId}/export` | Exporter le bundle ZIP |
| `POST` | `.../submissions/{subId}/submit-parallel` | Notifier CSIRT en parallele |

### Conformite CRA

| Methode | Path | Description |
|---------|------|-------------|
| `GET/POST` | `/api/v1/products/{id}/checklist` | Checklist CRA Annexe I |
| `GET` | `/api/v1/products/{id}/readiness` | Score de preparation CRA |
| `GET/POST` | `/api/v1/products/{id}/conformity-assessment` | Evaluation de conformite |
| `GET/POST` | `/api/v1/products/{id}/risk-assessments` | Analyses de risques |
| `GET/POST` | `/api/v1/products/{id}/standards` | Normes appliquees |
| `GET/POST` | `/api/v1/products/{id}/eu-doc` | Declaration de conformite UE |
| `GET/POST` | `/api/v1/products/{id}/cvd-policy` | Politique de divulgation |

### Avis de securite

| Methode | Path | Description |
|---------|------|-------------|
| `POST/GET` | `/api/v1/security-advisories` | Creer / Lister les advisories |
| `POST` | `/api/v1/security-advisories/{id}/publish` | Publier un advisory |

### Pipeline CI/CD

| Methode | Path | Description |
|---------|------|-------------|
| `POST` | `/api/v1/ci/sbom` | Upload SBOM depuis CI (API key) |
| `GET/PUT` | `/api/v1/ci-policy` | Consulter / Configurer la politique CI |
| `POST` | `/integrations/github/webhook` | Webhook GitHub (HMAC-SHA256) |
| `POST` | `/integrations/gitlab/webhook` | Webhook GitLab |

### Divulgation coordonnee (CVD) - Endpoints publics

| Methode | Path | Description |
|---------|------|-------------|
| `POST` | `/api/v1/cvd/reports` | Soumettre un rapport de vulnerabilite |
| `GET` | `/api/v1/cvd/reports/{trackingId}/status` | Consulter le statut |
| `GET` | `/.well-known/security.txt` | Fichier security.txt auto-genere |

### Dashboard et Audit

| Methode | Path | Description |
|---------|------|-------------|
| `GET` | `/api/v1/dashboard` | Donnees du centre de controle |
| `GET` | `/api/v1/audit/verify` | Verifier l'integrite de la piste d'audit |
| `GET` | `/api/v1/audit/events` | Consulter les evenements d'audit |

### Assistant IA

| Methode | Path | Description |
|---------|------|-------------|
| `POST` | `/api/v1/ai/srp-draft` | Generer un brouillon SRP |
| `POST` | `/api/v1/ai/comm-pack` | Generer un pack de communication |
| `POST` | `/api/v1/ai/questionnaire/parse` | Parser un questionnaire |
| `POST` | `/api/v1/ai/questionnaire/fill` | Pre-remplir un questionnaire |

### Partage SBOM - Endpoint public

| Methode | Path | Description |
|---------|------|-------------|
| `GET` | `/share/sbom/{token}` | Telecharger un SBOM partage (lien temporaire) |

---

## Securite

| Couche | Mesure |
|--------|--------|
| **Multi-tenancy** | Isolation par `org_id` dans le JWT — chaque requete est filtree |
| **Autorisation** | Controle de roles sur chaque endpoint |
| **Audit trail** | Chaine de hash SHA-256 infalsifiable |
| **Stockage** | Verification d'integrite SHA-256 sur chaque fichier |
| **IA locale** | LLM Ollama — aucune donnee ne quitte l'infrastructure |
| **Redaction PII** | Masquage automatique avant traitement IA |
| **Rate limiting** | Configurable par endpoint |
| **Dependances** | Scan CVE automatise (OWASP dependency-check) |
| **Webhooks** | Verification HMAC-SHA256 |
| **API keys** | Authentification dediee pour les integrations CI/CD |

---

## Migrations de base de donnees

Les 32 migrations Flyway sont appliquees automatiquement au demarrage.

| Migration | Description |
|-----------|-------------|
| V001-V010 | Fondations : produits, releases, preuves, audit, composants |
| V011-V014 | Integrations : webhooks, SBOM, vulnerabilites, organisations |
| V015-V017 | Module CRA : war room, SLA, soumissions SRP, IA, index |
| V018-V020 | Cles API, webhooks sortants, avis de securite, escalade |
| V021-V024 | VEX, surveillance vulnerabilites, qualite SBOM, SBOM fournisseurs, partage |
| V025-V027 | Connecteur ENISA enrichi, CSIRT parallele |
| V028-V029 | Politique CVD, evaluation de conformite, analyse de risques, normes, EU DoC |
| V030 | Intake CVD (rapports de vulnerabilite publics) |
| V031-V032 | Mappings repos CI/CD, pipeline CI/CD |

---

## Tests

```bash
# Tests backend (248 tests unitaires)
cd backend && ./mvnw test

# Frontend
cd frontend && npm run dev    # puis tester manuellement

# Tous les tests
make test
```

**248 tests backend**, 0 echecs, couvrant : services, domaine, infrastructure, IA, securite.

---

## Configuration

SRPDesk se configure via variables d'environnement. Voir `.env.example` pour la liste complete (~75 variables).

### Variables essentielles

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | Hote PostgreSQL |
| `DB_PORT` | `5432` | Port PostgreSQL |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8180/realms/lexsecura` | URL Keycloak |
| `S3_ENDPOINT` | `http://localhost:9000` | Endpoint MinIO/S3 |
| `CORS_ORIGINS` | `http://localhost:3000` | Origines CORS |
| `GITHUB_WEBHOOK_SECRET` | *(vide)* | Secret webhook GitHub |
| `VITE_KEYCLOAK_URL` | `http://localhost:8180` | Keycloak (frontend) |

---

## Deploiement Docker complet

```bash
cd infra && docker compose up -d --build
```

| Service | Port |
|---------|------|
| PostgreSQL 16 | 15432 |
| Keycloak 24 | 18180 |
| MinIO | 9000 / 9001 |
| Backend | 18080 |
| Frontend | 13000 |

---

## Depannage

### La redirection Keycloak ne marche pas
Verifier que le frontend pointe vers le bon port Keycloak. Creer un fichier `frontend/.env.local` :
```
VITE_KEYCLOAK_URL=http://localhost:18180
```

### Le backend ne demarre pas
Verifier les ports : `DB_PORT=15432` pour le Docker Compose par defaut.

### Reset complet
```bash
cd infra && docker compose down -v && docker compose up -d
```

---

## Licence

Proprietary - All rights reserved.
