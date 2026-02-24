import { useState } from 'react';
import { StatusBadge } from './StatusBadge';
import { FR } from '../i18n/fr';
import type { Finding } from '../types';
import { SEVERITY_COLORS } from '@/constants';

interface FindingsTableProps {
  findings: Finding[];
  onAddDecision: (findingId: string) => void;
}

export function FindingsTable({ findings, onAddDecision }: FindingsTableProps) {
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const t = FR.findingsTable;

  const toggleExpand = (id: string) => {
    setExpandedId(expandedId === id ? null : id);
  };

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('fr-FR');
  };

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[100px]">
              {t.severity}
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              {t.vulnerability}
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[140px]">
              {t.component}
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[120px]">
              {t.status}
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[100px]">
              {t.detected}
            </th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[50px]">
              {t.actions}
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {findings.map((f) => {
            const isExpanded = expandedId === f.id;
            const sev = f.severity || 'UNKNOWN';
            const sevColor = SEVERITY_COLORS[sev] || SEVERITY_COLORS.UNKNOWN;

            return (
              <Fragment key={f.id} f={f} isExpanded={isExpanded} sev={sev} sevColor={sevColor}
                toggleExpand={toggleExpand} onAddDecision={onAddDecision} formatDate={formatDate} />
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

// Separated to keep the main component clean
function Fragment({
  f, isExpanded, sev, sevColor, toggleExpand, onAddDecision, formatDate,
}: {
  f: Finding;
  isExpanded: boolean;
  sev: string;
  sevColor: string;
  toggleExpand: (id: string) => void;
  onAddDecision: (id: string) => void;
  formatDate: (d: string) => string;
}) {
  const t = FR.findingsTable;

  return (
    <>
      {/* Main row */}
      <tr
        className={`hover:bg-gray-50 cursor-pointer transition-colors ${isExpanded ? 'bg-blue-50/50' : ''}`}
        onClick={() => toggleExpand(f.id)}
      >
        <td className="px-4 py-3">
          <div className="flex items-center gap-2">
            <div className={`w-2 h-2 rounded-full ${sevColor} shrink-0`} />
            <StatusBadge status={sev} label={FR.severity[sev] || sev} className="text-xs font-semibold" />
          </div>
        </td>
        <td className="px-4 py-3">
          <div className="text-sm font-medium text-gray-900 leading-snug">
            {f.summary || f.osvId || f.vulnerabilityId.slice(0, 8) + '...'}
          </div>
          {f.osvId && (
            <span className="text-xs font-mono text-gray-400">{f.osvId}</span>
          )}
        </td>
        <td className="px-4 py-3 text-sm text-gray-600 truncate max-w-[140px]" title={f.componentPurl || f.componentName || ''}>
          {f.componentName || f.componentPurl || '-'}
        </td>
        <td className="px-4 py-3">
          <StatusBadge status={f.status} label={FR.findingStatus[f.status] || f.status} />
        </td>
        <td className="px-4 py-3 text-sm text-gray-500">
          {formatDate(f.detectedAt)}
        </td>
        <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
          <button
            className="p-1 text-gray-400 hover:text-primary-600 transition-colors"
            title={isExpanded ? t.showLess : t.showMore}
          >
            <svg className={`w-5 h-5 transition-transform ${isExpanded ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </button>
        </td>
      </tr>

      {/* Expanded detail row */}
      {isExpanded && (
        <tr className="bg-blue-50/30">
          <td colSpan={6} className="px-6 py-4">
            <div className="space-y-4">
              {/* Identifiers */}
              <div className="flex items-center gap-2 flex-wrap">
                {f.osvId && (
                  <a
                    href={`https://osv.dev/vulnerability/${f.osvId}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center px-2.5 py-0.5 rounded text-xs font-mono bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors"
                    onClick={(e) => e.stopPropagation()}
                  >
                    {f.osvId}
                  </a>
                )}
                {f.aliases?.map((alias) => (
                  <a
                    key={alias}
                    href={
                      alias.startsWith('CVE-')
                        ? `https://nvd.nist.gov/vuln/detail/${alias}`
                        : `https://osv.dev/vulnerability/${alias}`
                    }
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center px-2.5 py-0.5 rounded text-xs font-mono bg-blue-50 text-blue-700 hover:bg-blue-100 transition-colors"
                    onClick={(e) => e.stopPropagation()}
                  >
                    {alias}
                  </a>
                ))}
              </div>

              {/* Description */}
              {f.details && (
                <p className="text-sm text-gray-600 leading-relaxed max-w-3xl">
                  {f.details}
                </p>
              )}

              {/* Metadata grid */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-xs">
                <div>
                  <span className="text-gray-500 font-medium">{t.component}</span>
                  <p className="text-gray-700 font-mono truncate" title={f.componentPurl || ''}>
                    {f.componentName || f.componentPurl || '-'}
                  </p>
                </div>
                {f.publishedAt && (
                  <div>
                    <span className="text-gray-500 font-medium">{t.published}</span>
                    <p className="text-gray-700">{formatDate(f.publishedAt)}</p>
                  </div>
                )}
                <div>
                  <span className="text-gray-500 font-medium">{t.detected}</span>
                  <p className="text-gray-700">{formatDate(f.detectedAt)}</p>
                </div>
                <div>
                  <span className="text-gray-500 font-medium">{t.source}</span>
                  <p className="text-gray-700">{f.source}</p>
                </div>
              </div>

              {/* Existing decisions */}
              {f.decisions && f.decisions.length > 0 && (
                <div>
                  <h4 className="text-xs font-semibold text-gray-500 uppercase mb-2">{t.decisions}</h4>
                  <div className="pl-3 border-l-2 border-gray-200 space-y-2">
                    {f.decisions.map((d) => (
                      <div key={d.id} className="text-sm">
                        <div className="flex items-center gap-2">
                          <StatusBadge status={d.decisionType} label={FR.decisionType[d.decisionType] || d.decisionType} />
                          <span className="text-xs text-gray-400">
                            {t.decidedBy} {d.decidedBy} — {formatDate(d.createdAt)}
                          </span>
                        </div>
                        <p className="text-gray-600 mt-0.5">{d.rationale}</p>
                        {d.dueDate && (
                          <p className="text-xs text-gray-400">
                            {t.dueDate} : {formatDate(d.dueDate)}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Action button */}
              <div className="flex justify-end">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onAddDecision(f.id);
                  }}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-primary-600 border border-primary-300 rounded-lg hover:bg-primary-50 transition-colors"
                >
                  <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                  {t.addDecision}
                </button>
              </div>
            </div>
          </td>
        </tr>
      )}
    </>
  );
}
