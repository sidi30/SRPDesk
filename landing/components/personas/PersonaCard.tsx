import type { LucideIcon } from "lucide-react";

type PersonaCardProps = {
  icon: LucideIcon;
  role: string;
  perspective: string;
  quote: string;
};

export default function PersonaCard({
  icon: Icon,
  role,
  perspective,
  quote,
}: PersonaCardProps) {
  return (
    <div className="bg-dark-card rounded-2xl p-8 glow-border transition-all duration-300 hover:-translate-y-0.5">
      <div className="flex items-center gap-3 mb-4">
        <div className="w-12 h-12 rounded-full bg-accent/10 flex items-center justify-center">
          <Icon className="w-6 h-6 text-accent" />
        </div>
        <div>
          <h3 className="font-bold text-text-light">{role}</h3>
          <p className="text-sm text-text-muted-dark">{perspective}</p>
        </div>
      </div>
      <blockquote className="text-text-muted-dark leading-relaxed italic border-l-3 border-accent/30 pl-4">
        &ldquo;{quote}&rdquo;
      </blockquote>
    </div>
  );
}
