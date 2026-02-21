# LexSecura — Presentation Client

## Pitch (30 secondes)

> A partir de septembre 2026, le Reglement europeen CRA (Cyber Resilience Act) impose a tout editeur logiciel et fabricant IoT de **prouver la securite de ses produits** sous peine de sanctions pouvant aller jusqu'a **15 millions d'euros ou 2,5% du CA mondial**.
>
> Concretement : il faut tracer chaque composant, chaque vulnerabilite, chaque decision prise — et quand un incident se produit, il faut notifier les autorites en **24 heures**.
>
> **LexSecura** est le cockpit qui centralise tout ca. En un clic, vos equipes ont la preuve de conformite, les brouillons de notification, et les dossiers prets pour l'auditeur. Au lieu de gerer ca avec Excel, mails et panique, vous avez un processus guide, tracable, et des deadlines respectees.

---

## Le probleme global

Aujourd'hui, quand une entreprise doit se conformer au CRA, elle fait face a :

- **Pas d'outil dedie** : tout se gere dans Excel, Confluence, mails, Slack
- **Dispersion des preuves** : les SBOM sont sur un serveur, les rapports de scan ailleurs, les decisions dans des mails
- **Panique en cas d'incident** : "On a 24h pour notifier l'ENISA, par ou on commence ?"
- **Risque d'audit** : "Prouvez-moi que vous n'avez pas modifie ce rapport apres coup"
- **Charge repetitive** : remplir les memes questionnaires securite pour chaque client

LexSecura resout chacun de ces problemes.

---

## Les fonctionnalites expliquees

---

### 1. Gestion des Produits et Releases

#### Le probleme
Le CRA exige de maintenir un inventaire de chaque produit numerique mis sur le marche europeen, avec chaque version livree. La plupart des entreprises n'ont pas de registre centralise — les versions sont dispersees entre Git, Jira, et la memoire des developpeurs.

#### La solution
LexSecura permet d'enregistrer chaque produit avec sa classification CRA (standard, classe I, classe II, critique) et de suivre chaque version (release) avec son tag Git, son build ID, et son statut.

#### Pourquoi c'est important
En cas d'audit, la premiere question est : "Quels produits vendez-vous et quelles versions sont en circulation ?" Si vous ne pouvez pas repondre en 5 minutes, ca commence mal.

#### Exemple concret
> Votre produit "IoT Gateway" a 3 versions en production (1.0, 1.1, 2.0). Un auditeur demande la liste. Avec LexSecura : 2 clics. Sans LexSecura : 3 heures a fouiller Git et Jira.

---

### 2. Collecte de Preuves (Evidences)

#### Le probleme
Le CRA exige des **preuves tangibles** de conformite : rapports de tests, scans de vulnerabilites, plans de reponse aux incidents, declarations de conformite... Ces fichiers sont eparpilles sur des drives, des serveurs CI, des boites mail.

#### La solution
LexSecura centralise toutes les preuves par version de produit. Chaque fichier uploade est stocke avec un **hash SHA-256** qui garantit qu'il n'a pas ete modifie apres coup. Types supportes : SBOM, rapports de tests, scans de vulnerabilites, rapports de pentest, documentation de conception, plans de reponse aux incidents, declarations de conformite.

#### Pourquoi c'est important
Un auditeur ne demande pas seulement "est-ce que vous faites des tests ?" — il demande "**montrez-moi la preuve**, et prouvez-moi qu'elle n'a pas ete modifiee depuis". Le hash SHA-256 repond a cette deuxieme question.

#### Exemple concret
> L'auditeur demande : "Montrez-moi le rapport de pentest de la version 2.0." Avec LexSecura : clic sur la release 2.0 → onglet Preuves → le fichier est la avec sa date d'upload et son hash d'integrite. Sans LexSecura : "Attends, je crois que Jean l'avait mis sur le drive... ou c'etait par mail ?"

---

### 3. SBOM (Software Bill of Materials)

#### Le probleme
Le CRA impose de fournir un **inventaire complet des composants logiciels** de chaque produit. C'est comme la liste d'ingredients sur un produit alimentaire, mais pour le logiciel. Votre produit utilise peut-etre 200 librairies open source — il faut toutes les lister.

