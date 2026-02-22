import { Clock, AlertTriangle, FileText, Send, CheckCircle } from "lucide-react";

const steps = [
  {
    icon: AlertTriangle,
    time: "Vendredi 17h00",
    title: "Zero-day detecte",
    description:
      "Une vulnerabilite zero-day est exploitee sur votre firmware IoT. L'equipe securite est alertee.",
    color: "text-red",
    borderColor: "border-red/30",
    bgColor: "bg-red/10",
  },
  {
    icon: Clock,
    time: "Vendredi 17h15",
    title: "War Room activee",
    description:
      "Evenement CRA cree dans SRPDesk. Compteur 24h demarre. Pierre est Owner, Marie est Approver. Les releases affectees sont liees.",
    color: "text-amber",
    borderColor: "border-amber/30",
    bgColor: "bg-amber/10",
  },
  {
    icon: FileText,
    time: "Vendredi 18h00",
    title: "Brouillon SRP genere",
    description:
      "L'IA genere le brouillon d'alerte precoce en 30 secondes a partir des donnees existantes. L'equipe relit et ajuste.",
    color: "text-cyan",
    borderColor: "border-cyan/30",
    bgColor: "bg-cyan/10",
  },
  {
    icon: Send,
    time: "Vendredi 20h00",
    title: "Notification soumise",
    description:
      "L'alerte precoce est validee, le bundle ZIP exporte, et soumis a la Single Reporting Platform (ENISA). Communication pack envoye aux clients.",
    color: "text-cyan",
    borderColor: "border-cyan/30",
    bgColor: "bg-cyan/10",
  },
  {
    icon: CheckCircle,
    time: "Samedi matin",
    title: "Situation sous controle",
    description:
      "Le patch est deploye, la notification 72h est en cours de redaction. L'audit trail prouve chaque action. Deadline respectee.",
    color: "text-accent",
    borderColor: "border-accent/30",
    bgColor: "bg-accent/10",
  },
];

export default function UseCaseTimeline() {
  return (
    <div className="max-w-3xl mx-auto">
      <h3 className="text-2xl font-bold text-text-light mb-2 text-center">
        Scenario : Zero-day un vendredi soir
      </h3>
      <p className="text-text-muted-dark text-center mb-10">
        Comment SRPDesk transforme une crise en processus maitrise.
      </p>

      <div className="relative">
        {/* Vertical line */}
        <div className="absolute left-6 top-0 bottom-0 w-0.5 bg-dark-border" />

        <div className="space-y-8">
          {steps.map((step) => (
            <div key={step.title} className="relative flex gap-6">
              {/* Icon */}
              <div
                className={`relative z-10 w-12 h-12 rounded-full ${step.bgColor} border ${step.borderColor} flex items-center justify-center flex-shrink-0`}
              >
                <step.icon className={`w-5 h-5 ${step.color}`} />
              </div>

              {/* Content */}
              <div className="pb-2">
                <span className={`text-xs font-mono font-bold uppercase tracking-wider ${step.color}`}>
                  {step.time}
                </span>
                <h4 className="text-lg font-semibold text-text-light mt-1">
                  {step.title}
                </h4>
                <p className="text-text-muted-dark leading-relaxed mt-1">
                  {step.description}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
