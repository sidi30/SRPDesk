"use client";

import { AlertTriangle, Clock, Shield } from "lucide-react";
import Button from "@/components/ui/Button";
import AnimatedCounter from "@/components/ui/AnimatedCounter";

export default function HeroSection() {
  return (
    <section className="relative overflow-hidden bg-dark py-24 md:py-36">
      {/* Cyber grid background */}
      <div className="absolute inset-0 cyber-grid" />

      {/* Radial gradient overlay */}
      <div
        className="absolute inset-0"
        style={{
          background:
            "radial-gradient(ellipse at 50% 0%, rgba(34,197,94,0.08) 0%, transparent 60%)",
        }}
      />

      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <div className="animate-fade-in-up">
          <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold text-text-light mb-6 leading-tight tracking-tight">
            Conformite CRA,{" "}
            <span className="text-accent drop-shadow-[0_0_24px_rgba(34,197,94,0.4)]">
              maitrisee.
            </span>
          </h1>

          <p className="text-lg md:text-xl text-text-muted-dark max-w-3xl mx-auto mb-12 leading-relaxed">
            Le Reglement europeen CRA impose a tout editeur logiciel et fabricant IoT
            de prouver la securite de ses produits. SRPDesk est le cockpit qui
            centralise vos preuves, genere vos notifications et prepare vos audits.
          </p>

          {/* Stat cards */}
          <div className="flex flex-col sm:flex-row justify-center gap-4 sm:gap-6 mb-12">
            <div className="flex items-center gap-3 bg-dark-card glow-red rounded-xl px-6 py-4">
              <AlertTriangle className="w-6 h-6 text-red flex-shrink-0" />
              <div className="text-left">
                <div className="text-2xl font-bold text-red">
                  <AnimatedCounter end={15} suffix="M" prefix="" />
                  <span className="text-base font-normal text-text-muted-dark ml-1">EUR</span>
                </div>
                <div className="text-xs text-text-muted-dark">d&apos;amende max</div>
              </div>
            </div>

            <div className="flex items-center gap-3 bg-dark-card rounded-xl px-6 py-4 border border-amber/20 shadow-[0_0_12px_rgba(245,158,11,0.1)]">
              <Clock className="w-6 h-6 text-amber flex-shrink-0" />
              <div className="text-left">
                <div className="text-2xl font-bold text-amber">
                  <AnimatedCounter end={24} suffix="h" />
                </div>
                <div className="text-xs text-text-muted-dark">pour notifier</div>
              </div>
            </div>

            <div className="flex items-center gap-3 bg-dark-card glow-cyan rounded-xl px-6 py-4">
              <Shield className="w-6 h-6 text-cyan flex-shrink-0" />
              <div className="text-left">
                <div className="text-2xl font-bold text-cyan">
                  Sept. <AnimatedCounter end={2027} duration={1500} />
                </div>
                <div className="text-xs text-text-muted-dark">deadline</div>
              </div>
            </div>
          </div>

          {/* CTA */}
          <Button href="#how-it-works" size="lg">
            Decouvrir SRPDesk
          </Button>
        </div>
      </div>
    </section>
  );
}