#### La solution
LexSecura parse les fichiers SBOM au format CycloneDX (le standard industrie). Vous uploadez votre SBOM genere par votre CI/CD, et LexSecura extrait automatiquement chaque composant avec son nom, sa version, et son identifiant universel (PURL).

#### Pourquoi c'est important
Si une vulnerabilite critique est decouverte dans une librairie (ex: Log4Shell), il faut savoir **en quelques minutes** quels produits et quelles versions l'utilisent. Sans SBOM centralise, c'est des jours de travail.

#### Exemple concret
> "Est-ce qu'on utilise la librairie `lodash` quelque part ?" Avec LexSecura : recherche dans les composants → oui, dans IoT Gateway v1.0 et v1.1. Sans LexSecura : "Faut regarder les package.json de chaque projet..."

---

### 4. Scan de Vulnerabilites et Findings

#### Le probleme
Avoir un SBOM c'est bien, mais il faut aussi **surveiller les vulnerabilites connues** dans ces composants. Chaque jour, de nouvelles CVE sont publiees. Manuellement, c'est impossible a suivre pour 200+ composants.

#### La solution
LexSecura interroge automatiquement l'**API OSV** (base mondiale de vulnerabilites open source, maintenue par Google) pour chaque composant de votre SBOM. Les vulnerabilites trouvees apparaissent comme des "Findings" avec leur severite (Critique, Haute, Moyenne, Basse).

#### Pourquoi c'est important
Le CRA impose de "gerer les vulnerabilites de maniere effective" (Article 13). Cela signifie : les detecter, les evaluer, et documenter vos decisions. LexSecura automatise la detection.

#### Exemple concret
> Vous lancez un scan sur votre release 2.0. LexSecura detecte 12 vulnerabilites dont 2 critiques. Chaque finding affiche : quel composant est touche, la severite, un lien vers l'advisory officiel (GHSA/CVE).

---

### 5. Decisions sur les Findings

#### Le probleme
Detecter une vulnerabilite ne suffit pas — il faut **documenter ce que vous faites**. Certaines vulns ne vous affectent pas (vous n'utilisez pas la fonctionnalite concernee), d'autres necessitent un patch, d'autres sont mitigees autrement. L'auditeur veut voir cette analyse, pas juste "on a scanne".

#### La solution
Pour chaque finding, un responsable peut enregistrer une decision :
- **Non affecte** : "Cette vuln concerne le module XML, on ne l'utilise pas"
- **Correctif prevu** : "Patch planifie pour le 15 mars" (avec deadline)
- **Attenue** : "WAF en place qui bloque le vecteur d'attaque"
- **Corrige** : "Mis a jour vers la version 2.1 qui corrige le probleme"

Chaque decision exige une **justification ecrite** — c'est la trace d'audit.

#### Pourquoi c'est important
Avoir 50 vulns ouvertes n'est pas un probleme en soi. N'avoir aucune justification pour chacune, c'est le probleme. L'auditeur veut voir que vous avez **analyse et decide**, pas ignore.

#### Exemple concret
> L'auditeur : "Vous avez une CVE critique sur lodash, qu'avez-vous fait ?" Avec LexSecura : "Voici la decision 'Non affecte' prise le 12 fevrier par Marie, avec la justification : nous n'utilisons pas la fonction `template()` concernee par cette CVE." Sans LexSecura : "Euh... je crois qu'on en avait parle en standup..."

---

### 6. CRA War Room (Gestion de Crise)

#### Le probleme
Le CRA impose des **deadlines strictes** quand un incident de securite touche votre produit :
- **24 heures** : Alerte precoce a l'ENISA (l'agence EU de cybersecurite)
- **72 heures** : Notification detaillee avec analyse d'impact
- **14 ou 30 jours** : Rapport final complet

Rater ces deadlines = sanctions. Et en pleine crise, tout le monde panique, personne ne sait qui fait quoi, et les deadlines passent.

#### La solution
La War Room est l'ecran de gestion de crise. Quand un incident survient :
1. On cree un **evenement CRA** (vulnerabilite exploitee ou incident severe)
2. On lie les produits, releases, findings et preuves concernes
3. On assigne des responsables (Owner, Approver, Viewer)
4. Des **compteurs en temps reel** affichent le temps restant avant chaque deadline
5. On suit l'avancement : ouvert → en cours → resolu → ferme

