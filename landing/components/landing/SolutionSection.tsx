"use client";

import { Archive, Siren, Fingerprint, ArrowRight } from "lucide-react";
import SectionTitle from "@/components/ui/SectionTitle";
import ScrollReveal from "@/components/ui/ScrollReveal";

const steps = [
  {
    number: "01",
    icon: Archive,
    title: "Centralisez vos preuves",
    description:
      "SBOM, rapports de scan, decisions de correction — chaque version de chaque produit a son dossier avec hash SHA-256 d'integrite.",
  },
  {
    number: "02",
    icon: Siren,
    title: "Gerez la crise",
    description:
      "War Room avec compteurs SLA en temps reel. L'IA genere les brouillons de notification ENISA et les communications client.",
  },
  {
    number: "03",
    icon: Fingerprint,
    title: "Prouvez votre bonne foi",
    description:
      "Piste d'audit blockchain-like : chaque action est chainee par hash SHA-256. L'auditeur verifie mathematiquement l'authenticite.",
  },
];

export default function SolutionSection() {
  return (
    <section id="how-it-works" className="py-20 md:py-28 bg-light-bg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <ScrollReveal>
          <SectionTitle
            badge="Comment ca marche"
            title="3 etapes pour une conformite prouvable"
            subtitle="Un processus simple, guide, et entierement tracable."
          />
        </ScrollReveal>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 relative">
          {/* Connection arrows (desktop only) */}
          <div className="hidden lg:block absolute top-1/2 left-[33%] -translate-y-1/2 -translate-x-1/2 z-10">
            <ArrowRight className="w-8 h-8 text-accent/30" />
          </div>
          <div className="hidden lg:block absolute top-1/2 left-[67%] -translate-y-1/2 -translate-x-1/2 z-10">
            <ArrowRight className="w-8 h-8 text-accent/30" />
          </div>

          {steps.map((step, i) => (
            <ScrollReveal key={step.number} delay={i * 150}>
              <div className="bg-white rounded-2xl p-8 border border-border hover:shadow-xl transition-all duration-300 hover:-translate-y-1 relative">
                {/* Step number */}
                <span className="absolute -top-4 left-6 inline-flex items-center justify-center w-8 h-8 rounded-full bg-accent text-dark text-sm font-bold">
                  {step.number}
                </span>

                <div className="w-14 h-14 rounded-xl bg-accent/10 flex items-center justify-center mb-6 mt-2">
                  <step.icon className="w-7 h-7 text-accent" />
                </div>

                <h3 className="text-xl font-bold text-dark mb-3">
                  {step.title}
                </h3>
                <p className="text-text-muted leading-relaxed">
                  {step.description}
                </p>
              </div>
            </ScrollReveal>
          ))}
        </div>
      </div>
    </section>
  );
}
