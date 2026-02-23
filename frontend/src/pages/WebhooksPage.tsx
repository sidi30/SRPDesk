import { useState } from 'react';
import { useWebhooks, useCreateWebhook, useDeleteWebhook, useToggleWebhook } from '../hooks/useWebhooks';
import type { WebhookCreateRequest } from '../types';

const CHANNEL_TYPES = [
  { value: 'GENERIC', label: 'HTTP Generique' },
  { value: 'SLACK', label: 'Slack' },
  { value: 'TEAMS', label: 'Microsoft Teams' },
  { value: 'PAGERDUTY', label: 'PagerDuty' },
];

const EVENT_TYPES = [
  { value: '*', label: 'Tous les evenements' },
  { value: 'CRA_SLA_ALERT', label: 'Alertes SLA CRA' },
  { value: 'CRA_DEADLINE_ALERT', label: 'Alertes deadline CRA' },
  { value: 'ADVISORY_PUBLISHED', label: 'Advisory publie' },
];

export function WebhooksPage() {
  const { data: webhooks, isLoading } = useWebhooks();
  const createMutation = useCreateWebhook();
  const deleteMutation = useDeleteWebhook();
  const toggleMutation = useToggleWebhook();

  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState<WebhookCreateRequest>({
    name: '', url: '', channelType: 'GENERIC', eventTypes: '*',
  });

  const handleCreate = () => {
    createMutation.mutate(form, {
      onSuccess: () => { setShowCreate(false); setForm({ name: '', url: '', channelType: 'GENERIC', eventTypes: '*' }); },
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Webhooks sortants</h1>
          <p className="text-sm text-gray-500 mt-1">
            Recevez des notifications CRA en temps reel dans vos outils (Slack, Teams, PagerDuty, HTTP)
          </p>
        </div>
        <button onClick={() => setShowCreate(true)}
          className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium">
          Ajouter un webhook
        </button>
      </div>

      {/* Create Modal */}
      {showCreate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg p-6 space-y-4">
            <h2 className="text-lg font-semibold">Nouveau webhook</h2>
            <div className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nom</label>
                <input type="text" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" placeholder="Ex: Slack #security-alerts" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Type de canal</label>
                <select value={form.channelType} onChange={e => setForm({ ...form, channelType: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm">
                  {CHANNEL_TYPES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">URL</label>
                <input type="url" value={form.url} onChange={e => setForm({ ...form, url: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" placeholder="https://hooks.slack.com/services/..." />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Evenements</label>
                <select value={form.eventTypes} onChange={e => setForm({ ...form, eventTypes: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm">
                  {EVENT_TYPES.map(e => <option key={e.value} value={e.value}>{e.label}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Secret (optionnel, HMAC-SHA256)</label>
                <input type="password" value={form.secret || ''} onChange={e => setForm({ ...form, secret: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" placeholder="Secret pour signature HMAC" />
              </div>
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <button onClick={() => setShowCreate(false)} className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Annuler</button>
              <button onClick={handleCreate} disabled={!form.name || !form.url || createMutation.isPending}
                className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium disabled:opacity-50">
                {createMutation.isPending ? 'Creation...' : 'Creer'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Webhooks list */}
      <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center text-gray-500">Chargement...</div>
        ) : !webhooks?.length ? (
          <div className="p-8 text-center text-gray-500">
            <p className="text-lg font-medium mb-1">Aucun webhook configure</p>
            <p className="text-sm">Ajoutez un webhook pour recevoir les alertes CRA dans vos outils.</p>
          </div>
        ) : (
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nom</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Canal</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">URL</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Evenements</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Statut</th>
                <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {webhooks.map(w => (
                <tr key={w.id}>
                  <td className="px-4 py-3 text-sm font-medium text-gray-900">{w.name}</td>
                  <td className="px-4 py-3 text-sm">
                    <span className="px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">{w.channelType}</span>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-500 truncate max-w-[200px]">{w.url}</td>
                  <td className="px-4 py-3 text-sm text-gray-500">{w.eventTypes === '*' ? 'Tous' : w.eventTypes}</td>
                  <td className="px-4 py-3">
                    <button onClick={() => toggleMutation.mutate({ id: w.id, enabled: !w.enabled })}
                      className={`px-2 py-1 rounded-full text-xs font-medium ${w.enabled ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'}`}>
                      {w.enabled ? 'Actif' : 'Inactif'}
                    </button>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button onClick={() => { if (confirm('Supprimer ce webhook ?')) deleteMutation.mutate(w.id); }}
                      className="text-red-600 hover:text-red-800 text-sm">Supprimer</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Info card */}
      <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
        <h3 className="font-medium text-blue-900 mb-2">Article 14 CRA - Notifications automatiques</h3>
        <ul className="text-sm text-blue-800 space-y-1">
          <li>Les webhooks sont declenches automatiquement lors des alertes SLA (J-6h, J-2h, depassement)</li>
          <li>Supportent Slack, Microsoft Teams, PagerDuty et tout endpoint HTTP generique</li>
          <li>Les payloads sont signes HMAC-SHA256 si un secret est configure</li>
          <li>3 tentatives automatiques en cas d'echec de livraison</li>
        </ul>
      </div>
    </div>
  );
}
