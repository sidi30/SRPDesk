import type { ReactNode } from "react";

type SectionTitleProps = {
  title: string;
  subtitle?: string;
  badge?: string;
  children?: ReactNode;
  align?: "left" | "center";
  dark?: boolean;
};

export default function SectionTitle({
  title,
  subtitle,
  badge,
  children,
  align = "center",
  dark = false,
}: SectionTitleProps) {
  const alignment = align === "center" ? "text-center mx-auto" : "text-left";

  return (
    <div className={`max-w-3xl mb-12 ${alignment}`}>
      {badge && (
        <span className={`inline-block px-4 py-1.5 rounded-full text-sm font-semibold mb-4 ${
          dark
            ? "bg-accent/10 text-accent border border-accent/20"
            : "bg-dark/5 text-dark border border-dark/10"
        }`}>
          {badge}
        </span>
      )}
      <h2 className={`text-3xl md:text-4xl font-bold mb-4 tracking-tight ${
        dark ? "text-text-light" : "text-dark"
      }`}>
        {title}
      </h2>
      {subtitle && (
        <p className={`text-lg leading-relaxed ${
          dark ? "text-text-muted-dark" : "text-text-muted"
        }`}>
          {subtitle}
        </p>
      )}
      {children}
    </div>
  );
}
