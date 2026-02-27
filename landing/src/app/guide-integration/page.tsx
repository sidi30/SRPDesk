"use client";

import {
  Server,
  KeyRound,
  PackagePlus,
  ShieldCheck,
  FileCode2,
  AlertTriangle,
  BarChart3,
  CheckCircle2,
  ArrowRight,
  Clock,
  Users,
  FileText,
  Settings,
} from "lucide-react";
import SectionTitle from "@/components/ui/SectionTitle";
import CtaSection from "@/components/landing/CtaSection";
import ScrollReveal from "@/components/ui/ScrollReveal";

interface Step {
  number: number;
  icon: React.ElementType;
  title: string;
  duration: string;
  description: string;
  tasks: string[];
  tip?: string;
}

const steps: Step[] = [
  {
    number: 1,
    icon: Server,
    title: "Deployer l'infrastructure",
    duration: "~30 minutes",
    description:
      "SRPDesk se deploie via Docker Compose avec un fichier .env unique pour toute la configuration. L'infrastructure comprend la base de donnees, le stockage S3, le serveur d'authentification et le backend.",
    tasks: [
      "Installer Docker et Docker Compose sur votre serveur",
      "Cloner le repository et copier .env.example en .env",
      "Configurer les 5 secrets obligatoires (mots de passe BDD, Keycloak, S3)",
      "Lancer docker compose -f docker-compose.prod.yml up -d",
      "Verifier que tous les services sont healthy avec docker compose ps",
    ],
    tip: "En developpement, tout fonctionne en local avec les valeurs par defaut. En production, nous recommandons un serveur avec 4 vCPU et 8 Go de RAM minimum. Toutes les limites memoire des conteneurs sont parametrables via .env.",
  },
  {
    number: 2,
    icon: Settings,
    title: "Configurer l'environnement",
    duration: "~20 minutes",
    description:
      "Le fichier .env.example contient ~75 variables documentees par categorie. Chaque variable a une valeur par defaut sensible — vous n'avez a modifier que ce qui differe de votre infrastructure.",
    tasks: [
      "Configurer le domaine et les origines CORS (CORS_ORIGINS, KC_HOSTNAME)",
      "Ajuster les limites de performance si besoin (pool BDD, threads Tomcat, JVM)",
      "Configurer l'email SMTP pour les alertes CRA (SMTP_HOST, EMAIL_ENABLED)",
      "Activer les modules optionnels (IA, ENISA, CSIRT, feature flags)",
    ],
    tip: "Les variables sont organisees par categorie : infrastructure, securite, performance, email, CRA, integrations, IA, observabilite. Seules les 5 variables REQUIRED n'ont pas de defaut sur.",
  },
  {
    number: 3,
    icon: KeyRound,
    title: "Configurer l'authentification",
    duration: "~30 minutes",
    description:
      "SRPDesk utilise Keycloak pour l'authentification SSO. Configurez votre realm et vos utilisateurs pour demarrer.",
    tasks: [
      "Acceder a la console Keycloak (port 8180 par defaut)",
      "Creer les utilisateurs de votre equipe",
      "Attribuer les roles : ADMIN, COMPLIANCE_MANAGER ou CONTRIBUTOR",
      "Configurer votre fournisseur d'identite (LDAP, Google, Azure AD) si besoin",
    ],
    tip: "Le role ADMIN peut tout gerer. Le COMPLIANCE_MANAGER gere les produits et la conformite. Le CONTRIBUTOR peut ajouter des preuves et des composants.",
  },
  {
    number: 4,
    icon: PackagePlus,
    title: "Enregistrer vos produits numeriques",
    duration: "~15 minutes par produit",
    description:
      "Declarez chaque produit numerique que votre entreprise met sur le marche europeen. SRPDesk calcule automatiquement la classe CRA et le parcours de conformite.",
    tasks: [
      "Creer un produit avec son nom, sa description et sa categorie",
      "Indiquer le type CRA : DEFAULT, CLASS_I, CLASS_II ou CRITICAL",
      "Verifier le parcours de conformite calcule automatiquement",
      "Ajouter les contacts responsables du produit",
    ],
    tip: "La majorite des logiciels B2B sont en categorie DEFAULT (auto-evaluation). Les produits d'infrastructure reseau sont souvent CLASS_I ou CLASS_II.",
  },
  {
    number: 5,
    icon: FileCode2,
    title: "Importer votre premier SBOM",
    duration: "~10 minutes",
    description:
      "Le Software Bill of Materials est obligatoire sous le CRA. SRPDesk supporte les formats CycloneDX et SPDX, et detecte automatiquement les composants.",
    tasks: [
      "Generer un SBOM avec votre outil de build (Syft, cdxgen, Trivy...)",
      "Creer une release pour votre produit dans SRPDesk",
      "Uploader le fichier SBOM (JSON, max 10 Mo)",
      "Verifier les composants detectes et leurs versions",
    ],
    tip: "Integrez la generation du SBOM dans votre pipeline CI/CD pour que chaque release ait automatiquement son inventaire a jour.",
  },
  {
    number: 6,
    icon: AlertTriangle,
    title: "Scanner et gerer les vulnerabilites",
    duration: "~20 minutes",
    description:
      "SRPDesk identifie les vulnerabilites connues dans vos composants via OSV. Pour chaque finding, documentez votre decision : corrige, mitige, non affecte.",
    tasks: [
      "Lancer un scan de vulnerabilites sur votre release",
      "Examiner les findings par severite (CRITICAL, HIGH, MEDIUM, LOW)",
      "Prendre une decision pour chaque finding avec une justification",
      "Joindre les preuves de correction ou de mitigation",
    ],
    tip: "Le CRA exige que chaque vulnerabilite exploitee activement soit notifiee a l'ENISA sous 24h. Automatisez le scan pour ne rien rater.",
  },
  {
    number: 7,
    icon: ShieldCheck,
    title: "Completer la checklist CRA Annexe I",
    duration: "~2 heures par produit",
    description:
      "Les 21 exigences de l'Annexe I du CRA sont pre-chargees pour chaque produit. Evaluez votre conformite point par point et liez vos preuves.",
    tasks: [
      "Initialiser la checklist CRA pour votre produit",
      "Evaluer chaque exigence : Conforme, Partiellement conforme, Non conforme",
      "Ajouter des notes explicatives pour chaque point",
      "Lier les preuves (rapports de test, certifications, documentation technique)",
    ],
    tip: "Commencez par les 13 exigences de 'Security by Design' (Partie I) puis les 8 exigences de 'Vulnerability Management' (Partie II).",
  },
  {
    number: 8,
    icon: BarChart3,
    title: "Suivre votre score de readiness",
    duration: "Continu",
    description:
      "Le score de readiness (0-100) vous donne une vue d'ensemble de votre preparation CRA. Il se met a jour automatiquement a chaque action dans SRPDesk.",
    tasks: [
      "Consulter le score global et par categorie sur le dashboard",
      "Identifier les points faibles via les recommandations d'action",
      "Prendre des snapshots reguliers pour suivre la progression",
      "Viser un score > 80 avant la date limite de septembre 2026",
    ],
    tip: "Le score prend en compte 5 dimensions : Security by Design, Vulnerability Management, SBOM Management, Incident Reporting et Documentation.",
  },
  {
    number: 9,
    icon: FileText,
    title: "Preparer les rapports de conformite",
    duration: "~30 minutes par release",
    description:
      "Generez des compliance packs complets en un clic : rapport JSON structure + PDF lisible avec toutes vos preuves, composants, vulnerabilites et decisions.",
    tasks: [
      "Generer un compliance pack pour chaque release de produit",
      "Verifier le rapport : composants, vulnerabilites, decisions, preuves",
      "Exporter le ZIP (JSON + PDF) pour votre dossier de conformite",
      "L'audit trail SHA-256 garantit l'integrite de toutes les donnees",
    ],
    tip: "Conservez ces rapports dans vos archives. En cas de controle, vous pouvez prouver l'integrite de votre dossier grace a la chaine de hachage.",
  },
];

