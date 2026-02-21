import { useState } from 'react';
import { useCraEvents } from '../hooks/useCraEvents';
import { useGenerateSrpDraft } from '../hooks/useAi';
import { FR } from '../i18n/fr';
import type { AiJobResponse } from '../types';
import { getErrorMessage } from '../types';

const SUBMISSION_TYPES = ['EARLY_WARNING', 'NOTIFICATION', 'FINAL_REPORT'] as const;
const TYPE_LABELS: Record<string, string> = {
  EARLY_WARNING: FR.ai.earlyWarning,
  NOTIFICATION: FR.ai.notification,
  FINAL_REPORT: FR.ai.finalReport,
};

export function AiSrpDraftPage() {
  const { data: events, isLoading: eventsLoading } = useCraEvents();
  const generateDraft = useGenerateSrpDraft();
  const [selectedEvent, setSelectedEvent] = useState('');
  const [submissionType, setSubmissionType] = useState<string>('EARLY_WARNING');
  const [result, setResult] = useState<AiJobResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const t = FR.ai;

  const handleGenerate = () => {
    if (!selectedEvent) return;
    setError(null);
    setResult(null);
    generateDraft.mutate(
      { craEventId: selectedEvent, submissionType },
      {
        onSuccess: (data) => setResult(data),
        onError: (err: unknown) => setError(getErrorMessage(err, t.error)),
      }
    );
  };

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">{t.srpDraft}</h1>
        <p className="mt-1 text-sm text-gray-500">
          {t.draftWarning}
        </p>
      </div>

      {/* Warning banner */}
      <div className="mb-6 p-3 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-800 flex items-start gap-2">
        <svg className="w-5 h-5 shrink-0 mt-0.5 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        {t.draftWarning}
      </div>

      {/* Controls */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">{t.selectEvent}</label>
            <select
              value={selectedEvent}
              onChange={(e) => setSelectedEvent(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
            >
              <option value="">--</option>
              {events?.map((ev) => (
                <option key={ev.id} value={ev.id}>
                  {ev.title}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">{t.selectType}</label>
            <select
              value={submissionType}
              onChange={(e) => setSubmissionType(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
            >
              {SUBMISSION_TYPES.map((st) => (
                <option key={st} value={st}>{TYPE_LABELS[st]}</option>
              ))}
            </select>
          </div>
          <div className="flex items-end">
            <button
              onClick={handleGenerate}
              disabled={!selectedEvent || generateDraft.isPending}
              className="w-full px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
            >
              {generateDraft.isPending ? t.generating : t.generate}
            </button>
          </div>
        </div>
      </div>

      {/* Error */}
      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
          {error}
        </div>
      )}

      {/* Result */}
      {result && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-semibold text-gray-700">{t.draft}</h3>
              <span className={`text-xs px-2 py-0.5 rounded-full ${
                result.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
              }`}>
                {t.status[result.status] || result.status}
              </span>
            </div>
            <span className="text-xs text-gray-400">{result.model}</span>
          </div>

          {result.artifacts.map((artifact) => (
            <div key={artifact.id} className="mt-3">
              <pre className="bg-gray-50 border border-gray-200 rounded-lg p-4 text-xs overflow-x-auto whitespace-pre-wrap max-h-[500px] overflow-y-auto">
                {JSON.stringify(artifact.contentJson, null, 2)}
              </pre>
            </div>
          ))}

          {result.error && (
            <div className="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
              {result.error}
            </div>
          )}
        </div>
      )}

      {/* Empty state */}
      {!result && !generateDraft.isPending && (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-400">
          {eventsLoading ? 'Chargement...' : events?.length ? 'S\u00e9lectionnez un \u00e9v\u00e9nement et cliquez sur G\u00e9n\u00e9rer' : t.noEvents}
        </div>
      )}
    </div>
  );
}
