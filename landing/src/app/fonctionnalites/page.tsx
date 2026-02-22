"use client";

import {
  Package,
  FileCheck,
  List,
  Search,
  Scale,
  Siren,
  FileText,
  Mail,
  ClipboardCheck,
  Link2,
  Download,
} from "lucide-react";
import SectionTitle from "@/components/ui/SectionTitle";
import FeatureCard from "@/components/features/FeatureCard";
import FeatureGrid from "@/components/features/FeatureGrid";
import CtaSection from "@/components/landing/CtaSection";
import ScrollReveal from "@/components/ui/ScrollReveal";

const features = [
  {
    icon: Package,
    title: "Gestion Produits & Releases",
    description:
      "Enregistrez chaque produit avec sa classification CRA (standard, classe I/II, critique) et suivez chaque version avec son tag Git, build ID et statut de cycle de vie.",
    example:
      "Votre produit \"IoT Gateway\" a 3 versions en production (1.0, 1.1, 2.0). Un auditeur demande la liste. Avec SRPDesk : 2 clics. Sans SRPDesk : 3 heures a fouiller Git et Jira.",
  },
  {
    icon: FileCheck,
    title: "Collecte de Preuves (SHA-256)",
    description:
      "Centralisez toutes les preuves par version de produit. Chaque fichier est stocke avec un hash SHA-256 qui garantit qu'il n'a pas ete modifie apres coup. 9 types supportes.",
    example:
      "L'auditeur demande le rapport de pentest de la version 2.0. Avec SRPDesk : clic sur la release → onglet Preuves → fichier avec date et hash d'integrite. Sans SRPDesk : \"Je crois que Jean l'avait mis sur le drive...\"",
  },
  {
    icon: List,
    title: "SBOM (CycloneDX)",
    description:
      "Parsez les fichiers SBOM CycloneDX generes par votre CI/CD. SRPDesk extrait automatiquement chaque composant avec son nom, sa version et son PURL (Package URL).",
    example:
      "\"Est-ce qu'on utilise lodash quelque part ?\" Avec SRPDesk : recherche dans les composants → oui, dans IoT Gateway v1.0 et v1.1. Sans SRPDesk : \"Faut regarder les package.json de chaque projet...\"",
  },
  {
    icon: Search,
    title: "Scan de Vulnerabilites (OSV)",
    description:
      "Interrogation automatique de l'API OSV (base mondiale de vulnerabilites, maintenue par Google) pour chaque composant SBOM. Les vulnerabilites apparaissent comme des Findings avec leur severite.",
    example:
      "Vous lancez un scan sur la release 2.0. SRPDesk detecte 12 vulnerabilites dont 2 critiques. Chaque finding affiche le composant touche, la severite et un lien vers l'advisory officiel.",
  },
  {
    icon: Scale,
    title: "Decisions sur les Findings",
    description:
      "Pour chaque finding, documentez votre decision : Non affecte, Correctif prevu, Attenue ou Corrige. Chaque decision exige une justification ecrite — c'est la trace d'audit.",
    example:
      "L'auditeur : \"Vous avez une CVE critique sur lodash, qu'avez-vous fait ?\" → \"Voici la decision 'Non affecte' prise le 12 fevrier par Marie, avec la justification : nous n'utilisons pas la fonction template() concernee.\"",
  },
  {
    icon: Siren,
    title: "CRA War Room",
    description:
      "Ecran de gestion de crise avec compteurs SLA en temps reel (24h, 72h, 14/30j). Assignation de responsables (Owner, Approver, Viewer), liaison produits/releases/findings/preuves.",
    example:
      "Vendredi 17h : zero-day exploite sur votre firmware. Le compteur 24h demarre. La War Room montre : il reste 23h47 pour l'alerte precoce. Pierre est Owner, Marie est Approver. Tout le monde sait ou on en est.",
  },
  {
    icon: FileText,
    title: "Brouillon SRP (IA)",
    description:
      "L'assistant IA analyse vos donnees existantes et genere un brouillon de soumission SRP : Alerte Precoce (24h), Notification (72h) ou Rapport Final (14/30j). Point de depart pour validation humaine.",
    example:
      "Il est 23h, la deadline approche. Au lieu de partir d'une page blanche, vous cliquez \"Generer un brouillon\". En 30 secondes, un document structure avec le resume, les versions affectees, le statut du patch.",
  },
  {
    icon: Mail,
    title: "Communication Pack (IA)",
    description:
      "Generation automatique de 3 documents de communication crise : advisory securite (formel), email client (actionnable), release notes securite (pour les devs). Ton adapte a chaque audience.",
    example:
      "Votre vuln critique est patchee. Vous cliquez \"Generer Comm Pack\". Vous obtenez advisory + email + release notes. Vous relisez, ajustez le ton, publiez en 15 minutes au lieu de 3 heures.",
  },
  {
    icon: ClipboardCheck,
    title: "Questionnaire Securite (IA)",
    description:
      "Uploadez un questionnaire client (xlsx, docx, txt, csv). L'IA le parse et pre-remplit les reponses avec un niveau de confiance (Haute, Moyenne, Basse, Inconnue). Export JSON/CSV.",
    example:
      "Un prospect envoie un Excel de 80 questions. Au lieu d'y passer 3 jours, vous l'uploadez. L'IA remplit 60 reponses en confiance Haute, 15 en Moyenne, 5 en Inconnue. Vous renvoyez en 2 heures.",
  },
  {
    icon: Link2,
    title: "Piste d'Audit (blockchain-like)",
    description:
      "Chaine de hash SHA-256 append-only. Chaque action est liee a la precedente par son hash. Si un enregistrement est modifie en base, la chaine se casse. Verification a tout moment.",
    example:
      "L'auditeur : \"Comment je sais que ce scan a bien ete fait le 12 fevrier ?\" Vous montrez la piste d'audit : l'evenement est chaine. Modifier la date casserait la chaine. Verification → chaine intacte → confiance totale.",
  },
  {
    icon: Download,
    title: "Export Compliance Pack",
    description:
      "En un clic, generez un ZIP contenant un rapport PDF professionnel (tableaux colores, badges de severite, resume executif) et un fichier JSON structure pour traitement automatise.",
    example:
      "L'auditeur veut un dossier offline. Vous cliquez \"Export\". ZIP telecharge avec rapport PDF en francais, mise en page professionnelle, et donnees JSON. Pret a archiver et partager.",
  },
];

export default function FonctionnalitesPage() {
  return (
    <>
      <section className="py-20 md:py-28 bg-dark">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <ScrollReveal>
            <SectionTitle
              badge="Fonctionnalites"
              title="Tout ce dont vous avez besoin"
              subtitle="11 fonctionnalites qui couvrent l'integralite du cycle de conformite CRA — de l'inventaire des composants a l'export du dossier auditeur."
              dark
            />
          </ScrollReveal>
          <ScrollReveal stagger>
            <FeatureGrid>
              {features.map((feature) => (
                <FeatureCard key={feature.title} {...feature} />
              ))}
            </FeatureGrid>
          </ScrollReveal>
        </div>
      </section>
      <CtaSection />
    </>
  );
}