const milestones = [
  {
    date: "Septembre 2026",
    label: "Obligations de reporting Article 14",
    description:
      "Notification obligatoire des vulnerabilites exploitees a l'ENISA sous 24h.",
    urgent: true,
  },
  {
    date: "Septembre 2027",
    label: "Conformite complete CRA",
    description:
      "Toutes les exigences de l'Annexe I doivent etre respectees pour les produits sur le marche.",
    urgent: false,
  },
];

function StepCard({ step }: { step: Step }) {
  return (
    <div className="relative bg-dark-card border border-dark-border rounded-2xl p-8 hover:border-accent/30 transition-colors duration-300 group">
      {/* Step number badge */}
      <div className="absolute -top-4 -left-2 w-10 h-10 rounded-full bg-accent text-dark font-bold flex items-center justify-center text-lg shadow-lg shadow-accent/20">
        {step.number}
      </div>

      <div className="flex items-start gap-4 mb-4 mt-2">
        <div className="p-3 rounded-xl bg-accent/10 text-accent group-hover:bg-accent/20 transition-colors">
          <step.icon className="w-6 h-6" />
        </div>
        <div className="flex-1">
          <h3 className="text-xl font-semibold text-text-light mb-1">
            {step.title}
          </h3>
          <div className="flex items-center gap-2 text-text-muted-dark text-sm">
            <Clock className="w-4 h-4" />
            <span>{step.duration}</span>
          </div>
        </div>
      </div>

      <p className="text-text-muted-dark mb-5 leading-relaxed">
        {step.description}
      </p>

      <ul className="space-y-3 mb-5">
        {step.tasks.map((task) => (
          <li key={task} className="flex items-start gap-3">
            <CheckCircle2 className="w-5 h-5 text-accent shrink-0 mt-0.5" />
            <span className="text-text-light text-sm">{task}</span>
          </li>
        ))}
      </ul>

      {step.tip && (
        <div className="bg-accent/5 border border-accent/20 rounded-xl p-4">
          <p className="text-sm text-text-muted-dark">
            <span className="text-accent font-semibold">Conseil : </span>
            {step.tip}
          </p>
        </div>
      )}
    </div>
  );
}