#### Pourquoi c'est important
Sans outil dedie, en situation de crise : quelqu'un ecrit sur Slack "il nous reste combien de temps pour la notification ?", un autre repond "je sais pas, c'etait quand la detection deja ?", un troisieme est en vacances... La War Room met tout le monde sur la meme page.

#### Exemple concret
> Vendredi 17h : une vuln zero-day est exploitee sur votre firmware. Le compteur 24h demarre. La War Room montre : il reste 23h47 pour l'alerte precoce. Pierre est Owner, Marie est Approver. Les 3 releases affectees sont liees. Les findings pertinents sont rattaches. Tout le monde sait exactement ou on en est.

---

### 7. Brouillon SRP (Soumissions ENISA)

#### Le probleme
Les soumissions a l'ENISA via la Single Reporting Platform (SRP) sont des documents structures avec des dizaines de champs obligatoires : resume, versions affectees, impact, mesures de mitigation, timeline, references... Les rediger manuellement en 24h sous pression, c'est tres difficile.

#### La solution
L'assistant IA analyse les donnees deja presentes dans LexSecura (produit, releases, findings, preuves, decisions) et **genere un brouillon** de soumission SRP. Trois types :
- **Alerte Precoce** (24h) : resume rapide de l'incident
- **Notification** (72h) : analyse d'impact detaillee
- **Rapport Final** (14/30j) : rapport complet avec timeline et mitigations

Le brouillon est un **point de depart** — un humain le relit, l'ajuste et le valide avant envoi.

#### Pourquoi c'est important
En 24h, vous n'avez pas le temps de rediger un document structure de zero. L'IA vous fait gagner 80% du temps de redaction. Et comme elle utilise uniquement vos donnees internes (pas d'hallucination a partir d'internet), les informations sont fiables.

#### Exemple concret
> Il est 23h, la deadline de 24h approche. Au lieu de partir d'une page blanche, vous cliquez "Generer un brouillon Alerte Precoce". En 30 secondes, vous avez un document structure avec le resume de l'incident, les versions affectees, le statut du patch — le tout tire de vos donnees. Vous relisez, ajustez 2-3 phrases, et soumettez.

---

### 8. Communication Pack (Comm. Pack)

#### Le probleme
En plus de notifier les autorites (ENISA), le CRA vous impose aussi d'**informer vos clients** quand une vulnerabilite les affecte. Il faut rediger :
- Un **advisory securite** (document formel pour le site web)
- Un **email client** (clair, actionnable)
- Des **release notes securite** (pour les developpeurs de vos clients)

En pleine crise, rediger 3 documents de communication coherents prend des heures.

#### La solution
L'IA genere les 3 documents d'un coup a partir de l'evenement CRA. Chaque document a le bon ton :
- L'advisory est formel et technique
- L'email est clair et oriente action ("mettez a jour vers la version X")
- Les release notes sont concises pour les devs

Vous copiez, ajustez, envoyez.

#### Pourquoi c'est important
Une mauvaise communication de crise peut faire plus de degats que la vulnerabilite elle-meme. Des clients mal informes perdent confiance. Un advisory trop vague cree de la panique. L'IA produit une base professionnelle et coherente.

#### Exemple concret
> Votre vuln critique est patchee. Vous cliquez "Generer Comm Pack". Vous obtenez :
> - Advisory : "# Avis de securite - IoT Gateway / CVE-2026-1234 / Severite : CRITIQUE..."
> - Email : "Objet: [Action requise] Mise a jour de securite IoT Gateway..."
> - Release notes : "## v2.1.0 - Correctif de securite..."
> Vous relisez, ajustez le ton, et publiez les 3 en 15 minutes au lieu de 3 heures.

---

### 9. Questionnaire Securite (Autopilot)

#### Le probleme
Quand vous vendez votre logiciel a un grand compte (banque, hopital, assurance, industrie...), leur service achats/securite vous envoie un **questionnaire de securite** avant de signer. C'est un fichier Excel ou Word avec 50 a 200 questions :

