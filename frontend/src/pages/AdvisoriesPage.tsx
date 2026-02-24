import { useState } from 'react';
import { useAdvisories, useCreateAdvisory, usePublishAdvisory, useNotifyAdvisoryUsers } from '../hooks/useAdvisories';
import { useCraEvents } from '../hooks/useCraEvents';
import { Modal } from '../components/Modal';
import type { SecurityAdvisoryCreateRequest, AdvisoryStatus } from '../types';
import { SEVERITY_BADGE_COLORS } from '@/constants';

const STATUS_COLORS: Record<AdvisoryStatus, string> = {
  DRAFT: 'bg-gray-100 text-gray-800',
  PUBLISHED: 'bg-green-100 text-green-800',
  NOTIFIED: 'bg-blue-100 text-blue-800',
};

export function AdvisoriesPage() {
  const { data: advisories, isLoading } = useAdvisories();
  const { data: events } = useCraEvents();
  const createMutation = useCreateAdvisory();
  const publishMutation = usePublishAdvisory();
  const notifyMutation = useNotifyAdvisoryUsers();

  const [showCreate, setShowCreate] = useState(false);
  const [showNotify, setShowNotify] = useState<string | null>(null);
  const [recipients, setRecipients] = useState('');
  const [form, setForm] = useState<SecurityAdvisoryCreateRequest>({
    craEventId: '', title: '', severity: 'HIGH', description: '',
  });

  const handleCreate = () => {
    createMutation.mutate(form, {
      onSuccess: () => {
        setShowCreate(false);
        setForm({ craEventId: '', title: '', severity: 'HIGH', description: '' });
      },
    });
  };

  const handleNotify = (id: string) => {
    const list = recipients.split(/[,;\n]/).map(r => r.trim()).filter(Boolean);
    if (list.length === 0) return;
    notifyMutation.mutate({ id, recipients: list }, {
      onSuccess: () => { setShowNotify(null); setRecipients(''); },
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Security Advisories</h1>
          <p className="text-sm text-gray-500 mt-1">
            Article 14.3 CRA : notification des utilisateurs des vulnerabilites et correctifs disponibles
          </p>
        </div>
        <button onClick={() => setShowCreate(true)}
          className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium">
          Nouvel advisory
        </button>
      </div>

      {/* Create Modal */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)}>
          <div className="p-6 space-y-4">
            <h2 className="text-lg font-semibold">Nouvel advisory de securite</h2>
            <div className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Evenement CRA</label>
                <select value={form.craEventId} onChange={e => setForm({ ...form, craEventId: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm">
                  <option value="">Selectionner...</option>
                  {events?.map(ev => (
                    <option key={ev.id} value={ev.id}>[{ev.eventType}] {ev.title}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Titre</label>
                <input type="text" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" placeholder="Ex: CVE-2024-XXXX - Vulnerabilite critique" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Severite</label>
                <select value={form.severity} onChange={e => setForm({ ...form, severity: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm">
                  <option value="CRITICAL">Critique</option>
                  <option value="HIGH">Haute</option>
                  <option value="MEDIUM">Moyenne</option>
                  <option value="LOW">Basse</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Versions affectees</label>
                <input type="text" value={form.affectedVersions || ''} onChange={e => setForm({ ...form, affectedVersions: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" placeholder="Ex: < 2.1.0" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                <textarea value={form.description} onChange={e => setForm({ ...form, description: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" rows={3} placeholder="Description de la vulnerabilite..." />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Remediation</label>
                <textarea value={form.remediation || ''} onChange={e => setForm({ ...form, remediation: e.target.value })}
                  className="w-full border rounded-lg px-3 py-2 text-sm" rows={2} placeholder="Actions a prendre pour corriger..." />
              </div>
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <button onClick={() => setShowCreate(false)} className="px-4 py-2 text-sm text-gray-600">Annuler</button>
              <button onClick={handleCreate} disabled={!form.craEventId || !form.title || !form.description || createMutation.isPending}
                className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium disabled:opacity-50">
                {createMutation.isPending ? 'Creation...' : 'Creer'}
              </button>
            </div>
          </div>
      </Modal>

      {/* Notify Modal */}
      <Modal open={!!showNotify} onClose={() => { setShowNotify(null); setRecipients(''); }}>
          <div className="p-6 space-y-4">
            <h2 className="text-lg font-semibold">Notifier les utilisateurs</h2>
            <p className="text-sm text-gray-500">
              Envoyer l'advisory par email aux utilisateurs affectes (Art.14.3 CRA)
            </p>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Adresses email (une par ligne ou separees par des virgules)
              </label>
              <textarea value={recipients} onChange={e => setRecipients(e.target.value)}
                className="w-full border rounded-lg px-3 py-2 text-sm" rows={5}
                placeholder="user1@example.com&#10;user2@example.com" />
            </div>
            <div className="flex justify-end gap-3">
              <button onClick={() => setShowNotify(null)} className="px-4 py-2 text-sm text-gray-600">Annuler</button>
              <button onClick={() => showNotify && handleNotify(showNotify)} disabled={!recipients.trim() || notifyMutation.isPending}
                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm font-medium disabled:opacity-50">
                {notifyMutation.isPending ? 'Envoi...' : 'Envoyer les notifications'}
              </button>
            </div>
          </div>
      </Modal>

      {/* Advisories list */}
      <div className="bg-white rounded-xl shadow-sm border overflow-hidden">
        {isLoading ? (
          <div className="p-8 text-center text-gray-500">Chargement...</div>
        ) : !advisories?.length ? (
          <div className="p-8 text-center text-gray-500">
            <p className="text-lg font-medium mb-1">Aucun advisory</p>
            <p className="text-sm">Creez un advisory pour notifier vos utilisateurs d'une vulnerabilite.</p>
          </div>
        ) : (
          <div className="divide-y">
            {advisories.map(a => (
              <div key={a.id} className="p-4 hover:bg-gray-50">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${SEVERITY_BADGE_COLORS[a.severity] || 'bg-gray-100'}`}>
                        {a.severity}
                      </span>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[a.status]}`}>
                        {a.status}
                      </span>
                    </div>
                    <h3 className="font-medium text-gray-900">{a.title}</h3>
                    <p className="text-sm text-gray-500 mt-1 line-clamp-2">{a.description}</p>
                    {a.affectedVersions && (
                      <p className="text-xs text-gray-400 mt-1">Versions affectees: {a.affectedVersions}</p>
                    )}
                    <div className="flex gap-4 mt-2 text-xs text-gray-400">
                      <span>Cree le {new Date(a.createdAt).toLocaleDateString('fr-FR')}</span>
                      {a.publishedAt && <span>Publie le {new Date(a.publishedAt).toLocaleDateString('fr-FR')}</span>}
                      {a.notifiedAt && <span>Notifie le {new Date(a.notifiedAt).toLocaleDateString('fr-FR')}</span>}
                    </div>
                  </div>
                  <div className="flex gap-2 ml-4">
                    {a.status === 'DRAFT' && (
                      <button onClick={() => publishMutation.mutate(a.id)}
                        className="px-3 py-1.5 bg-green-600 text-white rounded-lg text-xs font-medium hover:bg-green-700">
                        Publier
                      </button>
                    )}
                    {a.status === 'PUBLISHED' && (
                      <button onClick={() => setShowNotify(a.id)}
                        className="px-3 py-1.5 bg-red-600 text-white rounded-lg text-xs font-medium hover:bg-red-700">
                        Notifier utilisateurs
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* CRA Article 14 info */}
      <div className="bg-amber-50 border border-amber-200 rounded-xl p-4">
        <h3 className="font-medium text-amber-900 mb-2">Article 14.3 - Notification aux utilisateurs</h3>
        <p className="text-sm text-amber-800">
          Le CRA exige que les fabricants notifient leurs utilisateurs "sans retard excessif" apres
          la disponibilite d'un correctif. Utilisez cette page pour creer des advisories de securite,
          les publier et notifier les utilisateurs par email. Chaque action est tracee dans la piste d'audit.
        </p>
      </div>
    </div>
  );
}
