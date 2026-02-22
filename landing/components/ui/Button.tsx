import Link from "next/link";
import type { ReactNode } from "react";

type ButtonProps = {
  children: ReactNode;
  href?: string;
  variant?: "primary" | "secondary" | "outline" | "ghost";
  size?: "sm" | "md" | "lg";
  className?: string;
  onClick?: () => void;
};

const variants = {
  primary:
    "bg-accent text-dark font-bold btn-glow hover:bg-accent-glow",
  secondary:
    "bg-dark-card text-text-light border border-dark-border hover:border-accent/30 hover:shadow-[0_0_16px_rgba(34,197,94,0.15)]",
  outline:
    "border-2 border-accent/50 text-accent hover:bg-accent/10 hover:border-accent",
  ghost:
    "text-text-muted-dark hover:text-accent hover:bg-accent/5",
};

const sizes = {
  sm: "px-4 py-2 text-sm",
  md: "px-6 py-3 text-base",
  lg: "px-8 py-4 text-lg",
};

export default function Button({
  children,
  href,
  variant = "primary",
  size = "md",
  className = "",
  onClick,
}: ButtonProps) {
  const classes = `inline-flex items-center justify-center font-semibold rounded-lg transition-all duration-300 ${variants[variant]} ${sizes[size]} ${className}`;

  if (href) {
    return (
      <Link href={href} className={classes}>
        {children}
      </Link>
    );
  }

  return (
    <button onClick={onClick} className={classes}>
      {children}
    </button>
  );
}
