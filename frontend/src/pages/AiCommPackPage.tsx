import { useState } from 'react';
import { useCraEvents } from '../hooks/useCraEvents';
import { useGenerateCommPack } from '../hooks/useAi';
import { FR } from '../i18n/fr';
import type { AiJobResponse } from '../types';
import { getErrorMessage } from '../types';

export function AiCommPackPage() {
  const { data: events, isLoading: eventsLoading } = useCraEvents();
  const generateCommPack = useGenerateCommPack();
  const [selectedEvent, setSelectedEvent] = useState('');
  const [result, setResult] = useState<AiJobResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState<string | null>(null);
  const t = FR.ai;

  const handleGenerate = () => {
    if (!selectedEvent) return;
    setError(null);
    setResult(null);
    generateCommPack.mutate(selectedEvent, {
      onSuccess: (data) => setResult(data),
      onError: (err: unknown) => setError(getErrorMessage(err, t.error)),
    });
  };

  const handleCopy = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    setCopied(label);
    setTimeout(() => setCopied(null), 2000);
  };

  const content = result?.artifacts?.[0]?.contentJson as Record<string, unknown> | undefined;

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">{t.commPack}</h1>
        <p className="mt-1 text-sm text-gray-500">{t.draftWarning}</p>
      </div>

      {/* Warning */}
      <div className="mb-6 p-3 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-800 flex items-start gap-2">
        <svg className="w-5 h-5 shrink-0 mt-0.5 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        {t.draftWarning}
      </div>

      {/* Controls */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="flex gap-4 items-end">
          <div className="flex-1">
            <label className="block text-xs font-medium text-gray-500 mb-1">{t.selectEvent}</label>
            <select
              value={selectedEvent}
              onChange={(e) => setSelectedEvent(e.target.value)}
              aria-label="Sélectionner un événement CRA"
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
            >
              <option value="">--</option>
              {events?.map((ev) => (
                <option key={ev.id} value={ev.id}>{ev.title}</option>
              ))}
            </select>
          </div>
          <button
            onClick={handleGenerate}
            disabled={!selectedEvent || generateCommPack.isPending}
            aria-label="Générer le pack de communication"
            className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
          >
            {generateCommPack.isPending ? t.generating : t.generate}
          </button>
        </div>
      </div>

      {error && (
        <div role="alert" className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">{error}</div>
      )}

      {/* Results — 3 tabs */}
      {content && (
        <div className="space-y-4">
          {/* Advisory */}
          <Section
            title={t.advisory}
            content={String(content.advisory_markdown || '')}
            onCopy={() => handleCopy(String(content.advisory_markdown || ''), 'advisory')}
            isCopied={copied === 'advisory'}
          />
          {/* Email */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex justify-between items-center mb-3">
              <h3 className="text-sm font-semibold text-gray-700">{t.email}</h3>
              <button
                onClick={() => handleCopy(
                  `Objet: ${content.email_subject}\n\n${content.email_body}`, 'email'
                )}
                className="text-xs text-primary-600 hover:text-primary-800 font-medium"
              >
                {copied === 'email' ? t.copied : t.copy}
              </button>
            </div>
            <div className="mb-2">
              <span className="text-xs font-medium text-gray-500">Objet : </span>
              <span className="text-sm text-gray-900">{String(content.email_subject || '')}</span>
            </div>
            <pre className="bg-gray-50 border border-gray-200 rounded-lg p-4 text-sm whitespace-pre-wrap text-gray-700">
              {String(content.email_body || '')}
            </pre>
          </div>
          {/* Release notes */}
          <Section
            title={t.releaseNotes}
            content={String(content.release_notes_markdown || '')}
            onCopy={() => handleCopy(String(content.release_notes_markdown || ''), 'notes')}
            isCopied={copied === 'notes'}
          />
        </div>
      )}

      {!result && !generateCommPack.isPending && (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-400">
          {eventsLoading ? 'Chargement...' : events?.length ? 'S\u00e9lectionnez un \u00e9v\u00e9nement et cliquez sur G\u00e9n\u00e9rer' : t.noEvents}
        </div>
      )}
    </div>
  );
}

function Section({ title, content, onCopy, isCopied }: {
  title: string; content: string; onCopy: () => void; isCopied: boolean;
}) {
  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-3">
        <h3 className="text-sm font-semibold text-gray-700">{title}</h3>
        <button onClick={onCopy} className="text-xs text-primary-600 hover:text-primary-800 font-medium">
          {isCopied ? FR.ai.copied : FR.ai.copy}
        </button>
      </div>
      <pre className="bg-gray-50 border border-gray-200 rounded-lg p-4 text-sm whitespace-pre-wrap text-gray-700 max-h-[400px] overflow-y-auto">
        {content}
      </pre>
    </div>
  );
}
