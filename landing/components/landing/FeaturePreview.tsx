"use client";

import Link from "next/link";
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
  ArrowRight,
} from "lucide-react";
import SectionTitle from "@/components/ui/SectionTitle";
import ScrollReveal from "@/components/ui/ScrollReveal";

const features = [
  { icon: Package, title: "Produits & Releases" },
  { icon: FileCheck, title: "Preuves SHA-256" },
  { icon: List, title: "SBOM CycloneDX" },
  { icon: Search, title: "Scan OSV" },
  { icon: Scale, title: "Decisions Findings" },
  { icon: Siren, title: "CRA War Room" },
  { icon: FileText, title: "Brouillon SRP (IA)" },
  { icon: Mail, title: "Comm Pack (IA)" },
  { icon: ClipboardCheck, title: "Questionnaire (IA)" },
  { icon: Link2, title: "Audit Trail" },
  { icon: Download, title: "Export Compliance" },
];

export default function FeaturePreview() {
  return (
    <section className="py-20 md:py-28 bg-light-bg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <ScrollReveal>
          <SectionTitle
            badge="Fonctionnalites"
            title="11 outils pour couvrir tout le cycle CRA"
          />
        </ScrollReveal>

        <ScrollReveal stagger>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {features.map((feature) => (
              <div
                key={feature.title}
                className="bg-white rounded-xl p-5 border border-border hover:shadow-lg hover:-translate-y-0.5 transition-all duration-300 group"
              >
                <div className="w-10 h-10 rounded-lg bg-accent/10 flex items-center justify-center mb-3 group-hover:bg-accent/20 transition-colors">
                  <feature.icon className="w-5 h-5 text-accent" />
                </div>
                <h3 className="text-sm font-semibold text-dark leading-tight">
                  {feature.title}
                </h3>
              </div>
            ))}

            {/* Link card */}
            <Link
              href="/fonctionnalites"
              className="bg-accent/5 rounded-xl p-5 border border-accent/20 hover:bg-accent/10 transition-all duration-300 flex flex-col items-center justify-center gap-2 group"
            >
              <ArrowRight className="w-5 h-5 text-accent group-hover:translate-x-1 transition-transform" />
              <span className="text-sm font-semibold text-accent">
                Voir tout
              </span>
            </Link>
          </div>
        </ScrollReveal>
      </div>
    </section>
  );
}
