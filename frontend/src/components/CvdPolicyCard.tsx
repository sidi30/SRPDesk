import { useState } from 'react';
import { useCvdPolicy, useSaveCvdPolicy, usePublishCvdPolicy } from '../hooks/useCvdPolicy';
import { StatusBadge } from './StatusBadge';
import type { CvdPolicyRequest } from '../types';
import { getErrorMessage } from '../types';

interface Props {
  productId: string;
}

export function CvdPolicyCard({ productId }: Props) {
  const { data: policy, isLoading, error: loadError } = useCvdPolicy(productId);
  const saveMutation = useSaveCvdPolicy();
  const publishMutation = usePublishCvdPolicy();

  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState<CvdPolicyRequest>({
    contactEmail: '',
    disclosureTimelineDays: 90,
    acceptsAnonymous: true,
    acceptedLanguages: 'en,fr',
  });
  const [error, setError] = useState<string | null>(null);

  const startEditing = () => {
    if (policy) {
      setForm({
        contactEmail: policy.contactEmail,
        contactUrl: policy.contactUrl,
        pgpKeyUrl: policy.pgpKeyUrl,
        policyUrl: policy.policyUrl,
        disclosureTimelineDays: policy.disclosureTimelineDays,
        acceptsAnonymous: policy.acceptsAnonymous,
        bugBountyUrl: policy.bugBountyUrl,
        acceptedLanguages: policy.acceptedLanguages,
        scopeDescription: policy.scopeDescription,
      });
    }
    setEditing(true);
  };

  const handleSave = () => {
    setError(null);
    saveMutation.mutate(
      { productId, data: form },
      {
        onSuccess: () => setEditing(false),
        onError: (err: unknown) => setError(getErrorMessage(err)),
      }
    );
  };

  const noPolicyYet = !isLoading && (loadError || !policy);

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-sm font-semibold text-gray-700">
          CVD Policy (CRA Annexe I §2(5))
        </h3>
        <div className="flex gap-2">
          {policy && <StatusBadge status={policy.status} />}
          {!editing && (
            <button
              onClick={startEditing}
              className="px-3 py-1 text-xs border border-gray-300 rounded hover:bg-gray-50"
            >
              {noPolicyYet ? 'Create Policy' : 'Edit'}
            </button>
          )}
          {policy && policy.status === 'DRAFT' && !editing && (
            <button
              onClick={() => publishMutation.mutate(productId)}
              disabled={publishMutation.isPending}
              className="px-3 py-1 text-xs text-green-600 border border-green-300 rounded hover:bg-green-50"
            >
              Publish
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="mb-3 p-2 bg-red-50 border border-red-200 rounded text-xs text-red-700">{error}</div>
      )}

      {isLoading && <p className="text-sm text-gray-400">Loading...</p>}

      {noPolicyYet && !editing && (
        <p className="text-sm text-gray-400">
          No CVD policy configured. CRA Annexe I §2(5) requires a coordinated vulnerability disclosure policy.
        </p>
      )}

      {policy && !editing && (
        <div className="space-y-3 text-sm">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <span className="text-xs text-gray-400 block">Security Contact</span>
              <span className="text-gray-700">{policy.contactEmail}</span>
            </div>
            <div>
              <span className="text-xs text-gray-400 block">Disclosure Timeline</span>
              <span className="text-gray-700">{policy.disclosureTimelineDays} days</span>
            </div>
            {policy.policyUrl && (
              <div>
                <span className="text-xs text-gray-400 block">Policy URL</span>
                <a href={policy.policyUrl} target="_blank" rel="noopener noreferrer"
                  className="text-primary-600 hover:underline truncate block">{policy.policyUrl}</a>
              </div>
            )}
            {policy.bugBountyUrl && (
              <div>
                <span className="text-xs text-gray-400 block">Bug Bounty</span>
                <a href={policy.bugBountyUrl} target="_blank" rel="noopener noreferrer"
                  className="text-primary-600 hover:underline truncate block">{policy.bugBountyUrl}</a>
              </div>
            )}
            <div>
              <span className="text-xs text-gray-400 block">Anonymous Reports</span>
              <span className="text-gray-700">{policy.acceptsAnonymous ? 'Accepted' : 'Not accepted'}</span>
            </div>
            <div>
              <span className="text-xs text-gray-400 block">Languages</span>
              <span className="text-gray-700">{policy.acceptedLanguages || 'en'}</span>
            </div>
          </div>
          {policy.scopeDescription && (
            <div>
              <span className="text-xs text-gray-400 block">Scope</span>
              <p className="text-gray-600 text-xs mt-1">{policy.scopeDescription}</p>
            </div>
          )}
        </div>
      )}

      {editing && (
        <div className="space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Security Contact Email *</label>
              <input type="email" value={form.contactEmail}
                onChange={(e) => setForm({ ...form, contactEmail: e.target.value })}
                className="w-full border border-gray-300 rounded px-2 py-1.5 text-sm" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Disclosure Timeline (days)</label>
              <input type="number" min={7} value={form.disclosureTimelineDays ?? 90}
                onChange={(e) => setForm({ ...form, disclosureTimelineDays: parseInt(e.target.value) })}
                className="w-full border border-gray-300 rounded px-2 py-1.5 text-sm" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Policy URL</label>
              <input type="url" value={form.policyUrl ?? ''}
                onChange={(e) => setForm({ ...form, policyUrl: e.target.value })}
                className="w-full border border-gray-300 rounded px-2 py-1.5 text-sm" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Contact URL</label>
              <input type="url" value={form.contactUrl ?? ''}
                onChange={(e) => setForm({ ...form, contactUrl: e.target.value })}
                className="w-full border border-gray-300 rounded px-2 py-1.5 text-sm" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">PGP Key URL</label>
              <input type="url" value={form.pgpKeyUrl ?? ''}
                onChange={(e) => setForm({ ...form, pgpKeyUrl: e.target.value })}
                className="w-full border border-gray-300 rounded px-2 py-1.5 text-sm" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Bug Bounty URL</label>
              <input type="url" value={form.bugBountyUrl ?? ''}
                onChange={(e) => setForm({ ...form, bugBountyUrl: e.target.value })}
                className="w-full border border-gray-300 rounded px-2 py-1.5 text-sm" />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Accepted Languages</label>
              <input type="text" value={form.acceptedLanguages ?? 'en,fr'}
                onChange={(e) => setForm({ ...form, acceptedLanguages: e.target.value })}
                className="w-full border border-gray-300 rounded px-2 py-1.5 text-sm" />
            </div>
            <div className="flex items-center pt-5">
              <label className="flex items-center gap-2 text-sm text-gray-700">
                <input type="checkbox" checked={form.acceptsAnonymous ?? true}
                  onChange={(e) => setForm({ ...form, acceptsAnonymous: e.target.checked })}
                  className="rounded border-gray-300" />
                Accept anonymous reports
              </label>
            </div>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-700 mb-1">Scope Description</label>
            <textarea value={form.scopeDescription ?? ''} rows={2}
              onChange={(e) => setForm({ ...form, scopeDescription: e.target.value })}
              className="w-full border border-gray-300 rounded px-2 py-1.5 text-sm" />
          </div>
          <div className="flex justify-end gap-2">
            <button onClick={() => setEditing(false)}
              className="px-3 py-1.5 text-xs text-gray-600 bg-gray-100 rounded hover:bg-gray-200">Cancel</button>
            <button onClick={handleSave} disabled={saveMutation.isPending || !form.contactEmail}
              className="px-3 py-1.5 text-xs text-white bg-primary-600 rounded hover:bg-primary-700 disabled:opacity-50">
              {saveMutation.isPending ? 'Saving...' : 'Save CVD Policy'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
