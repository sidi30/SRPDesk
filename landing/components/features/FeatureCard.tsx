"use client";

import { useState } from "react";
import { ChevronDown, ChevronUp } from "lucide-react";
import type { LucideIcon } from "lucide-react";

type FeatureCardProps = {
  icon: LucideIcon;
  title: string;
  description: string;
  example: string;
};

export default function FeatureCard({
  icon: Icon,
  title,
  description,
  example,
}: FeatureCardProps) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div className="bg-dark-card rounded-2xl p-6 glow-border transition-all duration-300 hover:-translate-y-0.5">
      <div className="flex items-start gap-4 mb-4">
        <div className="w-12 h-12 rounded-xl bg-accent/10 flex items-center justify-center flex-shrink-0">
          <Icon className="w-6 h-6 text-accent" />
        </div>
        <div>
          <h3 className="text-lg font-bold text-text-light">{title}</h3>
        </div>
      </div>
      <p className="text-text-muted-dark leading-relaxed mb-4">{description}</p>

      <button
        onClick={() => setExpanded(!expanded)}
        className="flex items-center gap-1.5 text-sm font-medium text-accent hover:text-accent-glow transition-colors"
        aria-expanded={expanded}
      >
        Exemple concret
        {expanded ? (
          <ChevronUp className="w-4 h-4" />
        ) : (
          <ChevronDown className="w-4 h-4" />
        )}
      </button>

      {expanded && (
        <div className="mt-3 p-4 bg-dark/50 rounded-lg border border-dark-border text-sm text-text-muted-dark leading-relaxed">
          {example}
        </div>
      )}
    </div>
  );
}