export default function GuideIntegrationPage() {
  return (
    <>
      {/* Hero */}
      <section className="py-20 md:py-28 bg-dark">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <ScrollReveal>
            <SectionTitle
              badge="Guide d'integration"
              title="Deployez SRPDesk en 9 etapes simples"
              subtitle="De l'installation a la conformite CRA complete, suivez ce guide pas a pas pour integrer SRPDesk dans votre organisation."
              dark
            />
          </ScrollReveal>

          {/* Quick summary */}
          <ScrollReveal delay={200}>
            <div className="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-dark-card border border-dark-border rounded-xl p-6 text-center">
                <Clock className="w-8 h-8 text-accent mx-auto mb-3" />
                <div className="text-2xl font-bold text-text-light">
                  &frac12; journee
                </div>
                <div className="text-text-muted-dark text-sm mt-1">
                  pour le deploiement initial
                </div>
              </div>
              <div className="bg-dark-card border border-dark-border rounded-xl p-6 text-center">
                <Users className="w-8 h-8 text-cyan mx-auto mb-3" />
                <div className="text-2xl font-bold text-text-light">
                  3 roles
                </div>
                <div className="text-text-muted-dark text-sm mt-1">
                  Admin, Compliance Manager, Contributor
                </div>
              </div>
              <div className="bg-dark-card border border-dark-border rounded-xl p-6 text-center">
                <ShieldCheck className="w-8 h-8 text-accent mx-auto mb-3" />
                <div className="text-2xl font-bold text-text-light">
                  21 exigences
                </div>
                <div className="text-text-muted-dark text-sm mt-1">
                  Annexe I du CRA couvertes
                </div>
              </div>
            </div>
          </ScrollReveal>
        </div>
      </section>

      {/* Timeline milestones */}
      <section className="py-16 bg-dark-card">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <ScrollReveal>
            <SectionTitle
              badge="Echeances"
              title="Dates cles a retenir"
              subtitle="Le CRA entre en vigueur progressivement. Voici les echeances qui vous concernent."
              dark
            />
          </ScrollReveal>
          <ScrollReveal delay={200}>
            <div className="mt-10 flex flex-col md:flex-row gap-6 justify-center">
              {milestones.map((m) => (
                <div
                  key={m.date}
                  className={`flex-1 max-w-md border rounded-xl p-6 ${
                    m.urgent
                      ? "border-red bg-red/5"
                      : "border-dark-border bg-dark"
                  }`}
                >
                  <div
                    className={`text-sm font-semibold mb-2 ${
                      m.urgent ? "text-red" : "text-amber"
                    }`}
                  >
                    {m.date}
                  </div>
                  <h3 className="text-lg font-semibold text-text-light mb-2">
                    {m.label}
                  </h3>
                  <p className="text-text-muted-dark text-sm">
                    {m.description}
                  </p>
                </div>
              ))}
            </div>
          </ScrollReveal>
        </div>
      </section>

      {/* Steps */}
      <section className="py-20 md:py-28 bg-dark">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <ScrollReveal>
            <SectionTitle
              badge="Pas a pas"
              title="Les 9 etapes de l'integration"
              subtitle="Chaque etape est concue pour etre realisable de maniere autonome. Suivez l'ordre recommande pour un deploiement optimal."
              dark
            />
          </ScrollReveal>

          <div className="mt-14 space-y-10">
            {steps.map((step) => (
              <ScrollReveal key={step.number}>
                <StepCard step={step} />
              </ScrollReveal>
            ))}
          </div>
        </div>
      </section>

      {/* After integration */}
      <section className="py-20 md:py-28 bg-dark-card">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <ScrollReveal>
            <SectionTitle
              badge="Et apres ?"
              title="Bonnes pratiques pour rester conforme"
              subtitle="Une fois SRPDesk en place, adoptez ces habitudes pour maintenir votre conformite dans la duree."
              dark
            />
          </ScrollReveal>
          <ScrollReveal stagger>
            <div className="mt-10 grid grid-cols-1 md:grid-cols-2 gap-8">
              <div className="bg-dark border border-dark-border rounded-xl p-6">
                <div className="flex items-center gap-3 mb-4">
                  <div className="p-2 rounded-lg bg-accent/10 text-accent">
                    <FileCode2 className="w-5 h-5" />
                  </div>
                  <h3 className="text-lg font-semibold text-text-light">
                    SBOM a chaque release
                  </h3>
                </div>
                <p className="text-text-muted-dark text-sm leading-relaxed">
                  Integrez la generation du SBOM dans votre CI/CD. Chaque
                  nouvelle version de produit doit avoir son inventaire de
                  composants a jour. Utilisez CycloneDX ou SPDX selon votre
                  ecosysteme.
                </p>
              </div>

              <div className="bg-dark border border-dark-border rounded-xl p-6">
                <div className="flex items-center gap-3 mb-4">
                  <div className="p-2 rounded-lg bg-cyan/10 text-cyan">
                    <AlertTriangle className="w-5 h-5" />
                  </div>
                  <h3 className="text-lg font-semibold text-text-light">
                    Scan de vulnerabilites regulier
                  </h3>
                </div>
                <p className="text-text-muted-dark text-sm leading-relaxed">
                  Planifiez des scans hebdomadaires minimum. Les nouvelles CVE
                  sont publiees quotidiennement. Un scan regulier vous permet de
                  reagir avant qu&apos;une vulnerabilite ne soit exploitee.
                </p>
              </div>

              <div className="bg-dark border border-dark-border rounded-xl p-6">
                <div className="flex items-center gap-3 mb-4">
                  <div className="p-2 rounded-lg bg-accent/10 text-accent">
                    <ShieldCheck className="w-5 h-5" />
                  </div>
                  <h3 className="text-lg font-semibold text-text-light">
                    Revue trimestrielle de la checklist
                  </h3>
                </div>
                <p className="text-text-muted-dark text-sm leading-relaxed">
                  Re-evaluez votre checklist CRA Annexe I tous les trimestres.
                  Les exigences ne changent pas, mais votre produit evolue. Liez
                  les nouvelles preuves au fur et a mesure.
                </p>
              </div>

              <div className="bg-dark border border-dark-border rounded-xl p-6">
                <div className="flex items-center gap-3 mb-4">
                  <div className="p-2 rounded-lg bg-cyan/10 text-cyan">
                    <BarChart3 className="w-5 h-5" />
                  </div>
                  <h3 className="text-lg font-semibold text-text-light">
                    Snapshots de readiness mensuels
                  </h3>
                </div>
                <p className="text-text-muted-dark text-sm leading-relaxed">
                  Prenez un snapshot de votre score de readiness chaque mois.
                  Cela cree un historique qui demontre votre progression continue
                  vers la conformite lors des audits.
                </p>
              </div>
            </div>
          </ScrollReveal>
        </div>
      </section>

      {/* Architecture overview */}
      <section className="py-20 md:py-28 bg-dark">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <ScrollReveal>
            <SectionTitle
              badge="Architecture"
              title="Comment SRPDesk s'integre dans votre SI"
              subtitle="SRPDesk est concu pour s'adapter a votre infrastructure existante."
              dark
            />
          </ScrollReveal>
          <ScrollReveal delay={200}>
            <div className="mt-10 bg-dark-card border border-dark-border rounded-2xl p-8 md:p-12">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                <div>
                  <div className="text-accent font-semibold mb-3 flex items-center gap-2">
                    <ArrowRight className="w-4 h-4" />
                    Entrees
                  </div>
                  <ul className="space-y-2 text-text-muted-dark text-sm">
                    <li>SBOM (CycloneDX / SPDX)</li>
                    <li>Rapports de scan (OSV, Trivy)</li>
                    <li>Preuves (PDF, JSON, ZIP)</li>
                    <li>Webhooks GitLab/GitHub</li>
                  </ul>
                </div>
                <div className="border-l border-r border-dark-border px-8">
                  <div className="text-cyan font-semibold mb-3 flex items-center gap-2">
                    <ShieldCheck className="w-4 h-4" />
                    SRPDesk
                  </div>
                  <ul className="space-y-2 text-text-muted-dark text-sm">
                    <li>Inventaire produits & releases</li>
                    <li>Checklist CRA Annexe I</li>
                    <li>Score de readiness temps reel</li>
                    <li>War Room & SLA incidents</li>
                    <li>Audit trail immutable (SHA-256)</li>
                    <li>IA assistante (brouillons, questionnaires)</li>
                  </ul>
                </div>
                <div>
                  <div className="text-accent font-semibold mb-3 flex items-center gap-2">
                    <ArrowRight className="w-4 h-4" />
                    Sorties
                  </div>
                  <ul className="space-y-2 text-text-muted-dark text-sm">
                    <li>Soumissions ENISA (SRP)</li>
                    <li>Compliance packs (JSON + PDF)</li>
                    <li>Rapports de readiness</li>
                    <li>Exports audit verifiables</li>
                  </ul>
                </div>
              </div>
            </div>
          </ScrollReveal>
        </div>
      </section>

      <CtaSection />
    </>
  );
}
