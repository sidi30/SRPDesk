"use client";

import Link from "next/link";
import { useState } from "react";
import { Menu, X, ShieldCheck } from "lucide-react";
import Button from "@/components/ui/Button";

const navigation = [
  { label: "Fonctionnalites", href: "/fonctionnalites" },
  { label: "Cas d'usage", href: "/cas-usage" },
  { label: "Tarifs", href: "/tarifs" },
];

export default function Header() {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <header className="sticky top-0 z-50 glass">
      <nav
        className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8"
        aria-label="Navigation principale"
      >
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link
            href="/"
            className="flex items-center gap-2 text-text-light font-bold text-xl group"
          >
            <ShieldCheck className="w-7 h-7 text-accent group-hover:drop-shadow-[0_0_8px_rgba(34,197,94,0.5)] transition-all" />
            <span>
              SRP<span className="text-accent">Desk</span>
            </span>
          </Link>

          {/* Desktop nav */}
          <div className="hidden md:flex items-center gap-8">
            {navigation.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className="text-text-muted-dark hover:text-accent font-medium transition-colors duration-200"
              >
                {item.label}
              </Link>
            ))}
            <Button href="#contact" size="sm">
              Demander une demo
            </Button>
          </div>

          {/* Mobile menu button */}
          <button
            className="md:hidden p-2 rounded-lg text-text-muted-dark hover:text-accent hover:bg-accent/5 transition-colors"
            onClick={() => setMobileOpen(!mobileOpen)}
            aria-label={mobileOpen ? "Fermer le menu" : "Ouvrir le menu"}
            aria-expanded={mobileOpen}
          >
            {mobileOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>

        {/* Mobile nav */}
        {mobileOpen && (
          <div className="md:hidden pb-4 border-t border-dark-border mt-2 pt-4">
            <div className="flex flex-col gap-3">
              {navigation.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="text-text-muted-dark hover:text-accent font-medium px-2 py-2 rounded-lg hover:bg-accent/5 transition-colors"
                  onClick={() => setMobileOpen(false)}
                >
                  {item.label}
                </Link>
              ))}
              <Button href="#contact" size="sm" className="mt-2">
                Demander une demo
              </Button>
            </div>
          </div>
        )}
      </nav>
    </header>
  );
}