- "Maintenez-vous un inventaire des composants tiers (SBOM) ?"
- "A quelle frequence effectuez-vous des scans de vulnerabilites ?"
- "Disposez-vous d'un plan de reponse aux incidents ?"
- "Comment gerez-vous les mises a jour de securite ?"
- "Vos donnees sont-elles chiffrees au repos et en transit ?"
- ...

Chaque client envoie **son propre questionnaire** avec des formulations differentes mais les memes questions de fond. Vos equipes passent des **jours** a remplir ces questionnaires, souvent les memes reponses, a chaque nouveau prospect.

#### La solution
1. Vous uploadez le questionnaire (xlsx, docx, txt, csv)
2. LexSecura le parse et extrait les questions
3. L'IA pre-remplit les reponses en se basant sur les donnees de votre organisation (produits, releases, preuves, scans, decisions)
4. Chaque reponse a un **niveau de confiance** (Haute, Moyenne, Basse, Inconnue)
5. Vous exportez en JSON ou CSV, vous relisez, ajustez, et renvoyez

#### Pourquoi c'est important
- Un commercial passe parfois **une semaine** a remplir un questionnaire securite
- Les reponses sont souvent les memes d'un client a l'autre
- Si la reponse dit "oui, on fait des scans" mais que LexSecura a les preuves, c'est encore plus credible
- Le niveau de confiance "INCONNUE" signale les questions ou il faut verifier manuellement — vous ne risquez pas de laisser passer une reponse fausse

#### Exemple concret
> Un prospect vous envoie un questionnaire Excel de 80 questions. Au lieu d'y passer 3 jours, vous l'uploadez dans LexSecura. L'IA remplit 60 reponses avec confiance Haute (car les preuves existent dans le systeme), 15 en Moyenne, et 5 en Inconnue. Vous relisez les 20 reponses a verifier, exportez le CSV, et renvoyez en 2 heures.

---

### 10. Piste d'Audit (Audit Trail)

#### Le probleme
Un auditeur ou une autorite de marche veut verifier que vos donnees de conformite sont **authentiques** — que personne n'a modifie un rapport de scan apres coup, supprime un finding genante, ou change une date de detection.

#### La solution
Chaque action dans LexSecura est enregistree dans une **chaine de hash SHA-256** :
- Chaque evenement est lie au precedent par son hash
- Si quelqu'un modifie un enregistrement en base de donnees, la chaine se casse
- Le dashboard affiche en permanence si la chaine est integre (vert) ou corrompue (rouge)
- On peut verifier la chaine a tout moment

C'est le meme principe qu'une blockchain, mais sans la complexite — c'est une chaine de confiance interne.

#### Pourquoi c'est important
Le CRA impose la "tracabilite des actions de conformite". Mais au-dela de la loi, c'est une question de **confiance** : si vous pouvez prouver mathematiquement que vos donnees n'ont pas ete alterees, votre credibilite aupres des auditeurs est totale.

#### Exemple concret
> L'auditeur demande : "Comment je sais que ce scan de vulnerabilite a bien ete fait le 12 fevrier et pas backdatee ?" Vous lui montrez la piste d'audit : l'evenement "Scan lance" du 12 fevrier est chaine avec les evenements precedents et suivants. Modifier la date casserait la chaine. L'auditeur verifie → chaine intacte → confiance totale.

---

### 11. Export Compliance Pack

#### Le probleme
L'auditeur ne va pas se connecter a votre outil. Il veut un **dossier complet** qu'il peut lire offline, archiver, et partager avec ses collegues.

