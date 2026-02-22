"use client";

import { useState, useEffect, useCallback, useRef } from "react";
import {
  Package,
  Search,
  Siren,
  FileText,
  Download,
  AlertTriangle,
  CheckCircle2,
  Shield,
  Clock,
  User,
  ChevronRight,
  RotateCcw,
  Sparkles,
  Hash,
  FileArchive,
} from "lucide-react";
import SectionTitle from "@/components/ui/SectionTitle";
import ScrollReveal from "@/components/ui/ScrollReveal";

const STEP_COUNT = 5;

const stepMeta = [
  { label: "Produits", icon: Package },
  { label: "Scan", icon: Search },
  { label: "War Room", icon: Siren },
  { label: "IA", icon: Sparkles },
  { label: "Export", icon: Download },
];

/* ------------------------------------------------------------------ */
/*  Shared sub-card style                                              */
/* ------------------------------------------------------------------ */
const cardStyle: React.CSSProperties = {
  backgroundColor: "rgba(30, 41, 59, 0.5)",
  border: "1px solid rgba(148, 163, 184, 0.15)",
  borderRadius: "0.5rem",
};

const cardStyleRed: React.CSSProperties = {
  backgroundColor: "rgba(239, 68, 68, 0.08)",
  border: "1px solid rgba(239, 68, 68, 0.3)",
  borderRadius: "0.5rem",
};

/* ------------------------------------------------------------------ */
/*  Step 1 — Dashboard Produits                                       */
/* ------------------------------------------------------------------ */

const products = [
  {
    name: "SmartGateway Pro",
    version: "v3.2.1",
    category: "Classe I",
    status: "conforme" as const,
  },
  {
    name: "IoT Sensor Hub",
    version: "v2.0.4",
    category: "Classe II",
    status: "critical" as const,
  },
  {
    name: "EdgeController",
    version: "v1.8.0",
    category: "Classe I",
    status: "conforme" as const,
  },
];

