# ADR 005: Export Bundle ZIP vs Submit API Direct pour SRP

## Status
Accepted

## Context
Le Cyber Resilience Act (Art. 14) impose aux fabricants de declarer les vulnerabilites exploitees et incidents severes a la Single Reporting Platform (SRP) operee par ENISA. Deux approches sont possibles :

1. **Submit API direct** : appeler directement l'API SRP depuis le backend pour soumettre les declarations
2. **Export Bundle** : generer un ZIP (JSON + PDF + audit trail) que l'utilisateur telecharge et soumet manuellement ou via un connecteur configurable

## Decision
Adopter l'approche **Export Bundle** comme mecanisme principal, avec un **connecteur SRP stub** preparant l'integration future.

### Raisons principales

**L'API SRP n'existe pas encore.**
Au moment de cette decision (Q1 2026), ENISA n'a pas publie la specification technique de l'API SRP. Le reglement CRA prevoit la mise en place de la plateforme, mais les specifications d'interface ne sont pas finalisees. Construire un client pour une API inexistante serait premature et generateur de dette technique.

**Controle utilisateur.**
Les declarations CRA ont des consequences juridiques (Art. 64 : sanctions jusqu'a 15M EUR / 2.5% CA). Les organisations doivent pouvoir reviser, valider et approuver le contenu exact avant soumission. Un flux "generer → reviser → soumettre" est plus adapte qu'un envoi automatique.

**Auditabilite.**
Le bundle ZIP contient la preuve complete : le JSON structure de la soumission, la chaine d'audit verifiee (hash SHA-256), et un PDF lisible par un humain. Ce triplet constitue un dossier de conformite autonome, utilisable lors d'un audit meme sans acces au SaaS.

**Decouplage.**
Le port `SrpConnector` est defini comme interface dans la couche application. Le `StubSrpConnector` actuel log et retourne null. Quand l'API SRP sera specifiee, il suffira d'implementer un `EnisaSrpConnector` sans modifier les services ni les controllers.

## Alternatives considerees

| Alternative | Raison du rejet |
|---|---|
| Submit API direct (mock) | API inexistante, risque de deviner le format |
| Email automatique | Non conforme aux exigences CRA de declaration electronique |
| Integration PDF seul | Le JSON structure est necessaire pour l'interoperabilite machine |

## Consequences

### Positives
- Zero risque d'integration cassee quand l'API SRP changera
- Bundle auto-portant pour les audits
- L'utilisateur garde le controle total avant soumission
- Architecture prete pour l'integration future (port/adapter)

### Negatives
- Etape manuelle de soumission (upload du ZIP sur la plateforme ENISA)
- Pas de notification de retour automatique (accusé de reception)
- Le champ `submittedReference` est renseigne manuellement par l'utilisateur

## Plan d'evolution
1. **Phase actuelle** : Export Bundle + stub connector
2. **Quand l'API SRP est publiee** : Implementer `EnisaSrpConnector`, ajouter un bouton "Submit to SRP" dans l'UI
3. **Phase finale** : Soumission automatique avec accusé de reception stocké comme evidence
