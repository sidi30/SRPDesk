import { useState } from 'react';
import { StatusBadge } from './StatusBadge';
import type { Finding } from '../types';

const SEVERITY_COLORS: Record<string, string> = {
  CRITICAL: 'bg-red-600',
  HIGH: 'bg-orange-500',
  MEDIUM: 'bg-yellow-500',
  LOW: 'bg-blue-500',
  UNKNOWN: 'bg-gray-400',
};

interface FindingCardProps {
  finding: Finding;
  onAddDecision: (findingId: string) => void;
}

export function FindingCard({ finding, onAddDecision }: FindingCardProps) {
  const [expanded, setExpanded] = useState(false);
  const f = finding;

  const severityColor = SEVERITY_COLORS[f.severity || 'UNKNOWN'] || SEVERITY_COLORS.UNKNOWN;

  return (
    <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
      <div className="flex">
        {/* Severity indicator bar */}
        <div className={`w-1.5 shrink-0 ${severityColor}`} />

        <div className="flex-1 p-4">
          {/* Header: severity badge + summary */}
          <div className="flex justify-between items-start gap-3">
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 flex-wrap">
                {f.severity && (
                  <StatusBadge status={f.severity} className="text-xs font-semibold" />
                )}
                <StatusBadge status={f.status} />
              </div>
              <h3 className="mt-1.5 font-medium text-gray-900 text-sm leading-snug">
                {f.summary || f.osvId || f.vulnerabilityId}
              </h3>
            </div>
            <div className="flex items-center gap-2 shrink-0">
              {f.osvUrl && (
                <a
                  href={f.osvUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="px-2 py-1 text-xs text-gray-500 border border-gray-300 rounded hover:bg-gray-50 whitespace-nowrap"
                >
                  View on OSV
                </a>
              )}
              <button
                onClick={() => onAddDecision(f.id)}
                className="px-2.5 py-1 text-xs text-primary-600 border border-primary-300 rounded hover:bg-primary-50 whitespace-nowrap font-medium"
              >
                Add Decision
              </button>
            </div>
          </div>

          {/* Identifiers: GHSA + CVE badges */}
          <div className="mt-2 flex items-center gap-1.5 flex-wrap">
            {f.osvId && (
              <a
                href={`https://osv.dev/vulnerability/${f.osvId}`}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center px-2 py-0.5 rounded text-xs font-mono bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors"
              >
                {f.osvId}
              </a>
            )}
            {f.aliases && f.aliases.map((alias) => (
              <a
                key={alias}
                href={
                  alias.startsWith('CVE-')
                    ? `https://nvd.nist.gov/vuln/detail/${alias}`
                    : `https://osv.dev/vulnerability/${alias}`
                }
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center px-2 py-0.5 rounded text-xs font-mono bg-blue-50 text-blue-700 hover:bg-blue-100 transition-colors"
              >
                {alias}
              </a>
            ))}
          </div>

          {/* Details: expandable description */}
          {f.details && (
            <div className="mt-2">
              <p className={`text-sm text-gray-600 leading-relaxed ${!expanded ? 'line-clamp-3' : ''}`}>
                {f.details}
              </p>
              {f.details.length > 200 && (
                <button
                  onClick={() => setExpanded(!expanded)}
                  className="mt-1 text-xs text-primary-600 hover:text-primary-800 font-medium"
                >
                  {expanded ? 'Show less' : 'Show more'}
                </button>
              )}
            </div>
          )}

          {/* Metadata row */}
          <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-gray-400">
            <span>
              <span className="text-gray-500 font-medium">Component:</span>{' '}
              {f.componentName || f.componentPurl || f.componentId}
            </span>
            {f.publishedAt && (
              <span>
                <span className="text-gray-500 font-medium">Published:</span>{' '}
                {new Date(f.publishedAt).toLocaleDateString()}
              </span>
            )}
            <span>
              <span className="text-gray-500 font-medium">Detected:</span>{' '}
              {new Date(f.detectedAt).toLocaleDateString()}
            </span>
            <span>
              <span className="text-gray-500 font-medium">Source:</span> {f.source}
            </span>
          </div>

          {/* Existing Decisions */}
          {f.decisions && f.decisions.length > 0 && (
            <div className="mt-3 pl-4 border-l-2 border-gray-200 space-y-2">
              {f.decisions.map((d) => (
                <div key={d.id} className="text-sm">
                  <div className="flex items-center gap-2">
                    <StatusBadge status={d.decisionType} />
                    <span className="text-xs text-gray-400">
                      by {d.decidedBy} on {new Date(d.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                  <p className="text-gray-600 mt-0.5">{d.rationale}</p>
                  {d.dueDate && (
                    <p className="text-xs text-gray-400">
                      Due: {new Date(d.dueDate).toLocaleDateString()}
                    </p>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
