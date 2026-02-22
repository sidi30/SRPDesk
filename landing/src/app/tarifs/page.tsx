"use client";

import { Check } from "lucide-react";
import SectionTitle from "@/components/ui/SectionTitle";
import Button from "@/components/ui/Button";
import ScrollReveal from "@/components/ui/ScrollReveal";

const plans = [
  {
    name: "Starter",
    price: "490",
    period: "/mois",
    description: "Pour les PME avec 1 a 5 produits numeriques",
    features: [
      "Jusqu'a 5 produits",
      "Gestion des releases et SBOM",
      "Scan de vulnerabilites (OSV)",
      "Collecte de preuves SHA-256",
      "Export Compliance Pack (PDF + JSON)",
      "Piste d'audit",
      "3 utilisateurs inclus",
      "Support email",
    ],
    cta: "Commencer",
    highlighted: false,
  },
  {
    name: "Pro",
    price: "1 490",
    period: "/mois",
    description: "Pour les ETI avec des obligations CRA etendues",
    features: [
      "Produits illimites",
      "Tout le plan Starter",
      "CRA War Room (gestion de crise)",
      "SLA Timers temps reel",
      "Brouillon SRP par IA",
      "Communication Pack IA",
      "Questionnaire Autopilot IA",
      "10 utilisateurs inclus",
      "Support prioritaire",
    ],
    cta: "Choisir Pro",
    highlighted: true,
  },
  {
    name: "Enterprise",
    price: "Sur devis",
    period: "",
    description: "Pour les grands groupes et les organisations critiques",
    features: [
      "Tout le plan Pro",
      "Deploiement on-premise",
      "SSO / SAML",
      "Utilisateurs illimites",
      "Connecteur SRP ENISA (des disponibilite)",
      "Webhook GitLab / GitHub",
      "SLA personnalises",
      "Account manager dedie",
      "Formation equipes",
    ],
    cta: "Nous contacter",
    highlighted: false,
  },
];

export default function TarifsPage() {
  return (
    <section className="py-20 md:py-28 bg-dark">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <ScrollReveal>
          <SectionTitle
            badge="Tarifs"
            title="Un plan adapte a votre taille"
            subtitle="Tous les plans incluent les mises a jour, la maintenance et l'hebergement securise. Sans engagement."
            dark
          />
        </ScrollReveal>

        <ScrollReveal stagger>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
            {plans.map((plan) => (
              <div
                key={plan.name}
                className={`rounded-2xl p-8 flex flex-col transition-all duration-300 ${
                  plan.highlighted
                    ? "bg-dark-card border-2 border-accent shadow-[0_0_32px_rgba(34,197,94,0.15)] scale-105"
                    : "bg-dark-card border border-dark-border hover:border-dark-border/80"
                }`}
              >
                {plan.highlighted && (
                  <span className="inline-block self-start px-3 py-1 rounded-full text-xs font-semibold bg-accent text-dark mb-4">
                    Populaire
                  </span>
                )}
                <h3 className="text-2xl font-bold mb-2 text-text-light">
                  {plan.name}
                </h3>
                <p className="text-sm mb-6 text-text-muted-dark">
                  {plan.description}
                </p>
                <div className="mb-8">
                  <span className="text-4xl font-bold text-text-light">
                    {plan.price}
                  </span>
                  {plan.period && (
                    <span className="text-lg text-text-muted-dark">
                      {plan.period}
                    </span>
                  )}
                </div>

                <ul className="space-y-3 mb-8 flex-1">
                  {plan.features.map((feature) => (
                    <li key={feature} className="flex items-start gap-2">
                      <Check
                        className={`w-5 h-5 flex-shrink-0 mt-0.5 ${
                          plan.highlighted ? "text-accent" : "text-accent/60"
                        }`}
                      />
                      <span className="text-sm text-text-muted-dark">
                        {feature}
                      </span>
                    </li>
                  ))}
                </ul>

                <Button
                  href="mailto:rsidiibrahim@gmail.com"
                  variant={plan.highlighted ? "primary" : "outline"}
                >
                  {plan.cta}
                </Button>
              </div>
            ))}
          </div>
        </ScrollReveal>

        <p className="text-center text-sm text-text-muted-dark mt-12">
          Tous les prix sont HT. TVA applicable selon votre pays.
          Les tarifs affich&eacute;s sont indicatifs et peuvent &ecirc;tre ajust&eacute;s.
        </p>
      </div>
    </section>
  );
}
