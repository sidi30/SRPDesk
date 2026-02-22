import Link from "next/link";
import { ShieldCheck, Mail } from "lucide-react";

const footerLinks = {
  Produit: [
    { label: "Fonctionnalites", href: "/fonctionnalites" },
    { label: "Cas d'usage", href: "/cas-usage" },
    { label: "Tarifs", href: "/tarifs" },
  ],
  Ressources: [
    { label: "Documentation", href: "#" },
    { label: "API Reference", href: "#" },
    { label: "Changelog", href: "#" },
  ],
  Legal: [
    { label: "Mentions legales", href: "/mentions-legales" },
    { label: "Politique de confidentialite", href: "/mentions-legales" },
    { label: "CGU", href: "/mentions-legales" },
  ],
};

export default function Footer() {
  return (
    <footer className="bg-dark text-text-light" role="contentinfo">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-12">
          {/* Brand */}
          <div>
            <div className="flex items-center gap-2 font-bold text-xl mb-4">
              <ShieldCheck className="w-6 h-6 text-accent" />
              <span>
                SRP<span className="text-accent">Desk</span>
              </span>
            </div>
            <p className="text-text-muted-dark text-sm leading-relaxed mb-4">
              Cockpit de conformite CRA pour les editeurs logiciels et
              fabricants IoT. Centralisez vos preuves, gerez les crises, prouvez
              votre conformite.
            </p>
            <a
              href="mailto:rsidiibrahim@gmail.com"
              className="inline-flex items-center gap-2 text-sm text-text-muted-dark hover:text-accent transition-colors duration-200"
            >
              <Mail className="w-4 h-4" />
              rsidiibrahim@gmail.com
            </a>
          </div>

          {/* Links */}
          {Object.entries(footerLinks).map(([title, links]) => (
            <div key={title}>
              <h3 className="font-semibold text-sm uppercase tracking-wider mb-4 text-text-muted-dark">
                {title}
              </h3>
              <ul className="space-y-3">
                {links.map((link) => (
                  <li key={link.label}>
                    <Link
                      href={link.href}
                      className="text-text-muted-dark hover:text-accent text-sm transition-colors duration-200"
                    >
                      {link.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="border-t border-dark-border mt-12 pt-8 flex flex-col sm:flex-row items-center justify-between gap-4">
          <p className="text-text-muted-dark text-sm">
            &copy; {new Date().getFullYear()} SRPDesk. Tous droits reserves.
          </p>
          <p className="text-text-muted-dark/60 text-xs">
            Conformite EU Cyber Resilience Act (2024/2847)
          </p>
        </div>
      </div>
    </footer>
  );
}
