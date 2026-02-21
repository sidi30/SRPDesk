import { useState } from 'react';
import { useProducts } from '../hooks/useProducts';
import { useProductFindings, useAddDecision } from '../hooks/useFindings';
import { FindingsTable } from '../components/FindingsTable';
import { FR } from '../i18n/fr';
import { getErrorMessage } from '../types';
import type { FindingDecisionRequest } from '../types';

const FINDING_STATUSES = ['', 'OPEN', 'NOT_AFFECTED', 'PATCH_PLANNED', 'MITIGATED', 'FIXED'] as const;
const DECISION_TYPES = ['NOT_AFFECTED', 'PATCH_PLANNED', 'MITIGATED', 'FIXED'] as const;

export function FindingsPage() {
  const { data: products, isLoading: productsLoading } = useProducts();
  const [selectedProduct, setSelectedProduct] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');

  // Decision form state
  const [decisionFindingId, setDecisionFindingId] = useState<string | null>(null);
  const [decisionForm, setDecisionForm] = useState<FindingDecisionRequest>({
    decisionType: 'NOT_AFFECTED',
    rationale: '',
  });
  const [error, setError] = useState<string | null>(null);

  const addDecision = useAddDecision();
  const t = FR.findingsPage;
  const tm = FR.decisionModal;

  // Auto-select first product if none selected
  const activeProductId = selectedProduct || products?.[0]?.id || '';

  const { data: findings, isLoading: findingsLoading } = useProductFindings(
    activeProductId,
    statusFilter || undefined
  );

  const handleAddDecision = () => {
    if (!decisionFindingId || !decisionForm.rationale) return;
    setError(null);
    addDecision.mutate(
      { findingId: decisionFindingId, data: decisionForm },
      {
        onSuccess: () => {
          setDecisionFindingId(null);
          setDecisionForm({ decisionType: 'NOT_AFFECTED', rationale: '' });
        },
        onError: (err: unknown) => {
          setError(getErrorMessage(err, 'Failed to add decision'));
        },
      }
    );
  };

  if (productsLoading) return <div className="text-gray-500">{t.loading}</div>;

  const openCount = findings?.filter((f) => f.status === 'OPEN').length || 0;
  const criticalCount = findings?.filter((f) => f.severity === 'CRITICAL' || f.severity === 'HIGH').length || 0;

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">{t.title}</h1>
        <p className="mt-1 text-sm text-gray-500">{t.subtitle}</p>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-6">
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">{t.product}</label>
          <select
            value={activeProductId}
            onChange={(e) => setSelectedProduct(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500 min-w-[200px]"
          >
            {(!products || products.length === 0) && (
              <option value="">{t.noProducts}</option>
            )}
            {products?.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">{t.status}</label>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500 min-w-[160px]"
          >
            {FINDING_STATUSES.map((s) => (
              <option key={s} value={s}>
                {s ? (FR.findingStatus[s] || s) : t.allStatuses}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Summary Stats */}
      {findings && findings.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-white rounded-lg border p-4">
            <p className="text-sm text-gray-500">{t.totalFindings}</p>
            <p className="text-2xl font-bold">{findings.length}</p>
          </div>
          <div className="bg-white rounded-lg border p-4">
            <p className="text-sm text-gray-500">{t.open}</p>
            <p className="text-2xl font-bold text-red-600">{openCount}</p>
          </div>
          <div className="bg-white rounded-lg border p-4">
            <p className="text-sm text-gray-500">{t.criticalHigh}</p>
            <p className="text-2xl font-bold text-orange-600">{criticalCount}</p>
          </div>
        </div>
      )}

      {/* Findings Table */}
      {!activeProductId ? (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-400">
          {t.noProductsAvailable}
        </div>
      ) : findingsLoading ? (
        <div className="text-gray-500 text-sm">{t.loading}</div>
      ) : !findings || findings.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-400">
          {statusFilter ? `${t.noFindingsWithStatus} ${FR.findingStatus[statusFilter] || statusFilter}` : t.noFindings}
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <FindingsTable
            findings={findings}
            onAddDecision={setDecisionFindingId}
          />
        </div>
      )}

      {/* Decision Modal */}
      {decisionFindingId && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <h2 className="text-lg font-semibold mb-4">{tm.title}</h2>

            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
                {error}
              </div>
            )}

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">{tm.type}</label>
                <select
                  value={decisionForm.decisionType}
                  onChange={(e) => setDecisionForm({ ...decisionForm, decisionType: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                >
                  {DECISION_TYPES.map((dt) => (
                    <option key={dt} value={dt}>
                      {FR.decisionType[dt] || dt}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">{tm.rationale}</label>
                <textarea
                  value={decisionForm.rationale}
                  onChange={(e) => setDecisionForm({ ...decisionForm, rationale: e.target.value })}
                  rows={3}
                  placeholder={tm.rationalePlaceholder}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">{tm.dueDate}</label>
                <input
                  type="date"
                  value={decisionForm.dueDate || ''}
                  onChange={(e) => setDecisionForm({ ...decisionForm, dueDate: e.target.value || undefined })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => {
                  setDecisionFindingId(null);
                  setError(null);
                }}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
              >
                {tm.cancel}
              </button>
              <button
                onClick={handleAddDecision}
                disabled={!decisionForm.rationale || addDecision.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {addDecision.isPending ? tm.saving : tm.save}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
