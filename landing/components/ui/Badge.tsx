import type { ReactNode } from "react";

type BadgeProps = {
  children: ReactNode;
  variant?: "info" | "warning" | "danger" | "success" | "neutral" | "accent";
  className?: string;
};

const variants = {
  info: "bg-cyan/10 text-cyan border border-cyan/20",
  warning: "bg-amber/10 text-amber border border-amber/20",
  danger: "bg-red/10 text-red border border-red/20",
  success: "bg-accent/10 text-accent border border-accent/20",
  neutral: "bg-dark-card text-text-muted-dark border border-dark-border",
  accent: "bg-accent/10 text-accent border border-accent/20",
};

export default function Badge({
  children,
  variant = "info",
  className = "",
}: BadgeProps) {
  return (
    <span
      className={`inline-flex items-center px-3 py-1.5 rounded-full text-sm font-medium ${variants[variant]} ${className}`}
    >
      {children}
    </span>
  );
}
