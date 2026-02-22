"use client";

import { useEffect, useRef, useState } from "react";
import { AlertTriangle, Clock, FileText, Send, CheckCircle } from "lucide-react";
import SectionTitle from "@/components/ui/SectionTitle";
import ScrollReveal from "@/components/ui/ScrollReveal";

const steps = [
  {
    icon: AlertTriangle,
    time: "17h00",
    title: "Zero-day detecte",
    description: "Vulnerabilite exploitee sur le firmware IoT",
    color: "text-red",
  },
  {
    icon: Clock,
    time: "17h15",
    title: "War Room activee",
    description: "Compteur 24h demarre, equipe assignee",
    color: "text-amber",
  },
  {
    icon: FileText,
    time: "18h00",
    title: "Brouillon SRP",
    description: "L'IA genere l'alerte precoce en 30s",
    color: "text-cyan",
  },
  {
    icon: Send,
    time: "20h00",
    title: "Notification soumise",
    description: "Bundle exporte, soumis a ENISA",
    color: "text-cyan",
  },
  {
    icon: CheckCircle,
    time: "Samedi",
    title: "Sous controle",
    description: "Patch deploye, audit trail prouve tout",
    color: "text-accent",
  },
];

export default function CrisisTimeline() {
  const [activeIndex, setActiveIndex] = useState(-1);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          // Animate dots sequentially
          steps.forEach((_, i) => {
            setTimeout(() => setActiveIndex(i), 400 * (i + 1));
          });
          observer.unobserve(el);
        }
      },
      { threshold: 0.3 }
    );

    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  return (
    <section className="py-20 md:py-28 bg-dark overflow-hidden">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <ScrollReveal>
          <SectionTitle
            badge="En situation"
            title="Vendredi soir, zero-day."
            subtitle="Comment SRPDesk transforme une crise en processus maitrise."
            dark
          />
        </ScrollReveal>

        <div ref={ref} className="relative max-w-5xl mx-auto">
          {/* Horizontal line (desktop) / Vertical line (mobile) */}
          <div className="hidden md:block absolute top-8 left-[5%] right-[5%] h-0.5 timeline-line" />
          <div className="md:hidden absolute left-6 top-0 bottom-0 w-0.5 timeline-line" />

          {/* Desktop horizontal layout */}
          <div className="hidden md:grid grid-cols-5 gap-4">
            {steps.map((step, i) => (
              <div key={step.title} className="flex flex-col items-center text-center">
                {/* Dot */}
                <div
                  className={`timeline-dot w-16 h-16 rounded-full bg-dark-card border-2 flex items-center justify-center mb-4 transition-all duration-500 ${
                    i <= activeIndex
                      ? "border-accent shadow-[0_0_16px_rgba(34,197,94,0.4)]"
                      : "border-dark-border"
                  }`}
                >
                  <step.icon
                    className={`w-6 h-6 transition-colors duration-500 ${
                      i <= activeIndex ? step.color : "text-text-muted-dark/40"
                    }`}
                  />
                </div>

                {/* Content */}
                <span
                  className={`text-xs font-mono font-bold uppercase tracking-wider mb-1 transition-colors duration-500 ${
                    i <= activeIndex ? step.color : "text-text-muted-dark/40"
                  }`}
                >
                  {step.time}
                </span>
                <h4
                  className={`text-sm font-semibold mb-1 transition-colors duration-500 ${
                    i <= activeIndex ? "text-text-light" : "text-text-muted-dark/40"
                  }`}
                >
                  {step.title}
                </h4>
                <p
                  className={`text-xs leading-relaxed transition-colors duration-500 ${
                    i <= activeIndex ? "text-text-muted-dark" : "text-text-muted-dark/30"
                  }`}
                >
                  {step.description}
                </p>
              </div>
            ))}
          </div>

          {/* Mobile vertical layout */}
          <div className="md:hidden space-y-8">
            {steps.map((step, i) => (
              <div key={step.title} className="relative flex gap-6">
                <div
                  className={`relative z-10 w-12 h-12 rounded-full bg-dark-card border-2 flex items-center justify-center flex-shrink-0 transition-all duration-500 ${
                    i <= activeIndex
                      ? "border-accent shadow-[0_0_12px_rgba(34,197,94,0.4)]"
                      : "border-dark-border"
                  }`}
                >
                  <step.icon
                    className={`w-5 h-5 transition-colors duration-500 ${
                      i <= activeIndex ? step.color : "text-text-muted-dark/40"
                    }`}
                  />
                </div>
                <div className="pb-2">
                  <span
                    className={`text-xs font-mono font-bold uppercase tracking-wider ${
                      i <= activeIndex ? step.color : "text-text-muted-dark/40"
                    }`}
                  >
                    {step.time}
                  </span>
                  <h4
                    className={`text-lg font-semibold mt-1 ${
                      i <= activeIndex ? "text-text-light" : "text-text-muted-dark/40"
                    }`}
                  >
                    {step.title}
                  </h4>
                  <p
                    className={`text-sm leading-relaxed mt-1 ${
                      i <= activeIndex ? "text-text-muted-dark" : "text-text-muted-dark/30"
                    }`}
                  >
                    {step.description}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
