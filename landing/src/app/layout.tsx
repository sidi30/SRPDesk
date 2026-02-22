import type { Metadata } from "next";
import "./globals.css";
import Header from "@/components/layout/Header";
import Footer from "@/components/layout/Footer";

export const metadata: Metadata = {
  title: "SRPDesk - Conformite CRA maitrisee",
  description:
    "Cockpit de conformite EU Cyber Resilience Act (CRA) pour les editeurs logiciels et fabricants IoT. Centralisez vos preuves, gerez les crises, prouvez votre conformite.",
  keywords: [
    "CRA",
    "Cyber Resilience Act",
    "conformite",
    "SBOM",
    "ENISA",
    "SRP",
    "audit",
    "vulnerabilites",
    "IoT",
    "securite",
    "SRPDesk",
  ],
  openGraph: {
    title: "SRPDesk - Conformite CRA maitrisee",
    description:
      "Le cockpit qui centralise vos preuves, genere vos notifications ENISA et prepare vos audits CRA. Pour editeurs logiciels et fabricants IoT.",
    type: "website",
    locale: "fr_FR",
    siteName: "SRPDesk",
  },
  twitter: {
    card: "summary_large_image",
    title: "SRPDesk - Conformite CRA maitrisee",
    description:
      "Le cockpit qui centralise vos preuves, genere vos notifications ENISA et prepare vos audits CRA.",
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="fr">
      <body className="min-h-screen flex flex-col">
        <a href="#main-content" className="skip-nav">
          Aller au contenu principal
        </a>
        <Header />
        <main id="main-content" className="flex-1">
          {children}
        </main>
        <Footer />
      </body>
    </html>
  );
}