#### La solution
En un clic, LexSecura genere un ZIP contenant :
- Un **rapport PDF professionnel** (tableaux colores, resume executif, liste complete des composants, vulnerabilites avec decisions, hash d'audit)
- Un **fichier JSON structure** (pour traitement automatise)

Le PDF est en francais, avec mise en page professionnelle : bandeau en-tete, cartes resume, tableaux zebres, badges de severite colores, pagination.

#### Pourquoi c'est important
Un PDF bien presente inspire confiance. Un dump JSON brut, non. Le rapport montre que vous avez un processus mature — pas juste un script qui tourne.

---

## Recapitulatif par profil

### Pour le CEO / Direction Generale

> "LexSecura, c'est votre assurance conformite CRA. Au lieu de risquer **15M d'euros d'amende** parce qu'un developpeur n'a pas documente une decision, tout est trace, prouvable, et exportable. En cas d'incident, vos equipes savent exactement quoi faire et les deadlines sont respectees."

### Pour le RSSI / Directeur Securite

> "LexSecura centralise votre cycle de gestion des vulnerabilites : SBOM → scan → findings → decisions → preuves → audit trail. Quand un zero-day tombe, la War Room vous donne les compteurs SLA en temps reel, et l'IA genere les brouillons de notification ENISA en 30 secondes. Plus besoin de partir d'une page blanche a 3h du matin."

### Pour le Compliance Manager

> "LexSecura automatise 80% de votre travail repetitif. Les questionnaires securite clients sont pre-remplis par l'IA. Les soumissions SRP sont brouillonnees automatiquement. Les rapports de conformite sont generes en un clic avec tous les composants, toutes les vulns, toutes les decisions. Vous relisez et validez au lieu de rediger from scratch."

### Pour le Commercial / Business Developer

> "Quand un prospect vous envoie un questionnaire securite de 100 questions, au lieu d'y passer une semaine vous l'uploadez dans LexSecura → l'IA pre-remplit les reponses en 2 minutes → vous ajustez et renvoyez le jour meme. C'est un avantage competitif : vous repondez plus vite que la concurrence."

### Pour le Developpeur / DevSecOps

> "LexSecura s'integre dans votre CI/CD : webhook GitLab pour creer les releases automatiquement, upload SBOM CycloneDX, scan de vulnerabilites via API OSV. Les composants et vulns sont traces par version. Vous n'avez plus a maintenir un Excel de suivi des CVE."

---

## Speech complet (3 minutes)

> Mesdames, Messieurs,
>
> En septembre 2026, le Reglement europeen CRA entre en application. A partir de cette date, tout editeur logiciel et fabricant IoT qui met un produit sur le marche europeen devra **prouver sa securite** — sous peine de sanctions pouvant atteindre 15 millions d'euros.
>
> Concretement, ca veut dire : maintenir un inventaire de vos composants logiciels, surveiller les vulnerabilites, documenter chaque decision de correction, et en cas d'incident — notifier les autorites en 24 heures. Pas 24 jours. 24 heures.
>
> Aujourd'hui, la plupart des entreprises gerent ca avec Excel, des mails, et beaucoup de bonne volonte. Ca fonctionne quand tout va bien. Mais quand un zero-day tombe un vendredi soir et que vous avez 24 heures pour notifier l'ENISA — Excel ne suffit plus.
>
> C'est pour ca que nous avons cree **LexSecura**.
>
> LexSecura est un cockpit de conformite CRA qui fait trois choses :
>
> **Premierement**, il centralise vos preuves. Chaque version de chaque produit a son SBOM, ses rapports de scan, ses decisions de correction, le tout avec verification d'integrite SHA-256. Quand un auditeur demande "montrez-moi la preuve", vous l'avez en deux clics.
>
> **Deuxiemement**, il gere la crise. Quand un incident survient, la War Room affiche les deadlines en temps reel — 23 heures restantes pour l'alerte precoce, 71 heures pour la notification. L'IA genere les brouillons de soumission et les communications client. Vos equipes savent qui fait quoi, et les deadlines sont respectees.
>
> **Troisiemement**, il prouve votre bonne foi. La piste d'audit fonctionne comme une blockchain : chaque action est chainee par un hash SHA-256. Si quelqu'un modifie un enregistrement, la chaine casse. L'auditeur peut verifier mathematiquement que vos donnees sont authentiques.
>
> Et parce que vos donnees sont sensibles, notre assistant IA tourne **localement** — aucune donnee ne quitte votre infrastructure. Les emails, adresses IP et tokens sont automatiquement masques avant traitement. Chaque sortie IA est validee contre un schema strict. Et surtout, chaque brouillon necessite une **validation humaine** avant envoi.
>
> LexSecura, c'est la difference entre "on gere a vue" et "on a un processus auditable". Entre une amende de 15 millions et un audit reussi.
>
> Merci.
