import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCraEvents, useCreateCraEvent } from '../hooks/useCraEvents';
import { useProducts } from '../hooks/useProducts';
import { StatusBadge } from '../components/StatusBadge';
import type { CraEvent, CraEventCreateRequest, CraEventType } from '../types';

const EVENT_STATUSES = ['', 'DRAFT', 'IN_REVIEW', 'SUBMITTED', 'CLOSED'] as const;
const EVENT_TYPES: CraEventType[] = ['EXPLOITED_VULNERABILITY', 'SEVERE_INCIDENT'];

export function CraEventsPage() {
  const navigate = useNavigate();
  const { data: products } = useProducts();
  const [productFilter, setProductFilter] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [showCreate, setShowCreate] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { data: events, isLoading } = useCraEvents(
    productFilter || undefined,
    statusFilter || undefined
  );
  const createEvent = useCreateCraEvent();

  const [form, setForm] = useState<CraEventCreateRequest>({
    productId: '',
    eventType: 'EXPLOITED_VULNERABILITY',
    title: '',
    detectedAt: new Date().toISOString().slice(0, 16),
  });

  const handleCreate = () => {
    if (!form.productId || !form.title) return;
    setError(null);
    createEvent.mutate(
      { ...form, detectedAt: new Date(form.detectedAt).toISOString() },
      {
        onSuccess: (ev) => {
          setShowCreate(false);
          setForm({ productId: '', eventType: 'EXPLOITED_VULNERABILITY', title: '', detectedAt: new Date().toISOString().slice(0, 16) });
          navigate(`/cra-events/${ev.id}`);
        },
        onError: (err: any) => setError(err.response?.data?.detail || err.message || 'Failed to create event'),
      }
    );
  };

  const overdueCount = events?.filter((e) => {
    // Simple overdue check: DRAFT/IN_REVIEW older than 24h from detectedAt
    const hoursSinceDetection = (Date.now() - new Date(e.detectedAt).getTime()) / 3600000;
    return (e.status === 'DRAFT' || e.status === 'IN_REVIEW') && hoursSinceDetection > 24;
  }).length || 0;

  return (
    <div>
      <div className="flex justify-between items-start mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">CRA War Room</h1>
          <p className="mt-1 text-sm text-gray-500">Security events requiring CRA reporting</p>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700"
        >
          New Event
        </button>
      </div>

      {/* Summary stats */}
      {events && events.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-white rounded-lg border p-4">
            <p className="text-sm text-gray-500">Total Events</p>
            <p className="text-2xl font-bold">{events.length}</p>
          </div>
          <div className="bg-white rounded-lg border p-4">
            <p className="text-sm text-gray-500">Draft</p>
            <p className="text-2xl font-bold text-gray-600">
              {events.filter((e) => e.status === 'DRAFT').length}
            </p>
          </div>
          <div className="bg-white rounded-lg border p-4">
            <p className="text-sm text-gray-500">In Review</p>
            <p className="text-2xl font-bold text-yellow-600">
              {events.filter((e) => e.status === 'IN_REVIEW').length}
            </p>
          </div>
          <div className="bg-white rounded-lg border p-4">
            <p className="text-sm text-gray-500">Potentially Overdue</p>
            <p className={`text-2xl font-bold ${overdueCount > 0 ? 'text-red-600' : 'text-green-600'}`}>
              {overdueCount}
            </p>
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="flex flex-wrap gap-4 mb-6">
        <div>
          <label className="block text-xs font-medium text-gray-500 mb-1">Product</label>
          <select
            value={productFilter}
            onChange={(e) => setProductFilter(e.target.value)}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500 min-w-[200px]"
          >
            <option value="">All Products</option>
            {products?.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
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
            {EVENT_STATUSES.map((s) => (
              <option key={s} value={s}>{s || 'All Statuses'}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Event list */}
      {isLoading ? (
        <div className="text-gray-500 text-sm">Loading...</div>
      ) : !events || events.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center text-gray-400">
          No CRA events found. Create one when a security incident needs to be reported.
        </div>
      ) : (
        <div className="space-y-3">
          {events.map((ev: CraEvent) => {
            const hoursSinceDetection = (Date.now() - new Date(ev.detectedAt).getTime()) / 3600000;
            const isOverdue = (ev.status === 'DRAFT' || ev.status === 'IN_REVIEW') && hoursSinceDetection > 24;

            return (
              <div
                key={ev.id}
                onClick={() => navigate(`/cra-events/${ev.id}`)}
                className={`bg-white rounded-lg shadow p-4 cursor-pointer hover:bg-gray-50 border-l-4 ${
                  isOverdue ? 'border-l-red-500' : ev.status === 'CLOSED' ? 'border-l-gray-300' : 'border-l-primary-500'
                }`}
              >
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-medium text-gray-900">{ev.title}</span>
                      <StatusBadge status={ev.eventType} />
                      <StatusBadge status={ev.status} />
                      {isOverdue && (
                        <span className="text-xs font-medium text-red-600 bg-red-50 px-2 py-0.5 rounded">
                          OVERDUE
                        </span>
                      )}
                    </div>
                    <div className="text-xs text-gray-400 mt-2 flex flex-wrap gap-x-4">
                      <span>Product: {ev.productName || ev.productId.slice(0, 8)}</span>
                      <span>Detected: {new Date(ev.detectedAt).toLocaleString()}</span>
                      <span>Links: {ev.links.length}</span>
                      <span>Participants: {ev.participants.length}</span>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Create Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <h2 className="text-lg font-semibold mb-4">New CRA Event</h2>
            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">{error}</div>
            )}
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Product *</label>
                <select
                  value={form.productId}
                  onChange={(e) => setForm({ ...form, productId: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                >
                  <option value="">Select a product</option>
                  {products?.map((p) => (
                    <option key={p.id} value={p.id}>{p.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Event Type *</label>
                <select
                  value={form.eventType}
                  onChange={(e) => setForm({ ...form, eventType: e.target.value as CraEventType })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                >
                  {EVENT_TYPES.map((t) => (
                    <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Title *</label>
                <input
                  type="text"
                  value={form.title}
                  onChange={(e) => setForm({ ...form, title: e.target.value })}
                  placeholder="e.g. Log4Shell exploitation detected"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                <textarea
                  value={form.description || ''}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                  rows={3}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Detected At *</label>
                <input
                  type="datetime-local"
                  value={form.detectedAt}
                  onChange={(e) => setForm({ ...form, detectedAt: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => { setShowCreate(false); setError(null); }}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
              >
                Cancel
              </button>
              <button
                onClick={handleCreate}
                disabled={!form.productId || !form.title || createEvent.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {createEvent.isPending ? 'Creating...' : 'Create Event'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
