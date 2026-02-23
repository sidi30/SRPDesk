import { useState } from 'react';
import type { CraChecklistItem, CraChecklistUpdateRequest, ChecklistStatus } from '../types';
import { FR } from '../i18n/fr';

interface CraChecklistTableProps {
  items: CraChecklistItem[];
  onUpdate: (itemId: string, data: CraChecklistUpdateRequest) => void;
  isUpdating?: boolean;
}

const STATUS_OPTIONS: { value: ChecklistStatus; label: string; color: string }[] = [
  { value: 'NOT_ASSESSED', label: 'Non évalué', color: 'bg-gray-100 text-gray-700' },
  { value: 'COMPLIANT', label: 'Conforme', color: 'bg-green-100 text-green-700' },
  { value: 'PARTIALLY_COMPLIANT', label: 'Partiellement conforme', color: 'bg-yellow-100 text-yellow-700' },
  { value: 'NON_COMPLIANT', label: 'Non conforme', color: 'bg-red-100 text-red-700' },
  { value: 'NOT_APPLICABLE', label: 'Non applicable', color: 'bg-blue-100 text-blue-700' },
];

function getStatusBadge(status: string) {
  const opt = STATUS_OPTIONS.find(s => s.value === status);
  if (!opt) return <span className="px-2 py-0.5 text-xs rounded bg-gray-100 text-gray-600">{status}</span>;
  return <span className={`px-2 py-0.5 text-xs rounded font-medium ${opt.color}`}>{opt.label}</span>;
}

export function CraChecklistTable({ items, onUpdate, isUpdating }: CraChecklistTableProps) {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editStatus, setEditStatus] = useState<ChecklistStatus>('NOT_ASSESSED');
  const [editNotes, setEditNotes] = useState('');

  const categories = [...new Set(items.map(i => i.category))];

  const startEdit = (item: CraChecklistItem) => {
    setEditingId(item.id);
    setEditStatus(item.status);
    setEditNotes(item.notes || '');
  };

  const saveEdit = (itemId: string) => {
    onUpdate(itemId, { status: editStatus, notes: editNotes || undefined });
    setEditingId(null);
  };

  return (
    <div className="space-y-6">
      {categories.map(category => {
        const categoryItems = items.filter(i => i.category === category);
        const compliant = categoryItems.filter(i => i.status === 'COMPLIANT' || i.status === 'NOT_APPLICABLE').length;
        const categoryLabel = FR.checklistCategory?.[category] || category.replace(/_/g, ' ');

        return (
          <div key={category}>
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-sm font-semibold text-gray-700">{categoryLabel}</h3>
              <span className="text-xs text-gray-500">
                {compliant}/{categoryItems.length} {FR.checklist?.compliant || 'conformes'}
              </span>
            </div>
            <div className="bg-white rounded-lg border divide-y">
              {categoryItems.map(item => (
                <div key={item.id} className="p-3">
                  {editingId === item.id ? (
                    <div className="space-y-2">
                      <div className="flex items-start justify-between">
                        <div>
                          <span className="text-xs font-mono text-gray-400 mr-2">{item.requirementRef}</span>
                          <span className="text-sm font-medium text-gray-900">{item.title}</span>
                        </div>
                      </div>
                      <select
                        value={editStatus}
                        onChange={(e) => setEditStatus(e.target.value as ChecklistStatus)}
                        className="text-sm border border-gray-300 rounded px-2 py-1"
                      >
                        {STATUS_OPTIONS.map(opt => (
                          <option key={opt.value} value={opt.value}>{opt.label}</option>
                        ))}
                      </select>
                      <textarea
                        value={editNotes}
                        onChange={(e) => setEditNotes(e.target.value)}
                        placeholder={FR.checklist?.notesPlaceholder || 'Notes...'}
                        rows={2}
                        className="w-full text-sm border border-gray-300 rounded px-2 py-1"
                      />
                      <div className="flex gap-2">
                        <button
                          onClick={() => saveEdit(item.id)}
                          disabled={isUpdating}
                          className="px-3 py-1 text-xs font-medium text-white bg-primary-600 rounded hover:bg-primary-700 disabled:opacity-50"
                        >
                          {isUpdating ? FR.checklist?.saving || 'Enregistrement...' : FR.checklist?.save || 'Enregistrer'}
                        </button>
                        <button
                          onClick={() => setEditingId(null)}
                          className="px-3 py-1 text-xs font-medium text-gray-600 bg-gray-100 rounded hover:bg-gray-200"
                        >
                          {FR.checklist?.cancel || 'Annuler'}
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div
                      className="flex items-start justify-between cursor-pointer hover:bg-gray-50 -m-3 p-3 rounded"
                      onClick={() => startEdit(item)}
                    >
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <span className="text-xs font-mono text-gray-400">{item.requirementRef}</span>
                          <span className="text-sm font-medium text-gray-900">{item.title}</span>
                        </div>
                        {item.description && (
                          <p className="text-xs text-gray-500 mt-0.5 line-clamp-1">{item.description}</p>
                        )}
                        {item.notes && (
                          <p className="text-xs text-blue-600 mt-0.5 italic">{item.notes}</p>
                        )}
                      </div>
                      <div className="ml-3 flex items-center gap-2">
                        {item.evidenceIds.length > 0 && (
                          <span className="text-xs text-gray-400" title={`${item.evidenceIds.length} evidence(s)`}>
                            {item.evidenceIds.length} ev.
                          </span>
                        )}
                        {getStatusBadge(item.status)}
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