function StepProducts({ onNext }: { onNext: () => void }) {
  return (
    <div className="animate-demo-fade-in" style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <h3 style={{ fontSize: "0.75rem", fontWeight: 600, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em" }}>
          Produits numeriques
        </h3>
        <span style={{ fontSize: "0.75rem", color: "#94a3b8" }}>3 produits</span>
      </div>

      <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
        {products.map((p) => (
          <div
            key={p.name}
            style={{
              ...(p.status === "critical" ? cardStyleRed : cardStyle),
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              padding: "0.75rem 1rem",
            }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", minWidth: 0 }}>
              <Package style={{ width: 16, height: 16, color: "#94a3b8", flexShrink: 0 }} />
              <div style={{ minWidth: 0 }}>
                <span style={{ fontSize: "0.875rem", fontWeight: 500, color: "#e2e8f0", display: "block" }}>
                  {p.name}
                </span>
                <span style={{ fontSize: "0.75rem", color: "#94a3b8" }}>
                  {p.version} · {p.category}
                </span>
              </div>
            </div>
            {p.status === "critical" ? (
              <span className="animate-pulse" style={{
                flexShrink: 0,
                padding: "0.125rem 0.5rem",
                fontSize: "10px",
                fontWeight: 700,
                textTransform: "uppercase",
                borderRadius: "0.25rem",
                backgroundColor: "rgba(239, 68, 68, 0.2)",
                color: "#ef4444",
              }}>
                CRITICAL
              </span>
            ) : (
              <span style={{
                flexShrink: 0,
                padding: "0.125rem 0.5rem",
                fontSize: "10px",
                fontWeight: 700,
                textTransform: "uppercase",
                borderRadius: "0.25rem",
                backgroundColor: "rgba(34, 197, 94, 0.2)",
                color: "#22c55e",
              }}>
                Conforme
              </span>
            )}
          </div>
        ))}
      </div>

      <button onClick={onNext} className="demo-btn" style={{ marginTop: "0.5rem" }}>
        <Search style={{ width: 16, height: 16 }} />
        Lancer le scan
        <ChevronRight style={{ width: 16, height: 16 }} />
      </button>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/*  Step 2 — Scan de vulnerabilites                                   */
/* ------------------------------------------------------------------ */

const findings = [
  {
    id: "CVE-2026-41032",
    severity: "CRITICAL" as const,
    title: "Remote Code Execution in libxml2",
    score: 9.8,
  },
  {
    id: "CVE-2026-38891",
    severity: "HIGH" as const,
    title: "Buffer overflow in OpenSSL 3.1",
    score: 7.5,
  },
  {
    id: "CVE-2026-22104",
    severity: "MEDIUM" as const,
    title: "Information disclosure in zlib",
    score: 5.3,
  },
];

const severityStyles: Record<string, React.CSSProperties> = {
  CRITICAL: { backgroundColor: "rgba(239, 68, 68, 0.2)", color: "#ef4444" },
  HIGH: { backgroundColor: "rgba(245, 158, 11, 0.2)", color: "#f59e0b" },
  MEDIUM: { backgroundColor: "rgba(6, 182, 212, 0.2)", color: "#06b6d4" },
};

function StepScan({ onNext }: { onNext: () => void }) {
  const [progress, setProgress] = useState(0);
  const [done, setDone] = useState(false);

  useEffect(() => {
    if (done) return;
    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval);
          setDone(true);
          return 100;
        }
        return prev + 4;
      });
    }, 50);
    return () => clearInterval(interval);
  }, [done]);

  return (
    <div className="animate-demo-fade-in" style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <h3 style={{ fontSize: "0.75rem", fontWeight: 600, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em" }}>
          Scan — IoT Sensor Hub v2.0.4
        </h3>
        <span style={{ fontSize: "0.75rem", color: done ? "#22c55e" : "#94a3b8" }}>
          {done ? "Termine" : "En cours..."}
        </span>
      </div>

      {/* Progress bar */}
      <div style={{ height: 8, borderRadius: 9999, backgroundColor: "rgba(148, 163, 184, 0.15)", overflow: "hidden" }}>
        <div
          style={{
            height: "100%",
            borderRadius: 9999,
            background: "linear-gradient(to right, #22c55e, #06b6d4)",
            transition: "width 100ms",
            width: `${Math.min(progress, 100)}%`,
          }}
        />
      </div>

      {/* Results */}
      <div style={{
        display: "flex",
        flexDirection: "column",
        gap: "0.5rem",
        transition: "opacity 500ms",
        opacity: done ? 1 : 0,
      }}>
        {findings.map((f) => (
          <div
            key={f.id}
            style={{ ...cardStyle, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0.75rem 1rem" }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", minWidth: 0 }}>
              <AlertTriangle style={{ width: 16, height: 16, color: "#94a3b8", flexShrink: 0 }} />
              <div style={{ minWidth: 0 }}>
                <span style={{ fontSize: "0.875rem", fontWeight: 500, color: "#e2e8f0", display: "block" }}>
                  {f.title}
                </span>
                <span style={{ fontSize: "0.75rem", color: "#94a3b8", fontFamily: "monospace" }}>
                  {f.id} · CVSS {f.score}
                </span>
              </div>
            </div>
            <span style={{
              flexShrink: 0,
              padding: "0.125rem 0.5rem",
              fontSize: "10px",
              fontWeight: 700,
              textTransform: "uppercase",
              borderRadius: "0.25rem",
              ...severityStyles[f.severity],
            }}>
              {f.severity}
            </span>
          </div>
        ))}
      </div>

      <button onClick={onNext} disabled={!done} className="demo-btn" style={{ marginTop: "0.5rem" }}>
        <Siren style={{ width: 16, height: 16 }} />
        Ouvrir la War Room
        <ChevronRight style={{ width: 16, height: 16 }} />
      </button>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/*  Step 3 — War Room                                                 */
/* ------------------------------------------------------------------ */

function StepWarRoom({ onNext }: { onNext: () => void }) {
  const [seconds, setSeconds] = useState(23 * 3600 + 47 * 60);

  useEffect(() => {
    const interval = setInterval(() => {
      setSeconds((s) => (s > 0 ? s - 1 : 0));
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;

  return (
    <div className="animate-demo-fade-in" style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <h3 style={{ fontSize: "0.75rem", fontWeight: 600, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em" }}>
          War Room — CVE-2026-41032
        </h3>
        <span style={{
          padding: "0.125rem 0.5rem",
          fontSize: "10px",
          fontWeight: 700,
          textTransform: "uppercase",
          borderRadius: "0.25rem",
          backgroundColor: "rgba(239, 68, 68, 0.2)",
          color: "#ef4444",
        }}>
          CRITICAL
        </span>
      </div>

      {/* SLA Counter */}
      <div style={{
        ...cardStyle,
        padding: "1.25rem",
        textAlign: "center",
        borderColor: "rgba(245, 158, 11, 0.25)",
        backgroundColor: "rgba(245, 158, 11, 0.05)",
      }}>
        <span style={{ fontSize: "0.7rem", color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em", display: "block", marginBottom: "0.25rem" }}>
          SLA Notification ENISA
        </span>
        <div style={{ fontFamily: "monospace", fontSize: "clamp(1.5rem, 4vw, 1.875rem)", color: "#f59e0b", fontWeight: 700, letterSpacing: "0.05em" }}>
          {String(h).padStart(2, "0")}h {String(m).padStart(2, "0")}m{" "}
          {String(s).padStart(2, "0")}s
        </div>
        <span style={{ fontSize: "0.7rem", color: "#94a3b8", display: "block", marginTop: "0.25rem" }}>
          Delai restant avant notification obligatoire (24h)
        </span>
      </div>

      {/* Team + Details */}
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0.75rem" }} className="demo-warroom-grid">
        <div style={{ ...cardStyle, padding: "0.75rem" }}>
          <span style={{ fontSize: "0.7rem", color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em", display: "block", marginBottom: "0.5rem" }}>
            Equipe assignee
          </span>
          <div style={{ display: "flex", flexDirection: "column", gap: "0.375rem" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", fontSize: "0.875rem", color: "#e2e8f0" }}>
              <User style={{ width: 14, height: 14, color: "#22c55e" }} />
              <span>Pierre D. — <span style={{ color: "#94a3b8" }}>Owner</span></span>
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", fontSize: "0.875rem", color: "#e2e8f0" }}>
              <User style={{ width: 14, height: 14, color: "#06b6d4" }} />
              <span>Marie L. — <span style={{ color: "#94a3b8" }}>Approver</span></span>
            </div>
          </div>
        </div>

        <div style={{ ...cardStyle, padding: "0.75rem" }}>
          <span style={{ fontSize: "0.7rem", color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em", display: "block", marginBottom: "0.5rem" }}>
            Contexte
          </span>
          <div style={{ display: "flex", flexDirection: "column", gap: "0.375rem", fontSize: "0.875rem", color: "#e2e8f0" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
              <Package style={{ width: 14, height: 14, color: "#94a3b8" }} />
              IoT Sensor Hub v2.0.4
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
              <AlertTriangle style={{ width: 14, height: 14, color: "#ef4444" }} />
              1 CRITICAL · 1 HIGH · 1 MEDIUM
            </div>
          </div>
        </div>
      </div>

      <button onClick={onNext} className="demo-btn" style={{ marginTop: "0.5rem" }}>
        <Sparkles style={{ width: 16, height: 16 }} />
        Generer le brouillon SRP
        <ChevronRight style={{ width: 16, height: 16 }} />
      </button>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/*  Step 4 — Generation IA                                            */
/* ------------------------------------------------------------------ */

const srpLines = [
  "## Rapport SRP — CVE-2026-41032",
  "",
  "**Produit :** IoT Sensor Hub v2.0.4",
  "**Severite :** CRITICAL (CVSS 9.8)",
  "**Composant :** libxml2 v2.9.14",
  "",
  "### Resume",
  "Execution de code a distance via un buffer overflow dans le parseur XML.",
  "Le correctif est disponible dans libxml2 v2.10.4.",
  "",
  "### Versions affectees",
  "- v2.0.0 a v2.0.4 (inclus)",
  "",
  "### Statut du patch",
  "Correctif integre — Build v2.0.5-rc1 en cours de validation.",
];

function StepAIGeneration({ onNext }: { onNext: () => void }) {
  const [visibleLines, setVisibleLines] = useState(0);
  const done = visibleLines >= srpLines.length;

  useEffect(() => {
    if (done) return;
    const interval = setInterval(() => {
      setVisibleLines((prev) => {
        if (prev >= srpLines.length) {
          clearInterval(interval);
          return srpLines.length;
        }
        return prev + 1;
      });
    }, 180);
    return () => clearInterval(interval);
  }, [done]);

  return (
    <div className="animate-demo-fade-in" style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <h3 style={{ fontSize: "0.75rem", fontWeight: 600, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em" }}>
          Generation IA — Brouillon SRP
        </h3>
        <div style={{ display: "flex", alignItems: "center", gap: "0.375rem" }}>
          <Sparkles style={{ width: 14, height: 14, color: "#22c55e" }} />
          <span style={{ fontSize: "0.75rem", color: "#22c55e", fontWeight: 500 }}>
            {done ? "Haute confiance" : "Generation..."}
          </span>
        </div>
      </div>

      {/* SRP Document */}
      <div style={{
        ...cardStyle,
        padding: "1rem",
        fontFamily: "monospace",
        fontSize: "0.75rem",
        lineHeight: 1.7,
        minHeight: 200,
        overflow: "hidden",
        backgroundColor: "rgba(15, 23, 42, 0.8)",
        borderColor: "rgba(34, 197, 94, 0.15)",
      }}>
        {srpLines.slice(0, visibleLines).map((line, i) => (
          <div key={i} className="animate-demo-typing">
            {line.startsWith("##") ? (
              <span style={{ color: "#22c55e", fontWeight: 700, fontSize: "0.8rem" }}>
                {line.replace(/^#+\s/, "")}
              </span>
            ) : line.startsWith("**") ? (
              <span style={{ color: "#e2e8f0" }}>
                {line.replace(/\*\*(.+?)\*\*/g, "$1")}
              </span>
            ) : line.startsWith("- ") ? (
              <span style={{ color: "#94a3b8" }}>
                {"  "}&#8226; {line.slice(2)}
              </span>
            ) : line === "" ? (
              <br />
            ) : (
              <span style={{ color: "#94a3b8" }}>{line}</span>
            )}
          </div>
        ))}
        {!done && (
          <span className="animate-pulse" style={{ display: "inline-block", width: 8, height: 16, backgroundColor: "#22c55e", marginLeft: 2 }} />
        )}
      </div>

      {done && (
        <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", fontSize: "0.75rem", color: "#22c55e" }}>
          <Shield style={{ width: 14, height: 14 }} />
          Haute confiance — Sources verifiees
        </div>
      )}

      <button onClick={onNext} disabled={!done} className="demo-btn" style={{ marginTop: "0.5rem" }}>
        <Download style={{ width: 16, height: 16 }} />
        Exporter le bundle
        <ChevronRight style={{ width: 16, height: 16 }} />
      </button>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/*  Step 5 — Export & Audit Trail                                     */
/* ------------------------------------------------------------------ */

const bundleFiles = [
  { name: "submission.json", size: "14 KB", icon: FileText },
  { name: "audit_chain.json", size: "8 KB", icon: Hash },
  { name: "rapport_srp.pdf", size: "247 KB", icon: FileArchive },
];

const auditEvents = [
  { action: "product.scan.started", time: "14:32:01", hash: "a3f8c1..d92e" },
  { action: "finding.created", time: "14:32:18", hash: "7b21e4..f103" },
  { action: "warroom.opened", time: "14:33:05", hash: "e8d4a5..b7c2" },
  { action: "srp.draft.generated", time: "14:35:42", hash: "2f9c86..a4e1" },
  { action: "bundle.exported", time: "14:36:10", hash: "c1d7b3..8f95" },
];

function StepExport({ onReset }: { onReset: () => void }) {
  const [downloaded, setDownloaded] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setDownloaded(true), 1200);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="animate-demo-fade-in" style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <h3 style={{ fontSize: "0.75rem", fontWeight: 600, color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em" }}>
          Export Compliance Bundle
        </h3>
        {downloaded && (
          <span style={{
            display: "flex",
            alignItems: "center",
            gap: "0.375rem",
            padding: "0.125rem 0.5rem",
            fontSize: "10px",
            fontWeight: 700,
            textTransform: "uppercase",
            borderRadius: "0.25rem",
            backgroundColor: "rgba(34, 197, 94, 0.2)",
            color: "#22c55e",
          }}>
            <CheckCircle2 style={{ width: 12, height: 12 }} />
            Telecharge
          </span>
        )}
      </div>

      {/* Download animation */}
      {!downloaded && (
        <div style={{
          ...cardStyle,
          padding: "1.5rem",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          gap: "0.75rem",
        }}>
          <Download className="animate-bounce" style={{ width: 32, height: 32, color: "#22c55e" }} />
          <span style={{ fontSize: "0.875rem", color: "#94a3b8" }}>
            Preparation du bundle...
          </span>
          <div style={{ height: 6, width: 192, borderRadius: 9999, backgroundColor: "rgba(148, 163, 184, 0.15)", overflow: "hidden" }}>
            <div className="demo-download-bar" style={{ height: "100%", borderRadius: 9999, backgroundColor: "#22c55e" }} />
          </div>
        </div>
      )}

      {/* Bundle files */}
      {downloaded && (
        <>
          <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
            {bundleFiles.map((f) => (
              <div key={f.name} style={{
                ...cardStyle,
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                padding: "0.625rem 1rem",
              }}>
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                  <f.icon style={{ width: 16, height: 16, color: "#22c55e" }} />
                  <span style={{ fontSize: "0.875rem", fontFamily: "monospace", color: "#e2e8f0" }}>
                    {f.name}
                  </span>
                </div>
                <span style={{ fontSize: "0.75rem", color: "#94a3b8" }}>{f.size}</span>
              </div>
            ))}
          </div>

          {/* Audit Trail */}
          <div style={{
            ...cardStyle,
            padding: "0.75rem",
            backgroundColor: "rgba(15, 23, 42, 0.8)",
            borderColor: "rgba(34, 197, 94, 0.15)",
          }}>
            <span style={{ fontSize: "0.7rem", color: "#94a3b8", textTransform: "uppercase", letterSpacing: "0.05em", display: "block", marginBottom: "0.5rem" }}>
              Piste d&apos;audit (SHA-256)
            </span>
            <div style={{ display: "flex", flexDirection: "column", gap: "0.25rem" }}>
              {auditEvents.map((evt, i) => (
                <div key={i} style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  fontSize: "0.75rem",
                  fontFamily: "monospace",
                  padding: "0.125rem 0",
                }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                    <Clock style={{ width: 12, height: 12, color: "#64748b" }} />
                    <span style={{ color: "#64748b" }}>{evt.time}</span>
                    <span style={{ color: "#e2e8f0" }}>{evt.action}</span>
                  </div>
                  <span style={{ color: "rgba(34, 197, 94, 0.6)" }}>{evt.hash}</span>
                </div>
              ))}
            </div>
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", fontSize: "0.75rem", color: "#22c55e", fontWeight: 600 }}>
            <CheckCircle2 style={{ width: 14, height: 14 }} />
            Chaine integre — Verification OK
          </div>
        </>
      )}

      <button onClick={onReset} className="demo-btn" style={{ marginTop: "0.5rem" }}>
        <RotateCcw style={{ width: 16, height: 16 }} />
        Recommencer la demo
      </button>
    </div>
  );
}

/* ------------------------------------------------------------------ */
/*  Main Component                                                    */
/* ------------------------------------------------------------------ */

export default function InteractiveDemo() {
  const [step, setStep] = useState(0);
  const [transitioning, setTransitioning] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const goTo = useCallback(
    (next: number) => {
      if (transitioning) return;
      setTransitioning(true);
      setTimeout(() => {
        setStep(next);
        setTransitioning(false);
      }, 300);
    },
    [transitioning]
  );

  const next = useCallback(() => goTo(step + 1), [goTo, step]);
  const reset = useCallback(() => goTo(0), [goTo]);

  return (
    <section style={{ paddingTop: "5rem", paddingBottom: "5rem", backgroundColor: "#0b1120", position: "relative", overflow: "hidden" }}>
      {/* Cyber grid on a separate layer — NOT on the section itself (grid-pulse sets opacity to 3%) */}
      <div className="cyber-grid" style={{ position: "absolute", inset: 0 }} />
      <div style={{ position: "relative", maxWidth: "80rem", margin: "0 auto", padding: "0 1rem" }}>
        <ScrollReveal>
          <SectionTitle
            badge="Live Demo"
            title="Testez SRPDesk en 2 minutes"
            subtitle="Vivez un scenario de crise du point de vue du Compliance Manager. Cliquez pour avancer dans le scenario."
            dark
          />
        </ScrollReveal>

        <ScrollReveal>
          <div
            ref={containerRef}
            style={{
              maxWidth: "48rem",
              margin: "0 auto",
              borderRadius: "1rem",
              border: "1px solid rgba(34, 197, 94, 0.25)",
              backgroundColor: "#111827",
              overflow: "hidden",
              boxShadow: "0 0 60px rgba(34, 197, 94, 0.1), 0 0 120px rgba(34, 197, 94, 0.05), 0 25px 50px rgba(0,0,0,0.5)",
            }}
          >
            {/* Window title bar */}
            <div style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              padding: "0.75rem 1rem",
              borderBottom: "1px solid rgba(148, 163, 184, 0.12)",
              backgroundColor: "rgba(30, 41, 59, 0.5)",
            }}>
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                <span style={{ width: 12, height: 12, borderRadius: "50%", backgroundColor: "#ef4444" }} />
                <span style={{ width: 12, height: 12, borderRadius: "50%", backgroundColor: "#f59e0b" }} />
                <span style={{ width: 12, height: 12, borderRadius: "50%", backgroundColor: "#22c55e" }} />
                <span style={{ marginLeft: "0.75rem", fontSize: "0.75rem", color: "#94a3b8", fontFamily: "monospace" }}>
                  SRPDesk — Simulateur de crise
                </span>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: "0.375rem", fontSize: "0.75rem", color: "#64748b" }}>
                <Shield style={{ width: 14, height: 14, color: "rgba(34, 197, 94, 0.5)" }} />
                Mode demo
              </div>
            </div>

            {/* Step indicator */}
            <div style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: "0.5rem",
              padding: "0.75rem 1rem",
              borderBottom: "1px solid rgba(148, 163, 184, 0.08)",
              backgroundColor: "rgba(30, 41, 59, 0.25)",
              flexWrap: "wrap",
            }}>
              {stepMeta.map((s, i) => {
                const Icon = s.icon;
                const isActive = i === step;
                const isPast = i < step;
                return (
                  <button
                    key={i}
                    onClick={() => i < step && goTo(i)}
                    disabled={i > step}
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: "0.375rem",
                      padding: "0.375rem 0.75rem",
                      borderRadius: 9999,
                      fontSize: "0.75rem",
                      fontWeight: 500,
                      border: "none",
                      cursor: i <= step ? "pointer" : "default",
                      transition: "all 200ms",
                      backgroundColor: isActive
                        ? "rgba(34, 197, 94, 0.2)"
                        : isPast
                        ? "rgba(148, 163, 184, 0.12)"
                        : "transparent",
                      color: isActive
                        ? "#22c55e"
                        : isPast
                        ? "#e2e8f0"
                        : "rgba(148, 163, 184, 0.35)",
                    }}
                  >
                    <Icon style={{ width: 14, height: 14 }} />
                    <span className="demo-step-label">{s.label}</span>
                  </button>
                );
              })}
            </div>

            {/* Step content */}
            <div
              style={{
                padding: "1.25rem",
                minHeight: 360,
                transition: "opacity 300ms",
                opacity: transitioning ? 0 : 1,
              }}
            >
              {step === 0 && <StepProducts onNext={next} />}
              {step === 1 && <StepScan onNext={next} />}
              {step === 2 && <StepWarRoom onNext={next} />}
              {step === 3 && <StepAIGeneration onNext={next} />}
              {step === 4 && <StepExport onReset={reset} />}
            </div>

            {/* Footer progress */}
            <div style={{
              padding: "0.625rem 1rem",
              borderTop: "1px solid rgba(148, 163, 184, 0.08)",
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
            }}>
              <span style={{ fontSize: "10px", color: "rgba(148, 163, 184, 0.5)" }}>
                Etape {step + 1} / {STEP_COUNT}
              </span>
              <div style={{ display: "flex", gap: 4 }}>
                {Array.from({ length: STEP_COUNT }).map((_, i) => (
                  <div
                    key={i}
                    style={{
                      height: 4,
                      borderRadius: 9999,
                      transition: "all 500ms",
                      width: i <= step ? 24 : 12,
                      backgroundColor: i <= step ? "#22c55e" : "rgba(148, 163, 184, 0.15)",
                    }}
                  />
                ))}
              </div>
            </div>
          </div>
        </ScrollReveal>
      </div>
    </section>
  );
}
