import { useState } from 'react';
import { Link } from 'react-router-dom';
import type { DashboardAlert } from '../types';
import { FR } from '../i18n/fr';

const severityConfig: Record<string, { bg: string; border: string; text: string; icon: string }> = {
  CRITICAL: { bg: 'bg-red-50', border: 'border-red-300', text: 'text-red-800', icon: '!!' },
  HIGH: { bg: 'bg-orange-50', border: 'border-orange-300', text: 'text-orange-800', icon: '!' },
  MEDIUM: { bg: 'bg-yellow-50', border: 'border-yellow-300', text: 'text-yellow-800', icon: '~' },
  LOW: { bg: 'bg-blue-50', border: 'border-blue-300', text: 'text-blue-800', icon: 'i' },
};

const alertIcon: Record<string, string> = {
  CRITICAL_VULN: '\u26a0',
  SLA_OVERDUE: '\u23f0',
  SLA_WARNING: '\u23f3',
  EOL_IMMINENT: '\u2622',
  SBOM_OUTDATED: '\u2699',
  CI_FAILING: '\u2717',
  NO_RISK_ASSESSMENT: '\u2691',
  CONFORMITY_BLOCKED: '\u2693',
};

export function AlertBanner({ alerts }: { alerts: DashboardAlert[] }) {
  const [expanded, setExpanded] = useState(false);

  if (!alerts || alerts.length === 0) return null;

  const sorted = [...alerts].sort((a, b) => {
    const order: Record<string, number> = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };
    return (order[a.severity] ?? 9) - (order[b.severity] ?? 9);
  });

  const topSeverity = sorted[0].severity;
  const config = severityConfig[topSeverity] || severityConfig.MEDIUM;
  const critCount = alerts.filter(a => a.severity === 'CRITICAL').length;
  const highCount = alerts.filter(a => a.severity === 'HIGH').length;

  return (
    <div className={`mb-6 rounded-lg border ${config.border} ${config.bg}`}>
      <button
        onClick={() => setExpanded(!expanded)}
        className="w-full p-4 flex items-center justify-between text-left"
      >
        <div className="flex items-center gap-3">
          <div className={`w-3 h-3 rounded-full ${topSeverity === 'CRITICAL' ? 'bg-red-500 animate-pulse' : topSeverity === 'HIGH' ? 'bg-orange-500' : 'bg-yellow-500'}`} />
          <div>
            <p className={`text-sm font-semibold ${config.text}`}>
              {alerts.length} {FR.dashboard.alertsBanner}
            </p>
            <p className="text-xs text-gray-500">
              {critCount > 0 && `${critCount} critiques`}
              {critCount > 0 && highCount > 0 && ' / '}
              {highCount > 0 && `${highCount} hautes`}
            </p>
          </div>
        </div>
        <span className={`text-sm ${config.text} transition-transform ${expanded ? 'rotate-180' : ''}`}>
          &#9660;
        </span>
      </button>

      {expanded && (
        <div className="border-t px-4 pb-3 pt-2 space-y-2">
          {sorted.map((alert, i) => {
            const c = severityConfig[alert.severity] || severityConfig.MEDIUM;
            return (
              <Link
                key={i}
                to={`/products/${alert.productId}`}
                className={`flex items-center gap-3 p-2 rounded-md hover:bg-white/50 transition-colors`}
              >
                <span className="text-base" title={FR.alertType[alert.type] || alert.type}>
                  {alertIcon[alert.type] || '?'}
                </span>
                <div className="flex-1 min-w-0">
                  <p className={`text-sm font-medium ${c.text} truncate`}>
                    {alert.productName}
                  </p>
                  <p className="text-xs text-gray-600 truncate">{alert.message}</p>
                </div>
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${c.bg} ${c.text} border ${c.border}`}>
                  {alert.severity}
                </span>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}
