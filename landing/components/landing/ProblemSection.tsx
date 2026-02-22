"use client";

import {
  X,
  Check,
  TableProperties,
  FolderSearch,
  Siren,
  ShieldAlert,
  Archive,
  Zap,
  Clock,
  FileCheck,
} from "lucide-react";
import SectionTitle from "@/components/ui/SectionTitle";
import ScrollReveal from "@/components/ui/ScrollReveal";

const without = [
  { icon: TableProperties, text: "Excel, mails et Slack pour gerer la conformite" },
  { icon: FolderSearch, text: "Preuves dispersees, introuvables en cas d'audit" },
  { icon: Siren, text: "Panique en cas d'incident — personne ne sait par ou commencer" },
  { icon: ShieldAlert, text: "Aucune trace d'audit verifiable ni opposable" },
];

const withSrpdesk = [
  { icon: Archive, text: "Preuves centralisees avec hash SHA-256 d'integrite" },
  { icon: Zap, text: "War Room avec SLA en temps reel et brouillons IA" },
  { icon: Clock, text: "Notifications generees et soumises dans les delais" },
  { icon: FileCheck, text: "Piste d'audit inviolable, export en un clic" },
];

export default function ProblemSection() {
  return (
    <section className="py-20 md:py-28 bg-dark">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <ScrollReveal>
          <SectionTitle
            badge="Le probleme"
            title="Sans outil dedie, vous risquez..."
            dark
          />
        </ScrollReveal>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 max-w-5xl mx-auto">
          {/* SANS SRPDesk */}
          <ScrollReveal direction="left">
            <div className="bg-dark-card rounded-2xl p-8 glow-red">
              <div className="flex items-center gap-2 mb-6">
                <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-red/10 text-red border border-red/20 uppercase tracking-wider">
                  Sans SRPDesk
                </span>
              </div>
              <ul className="space-y-5">
                {without.map((item) => (
                  <li key={item.text} className="flex items-start gap-3">
                    <div className="w-8 h-8 rounded-lg bg-red/10 flex items-center justify-center flex-shrink-0 mt-0.5">
                      <X className="w-4 h-4 text-red" />
                    </div>
                    <span className="text-text-muted-dark leading-relaxed">
                      {item.text}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          </ScrollReveal>

          {/* AVEC SRPDesk */}
          <ScrollReveal direction="right">
            <div className="bg-dark-card rounded-2xl p-8 glow-border">
              <div className="flex items-center gap-2 mb-6">
                <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-accent/10 text-accent border border-accent/20 uppercase tracking-wider">
                  Avec SRPDesk
                </span>
              </div>
              <ul className="space-y-5">
                {withSrpdesk.map((item) => (
                  <li key={item.text} className="flex items-start gap-3">
                    <div className="w-8 h-8 rounded-lg bg-accent/10 flex items-center justify-center flex-shrink-0 mt-0.5">
                      <Check className="w-4 h-4 text-accent" />
                    </div>
                    <span className="text-text-muted-dark leading-relaxed">
                      {item.text}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
}
