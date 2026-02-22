"use client";

import { useState } from "react";
import { ArrowRight, Send, CheckCircle2, Mail, Building2, User, MessageSquare } from "lucide-react";
import Button from "@/components/ui/Button";
import ScrollReveal from "@/components/ui/ScrollReveal";

export default function CtaSection() {
  const [submitted, setSubmitted] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setSubmitting(true);

    const form = e.currentTarget;
    const data = new FormData(form);

    try {
      await fetch("https://formsubmit.co/ajax/rsidiibrahim@gmail.com", {
        method: "POST",
        headers: { Accept: "application/json" },
        body: data,
      });
      setSubmitted(true);
    } catch {
      // Fallback: try regular form submission
      form.submit();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section
      id="contact"
      className="relative py-20 md:py-28 bg-dark overflow-hidden"
    >
      {/* Glow background */}
      <div
        className="absolute inset-0"
        style={{
          background:
            "radial-gradient(ellipse at 30% 50%, rgba(34,197,94,0.06) 0%, transparent 50%), radial-gradient(ellipse at 70% 50%, rgba(6,182,212,0.04) 0%, transparent 50%)",
        }}
      />

      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16 items-start">
          {/* Left: CTA text */}
          <ScrollReveal>
            <div>
              <span className="inline-block px-4 py-1.5 rounded-full text-sm font-semibold mb-6 bg-accent/10 text-accent border border-accent/20">
                Contactez-nous
              </span>
              <h2 className="text-3xl md:text-4xl font-bold text-text-light mb-6 tracking-tight">
                Pret a securiser votre conformite ?
              </h2>
              <p className="text-lg text-text-muted-dark mb-8 leading-relaxed">
                SRPDesk, c&apos;est la difference entre &quot;on gere a vue&quot; et
                &quot;on a un processus auditable&quot;. Entre une amende de 15
                millions et un audit reussi.
              </p>

              <div className="space-y-4 mb-10">
                <div className="flex items-center gap-3 text-text-muted-dark">
                  <CheckCircle2 className="w-5 h-5 text-accent flex-shrink-0" />
                  <span>Demo personnalisee de 30 minutes</span>
                </div>
                <div className="flex items-center gap-3 text-text-muted-dark">
                  <CheckCircle2 className="w-5 h-5 text-accent flex-shrink-0" />
                  <span>Analyse gratuite de votre exposition CRA</span>
                </div>
                <div className="flex items-center gap-3 text-text-muted-dark">
                  <CheckCircle2 className="w-5 h-5 text-accent flex-shrink-0" />
                  <span>Mise en place accompagnee</span>
                </div>
              </div>

              <Button href="/fonctionnalites" variant="outline" size="lg">
                Explorer les fonctionnalites
                <ArrowRight className="w-5 h-5 ml-2" />
              </Button>
            </div>
          </ScrollReveal>

          {/* Right: Contact form */}
          <ScrollReveal delay={150}>
            <div
              style={{
                backgroundColor: "#111827",
                border: "1px solid rgba(34, 197, 94, 0.2)",
                borderRadius: "1rem",
                padding: "2rem",
                boxShadow: "0 0 40px rgba(34, 197, 94, 0.06), 0 25px 50px rgba(0,0,0,0.3)",
              }}
            >
              {submitted ? (
                <div style={{ textAlign: "center", padding: "2rem 0" }}>
                  <div style={{
                    width: 64,
                    height: 64,
                    borderRadius: "50%",
                    backgroundColor: "rgba(34, 197, 94, 0.15)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    margin: "0 auto 1.5rem",
                  }}>
                    <CheckCircle2 style={{ width: 32, height: 32, color: "#22c55e" }} />
                  </div>
                  <h3 style={{ fontSize: "1.25rem", fontWeight: 700, color: "#e2e8f0", marginBottom: "0.5rem" }}>
                    Message envoye !
                  </h3>
                  <p style={{ fontSize: "0.875rem", color: "#94a3b8", lineHeight: 1.6 }}>
                    Merci pour votre interet. Nous reviendrons vers vous sous 24 heures.
                  </p>
                </div>
              ) : (
                <form
                  onSubmit={handleSubmit}
                  action="https://formsubmit.co/rsidiibrahim@gmail.com"
                  method="POST"
                >
                  {/* Formsubmit config */}
                  <input type="hidden" name="_subject" value="Nouveau contact SRPDesk" />
                  <input type="hidden" name="_template" value="table" />
                  <input type="hidden" name="_captcha" value="false" />
                  <input type="text" name="_honey" style={{ display: "none" }} tabIndex={-1} />

                  <h3 style={{ fontSize: "1.125rem", fontWeight: 700, color: "#e2e8f0", marginBottom: "1.5rem" }}>
                    Demander une demo
                  </h3>

                  <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
                    {/* Nom */}
                    <div>
                      <label htmlFor="contact-name" style={{ display: "flex", alignItems: "center", gap: "0.375rem", fontSize: "0.75rem", fontWeight: 600, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: "0.375rem" }}>
                        <User style={{ width: 12, height: 12 }} />
                        Nom complet
                      </label>
                      <input
                        id="contact-name"
                        type="text"
                        name="name"
                        required
                        placeholder="Jean Dupont"
                        className="demo-input"
                      />
                    </div>

                    {/* Email */}
                    <div>
                      <label htmlFor="contact-email" style={{ display: "flex", alignItems: "center", gap: "0.375rem", fontSize: "0.75rem", fontWeight: 600, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: "0.375rem" }}>
                        <Mail style={{ width: 12, height: 12 }} />
                        Email professionnel
                      </label>
                      <input
                        id="contact-email"
                        type="email"
                        name="email"
                        required
                        placeholder="jean@entreprise.com"
                        className="demo-input"
                      />
                    </div>

                    {/* Entreprise */}
                    <div>
                      <label htmlFor="contact-company" style={{ display: "flex", alignItems: "center", gap: "0.375rem", fontSize: "0.75rem", fontWeight: 600, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: "0.375rem" }}>
                        <Building2 style={{ width: 12, height: 12 }} />
                        Entreprise
                      </label>
                      <input
                        id="contact-company"
                        type="text"
                        name="company"
                        placeholder="Nom de votre entreprise"
                        className="demo-input"
                      />
                    </div>

                    {/* Message */}
                    <div>
                      <label htmlFor="contact-message" style={{ display: "flex", alignItems: "center", gap: "0.375rem", fontSize: "0.75rem", fontWeight: 600, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: "0.375rem" }}>
                        <MessageSquare style={{ width: 12, height: 12 }} />
                        Message
                      </label>
                      <textarea
                        id="contact-message"
                        name="message"
                        rows={3}
                        required
                        placeholder="Decrivez votre besoin ou posez vos questions..."
                        className="demo-input"
                        style={{ resize: "vertical", minHeight: 80 }}
                      />
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={submitting}
                    className="demo-btn"
                    style={{ marginTop: "1.5rem" }}
                  >
                    {submitting ? (
                      <>Envoi en cours...</>
                    ) : (
                      <>
                        <Send style={{ width: 16, height: 16 }} />
                        Envoyer le message
                      </>
                    )}
                  </button>

                  <p style={{ fontSize: "0.7rem", color: "#64748b", marginTop: "0.75rem", textAlign: "center" }}>
                    Nous repondons sous 24h. Pas de spam, promis.
                  </p>
                </form>
              )}
            </div>
          </ScrollReveal>
        </div>
      </div>
    </section>
  );
}
