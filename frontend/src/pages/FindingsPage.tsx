import { useState } from 'react';
import { useProducts } from '../hooks/useProducts';
import { useProductFindings, useAddDecision } from '../hooks/useFindings';
import { FindingCard } from '../components/FindingCard';
import type { Finding, FindingDecisionRequest } from '../types';

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
        onError: (err: any) => {
          setError(err.response?.data?.detail || err.message || 'Failed to add decision');
        },
      }
    );
  };

  if (productsLoading) return <div className="text-gray-500">Loading...</div>;

  const openCount = findings?.filter((f) => f.status === 'OPEN').length || 0;
  const criticalCount = findings?.filter((f) => f.severity === 'CRITICAL' || f.severity === 'HIGH').length || 0;

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Findings</h1>
        <p className="mt-1 text-sm text-gray-500">
          Vulnerability findings across your products
        </p>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-6">
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">Product</label>
          <select
            value={activeProductId}
            onChange={(e) => setSelectedProduct(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500 min-w-[200px]"
          >
            {(!products || products.length === 0) && (
              <option value="">No products</option>
            )}
            {products?.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">Status</label>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500 min-w-[160px]"
          >
            {FINDING_STATUSES.map((s) => (
              <option key={s} value={s}>
                {s || 'All Statuses'}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Summary Stats */}
      {findings && findings.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-white rounded-lg border p-4">
            <p className="text-sm text-gray-500">Total Findings</p>
            <p className="text-2xl font-bold">{findings.length}</p>
          </div>
          <div className="bg-white rounded-lg border p-4">
            <p className="text-sm text-gray-500">Open</p>
            <p className="text-2xl font-bold text-red-600">{openCount}</p>
          </div>
          <div className="bg-white rounded-lg border p-4">
            <p className="text-sm text-gray-500">Critical / High</p>
            <p className="text-2xl font-bold text-orange-600">{criticalCount}</p>
          </div>
        </div>
      )}

      {/* Findings List */}
      {!activeProductId ? (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-400">
          No products available. Create a product first.
        </div>
      ) : findingsLoading ? (
        <div className="text-gray-500 text-sm">Loading findings...</div>
      ) : !findings || findings.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-400">
          No findings found{statusFilter ? ` with status ${statusFilter}` : ''} for this product.
        </div>
      ) : (
        <div className="space-y-3">
          {findings.map((f: Finding) => (
            <FindingCard
              key={f.id}
              finding={f}
              onAddDecision={setDecisionFindingId}
            />
          ))}
        </div>
      )}

      {/* Decision Modal */}
      {decisionFindingId && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <h2 className="text-lg font-semibold mb-4">Add Decision</h2>

            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
                {error}
              </div>
            )}

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Decision Type *</label>
                <select
                  value={decisionForm.decisionType}
                  onChange={(e) => setDecisionForm({ ...decisionForm, decisionType: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                >
                  {DECISION_TYPES.map((t) => (
                    <option key={t} value={t}>
                      {t.replace(/_/g, ' ')}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Rationale *</label>
                <textarea
                  value={decisionForm.rationale}
                  onChange={(e) => setDecisionForm({ ...decisionForm, rationale: e.target.value })}
                  rows={3}
                  placeholder="Explain why this decision was made..."
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Due Date (optional)</label>
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
                Cancel
              </button>
              <button
                onClick={handleAddDecision}
                disabled={!decisionForm.rationale || addDecision.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {addDecision.isPending ? 'Saving...' : 'Save Decision'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
