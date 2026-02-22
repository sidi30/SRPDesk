"use client";

import {
  Building2,
  ShieldCheck,
  ClipboardList,
  Briefcase,
} from "lucide-react";
import SectionTitle from "@/components/ui/SectionTitle";
import PersonaCard from "@/components/personas/PersonaCard";
import UseCaseTimeline from "@/components/personas/UseCaseTimeline";
import CtaSection from "@/components/landing/CtaSection";
import ScrollReveal from "@/components/ui/ScrollReveal";

const personas = [
  {
    icon: Building2,
    role: "CEO / Direction Generale",
    perspective: "Vue risque financier",
    quote:
      "SRPDesk, c'est votre assurance conformite CRA. Au lieu de risquer 15M d'euros d'amende parce qu'un developpeur n'a pas documente une decision, tout est trace, prouvable, et exportable. En cas d'incident, vos equipes savent exactement quoi faire et les deadlines sont respectees.",
  },
  {
    icon: ShieldCheck,
    role: "RSSI / Directeur Securite",
    perspective: "Vue technique",
    quote:
      "SRPDesk centralise votre cycle de gestion des vulnerabilites : SBOM, scan, findings, decisions, preuves, audit trail. Quand un zero-day tombe, la War Room vous donne les compteurs SLA en temps reel, et l'IA genere les brouillons de notification ENISA en 30 secondes. Plus besoin de partir d'une page blanche a 3h du matin.",
  },
  {
    icon: ClipboardList,
    role: "Compliance Manager",
    perspective: "Vue operationnelle",
    quote:
      "SRPDesk automatise 80% de votre travail repetitif. Les questionnaires securite clients sont pre-remplis par l'IA. Les soumissions SRP sont brouillonnees automatiquement. Les rapports de conformite sont generes en un clic avec tous les composants, toutes les vulns, toutes les decisions.",
  },
  {
    icon: Briefcase,
    role: "Commercial / Business Developer",
    perspective: "Vue business",
    quote:
      "Quand un prospect vous envoie un questionnaire securite de 100 questions, au lieu d'y passer une semaine vous l'uploadez dans SRPDesk — l'IA pre-remplit les reponses en 2 minutes — vous ajustez et renvoyez le jour meme. C'est un avantage competitif : vous repondez plus vite que la concurrence.",
  },
];

export default function CasUsagePage() {
  return (
    <>
      {/* Personas */}
      <section className="py-20 md:py-28 bg-dark">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <ScrollReveal>
            <SectionTitle
              badge="Cas d'usage"
              title="Pour chaque profil, une valeur concrete"
              subtitle="SRPDesk s'adresse a toute l'organisation — de la direction generale aux equipes techniques."
              dark
            />
          </ScrollReveal>
          <ScrollReveal stagger>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {personas.map((persona) => (
                <PersonaCard key={persona.role} {...persona} />
              ))}
            </div>
          </ScrollReveal>
        </div>
      </section>

      {/* Timeline scenario */}
      <section className="py-20 md:py-28 bg-dark-card">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <ScrollReveal>
            <SectionTitle
              badge="En situation"
              title="Quand la crise frappe"
              subtitle="Un scenario reel pour comprendre la valeur de SRPDesk quand chaque minute compte."
              dark
            />
          </ScrollReveal>
          <ScrollReveal>
            <UseCaseTimeline />
          </ScrollReveal>
        </div>
      </section>

      <CtaSection />
    </>
  );
}
