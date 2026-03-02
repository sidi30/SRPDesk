import { FR } from '../i18n/fr';

type Level = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

const config: Record<Level, { bg: string; text: string }> = {
  CRITICAL: { bg: 'bg-red-100',    text: 'text-red-800' },
  HIGH:     { bg: 'bg-orange-100', text: 'text-orange-800' },
  MEDIUM:   { bg: 'bg-yellow-100', text: 'text-yellow-800' },
  LOW:      { bg: 'bg-green-100',  text: 'text-green-800' },
};

export function RiskBadge({ level }: { level?: string }) {
  if (!level) {
    return (
      <span className="inline-flex px-2 py-0.5 text-xs font-medium rounded-full bg-gray-100 text-gray-500">
        Risque N/A
      </span>
    );
  }
  const c = config[level as Level];
  if (!c) return null;
  return (
    <span className={`inline-flex px-2 py-0.5 text-xs font-medium rounded-full ${c.bg} ${c.text}`}>
      Risque {FR.riskLevel[level] || level}
    </span>
  );
}
