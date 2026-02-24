import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  useCraEvent, useCraEventSla, useUpdateCraEvent, useCloseCraEvent,
  useSubmissions, useCreateSubmission, useValidateSubmission,
  useMarkReady, useExportBundle, useMarkSubmitted, useSubmitParallel,
} from '../hooks/useCraEvents';
import { StatusBadge } from '../components/StatusBadge';
import { SlaCountdown } from '../components/SlaCountdown';
import { Modal } from '../components/Modal';
import type { CraEventUpdateRequest, CraEventStatus, SrpSubmission, SubmissionType, MarkSubmittedRequest } from '../types';
import { getErrorMessage } from '../types';

const SUBMISSION_TYPES: SubmissionType[] = ['EARLY_WARNING', 'NOTIFICATION', 'FINAL_REPORT'];

type TabId = 'overview' | 'submissions';

export function CraEventDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const eventId = id!;

  const { data: event, isLoading } = useCraEvent(eventId);
  const { data: sla } = useCraEventSla(eventId);
  const { data: submissions } = useSubmissions(eventId);
  const updateEvent = useUpdateCraEvent();
  const closeEvent = useCloseCraEvent();
  const createSubmission = useCreateSubmission();
  const validateSub = useValidateSubmission();
  const markReady = useMarkReady();
  const exportBundle = useExportBundle();
  const markSubmitted = useMarkSubmitted();
  const submitParallel = useSubmitParallel();

  const [activeTab, setActiveTab] = useState<TabId>('overview');
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState<CraEventUpdateRequest>({});
  const [submitForm, setSubmitForm] = useState<{ subId: string; reference: string } | null>(null);
  const [error, setError] = useState<string | null>(null);

  if (isLoading || !event) return <div className="text-gray-500">Loading...</div>;

  const handleUpdate = () => {
    setError(null);
    updateEvent.mutate(
      { id: eventId, data: editForm },
      {
        onSuccess: () => setEditing(false),
        onError: (err: unknown) => setError(getErrorMessage(err)),
      }
    );
  };

  const handleCreateSubmission = (type: SubmissionType) => {
    setError(null);
    createSubmission.mutate(
      { eventId, data: { submissionType: type } },
      { onError: (err: unknown) => setError(getErrorMessage(err)) }
    );
  };

  const handleMarkSubmitted = () => {
    if (!submitForm) return;
    setError(null);
    const data: MarkSubmittedRequest = { reference: submitForm.reference };
    markSubmitted.mutate(
      { eventId, subId: submitForm.subId, data },
      {
        onSuccess: () => setSubmitForm(null),
        onError: (err: unknown) => setError(getErrorMessage(err)),
      }
    );
  };

  const tabs: { id: TabId; label: string; count?: number }[] = [
    { id: 'overview', label: 'Overview' },
    { id: 'submissions', label: 'Submissions', count: submissions?.length },
  ];

  return (
    <div>
      <button
        onClick={() => navigate('/cra-events')}
        className="text-sm text-primary-600 hover:text-primary-800 mb-4 inline-block"
      >
        &larr; Back to CRA Events
      </button>

      {/* Header */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="flex justify-between items-start">
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-xl font-bold text-gray-900">{event.title}</h1>
              <StatusBadge status={event.eventType} />
              <StatusBadge status={event.status} />
            </div>
            <p className="text-sm text-gray-500 mt-1">
              Product: {event.productName || event.productId.slice(0, 8)} | Created: {new Date(event.createdAt).toLocaleString()}
            </p>
          </div>
          <div className="flex gap-2">
            {event.status !== 'CLOSED' && (
              <>
                <button
                  onClick={() => { setEditing(true); setEditForm({}); }}
                  className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-100"
                >
                  Edit
                </button>
                <button
                  onClick={() => closeEvent.mutate(eventId)}
                  disabled={closeEvent.isPending}
                  className="px-3 py-1.5 text-sm border border-red-300 text-red-600 rounded-lg hover:bg-red-50"
                >
                  Close
                </button>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Error */}
      {error && (
        <div role="alert" className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700 flex justify-between">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="text-red-500 hover:text-red-700 ml-2" aria-label="Fermer le message d'erreur">&times;</button>
        </div>
      )}

      {/* SLA Countdown */}
      {sla && event.status !== 'CLOSED' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <SlaCountdown label="Early Warning" deadline={sla.earlyWarning} />
          <SlaCountdown label="Notification" deadline={sla.notification} />
          <SlaCountdown label="Final Report" deadline={sla.finalReport} />
        </div>
      )}

      {/* Tabs */}
      <div className="border-b border-gray-200 mb-6">
        <nav className="-mb-px flex space-x-8">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`py-3 px-1 border-b-2 text-sm font-medium transition-colors ${
                activeTab === tab.id
                  ? 'border-primary-500 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              {tab.label}
              {tab.count !== undefined && (
                <span className="ml-2 bg-gray-100 text-gray-600 py-0.5 px-2 rounded-full text-xs">{tab.count}</span>
              )}
            </button>
          ))}
        </nav>
      </div>

      {/* Overview Tab */}
      {activeTab === 'overview' && (
        <div className="space-y-6">
          {/* Description */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">Details</h3>
            <p className="text-sm text-gray-600">{event.description || 'No description provided.'}</p>
            <div className="mt-4 grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
              <div>
                <span className="text-xs text-gray-400 block">Detected</span>
                <span className="text-gray-700">{new Date(event.detectedAt).toLocaleString()}</span>
              </div>
              {event.startedAt && (
                <div>
                  <span className="text-xs text-gray-400 block">Started</span>
                  <span className="text-gray-700">{new Date(event.startedAt).toLocaleString()}</span>
                </div>
              )}
              {event.patchAvailableAt && (
                <div>
                  <span className="text-xs text-gray-400 block">Patch Available</span>
                  <span className="text-gray-700">{new Date(event.patchAvailableAt).toLocaleString()}</span>
                </div>
              )}
              {event.resolvedAt && (
                <div>
                  <span className="text-xs text-gray-400 block">Resolved</span>
                  <span className="text-gray-700">{new Date(event.resolvedAt).toLocaleString()}</span>
                </div>
              )}
            </div>
          </div>

          {/* Links */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">
              Linked Entities ({event.links.length})
            </h3>
            {event.links.length === 0 ? (
              <p className="text-sm text-gray-400">No linked releases, findings, or evidences yet.</p>
            ) : (
              <div className="space-y-2">
                {event.links.map((link) => (
                  <div key={link.id} className="flex items-center gap-2 text-sm">
                    <StatusBadge status={link.linkType} />
                    <span className="font-mono text-xs text-gray-500">{link.targetId.slice(0, 12)}...</span>
                    <span className="text-xs text-gray-400">{new Date(link.createdAt).toLocaleDateString()}</span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Participants */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">
              Participants ({event.participants.length})
            </h3>
            {event.participants.length === 0 ? (
              <p className="text-sm text-gray-400">No participants assigned.</p>
            ) : (
              <div className="space-y-2">
                {event.participants.map((p) => (
                  <div key={p.id} className="flex items-center gap-2 text-sm">
                    <StatusBadge status={p.role} />
                    <span className="font-mono text-xs text-gray-500">{p.userId.slice(0, 12)}...</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Submissions Tab */}
      {activeTab === 'submissions' && (
        <div>
          {/* Create submission buttons */}
          {event.status !== 'CLOSED' && (
            <div className="flex gap-2 mb-6">
              {SUBMISSION_TYPES.map((type) => (
                <button
                  key={type}
                  onClick={() => handleCreateSubmission(type)}
                  disabled={createSubmission.isPending}
                  className="px-3 py-2 text-sm border border-primary-300 text-primary-600 rounded-lg hover:bg-primary-50 disabled:opacity-50"
                >
                  + {type.replace(/_/g, ' ')}
                </button>
              ))}
            </div>
          )}

          {/* Submissions list */}
          {!submissions || submissions.length === 0 ? (
            <div className="bg-white rounded-lg shadow p-8 text-center text-gray-400">
              No submissions yet. Create one to start preparing your SRP report.
            </div>
          ) : (
            <div className="space-y-4">
              {submissions.map((sub: SrpSubmission) => (
                <div key={sub.id} className="bg-white rounded-lg shadow p-5">
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="flex items-center gap-2">
                        <StatusBadge status={sub.submissionType} />
                        <StatusBadge status={sub.status} />
                        <span className="text-xs text-gray-400">v{sub.schemaVersion}</span>
                      </div>
                      <p className="text-xs text-gray-400 mt-1">
                        Generated: {new Date(sub.generatedAt).toLocaleString()}
                        {sub.submittedAt && ` | Submitted: ${new Date(sub.submittedAt).toLocaleString()}`}
                        {sub.submittedReference && ` | Ref: ${sub.submittedReference}`}
                      </p>
                      {/* ENISA + CSIRT Status (Art. 14 parallel notification) */}
                      {(sub.enisaStatus || sub.csirtStatus) && (
                        <div className="flex gap-3 mt-2">
                          {sub.enisaStatus && (
                            <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                              sub.enisaStatus === 'SUBMITTED' ? 'bg-green-100 text-green-700' :
                              sub.enisaStatus === 'FAILED' ? 'bg-red-100 text-red-700' :
                              'bg-yellow-100 text-yellow-700'
                            }`}>
                              ENISA: {sub.enisaStatus}
                              {sub.enisaReference && ` (${sub.enisaReference})`}
                            </span>
                          )}
                          {sub.csirtStatus && (
                            <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                              sub.csirtStatus === 'SUBMITTED' ? 'bg-green-100 text-green-700' :
                              sub.csirtStatus === 'FAILED' ? 'bg-red-100 text-red-700' :
                              'bg-yellow-100 text-yellow-700'
                            }`}>
                              CSIRT ({sub.csirtCountryCode || 'N/A'}): {sub.csirtStatus}
                              {sub.csirtReference && ` (${sub.csirtReference})`}
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                    <div className="flex gap-2">
                      {sub.status === 'DRAFT' && (
                        <>
                          <button
                            onClick={() => validateSub.mutate({ eventId, subId: sub.id })}
                            className="px-2 py-1 text-xs border border-gray-300 rounded hover:bg-gray-50"
                          >
                            Validate
                          </button>
                          <button
                            onClick={() => markReady.mutate({ eventId, subId: sub.id }, {
                              onError: (err: unknown) => setError(getErrorMessage(err)),
                            })}
                            className="px-2 py-1 text-xs text-green-600 border border-green-300 rounded hover:bg-green-50"
                          >
                            Mark Ready
                          </button>
                        </>
                      )}
                      {(sub.status === 'READY' || sub.status === 'EXPORTED') && (
                        <>
                          <button
                            onClick={() => exportBundle.mutate({ eventId, subId: sub.id })}
                            className="px-2 py-1 text-xs text-primary-600 border border-primary-300 rounded hover:bg-primary-50"
                          >
                            Download Bundle
                          </button>
                          <button
                            onClick={() => submitParallel.mutate(
                              { eventId, subId: sub.id },
                              { onError: (err: unknown) => setError(getErrorMessage(err)) }
                            )}
                            disabled={submitParallel.isPending}
                            className="px-2 py-1 text-xs text-emerald-700 border border-emerald-400 rounded hover:bg-emerald-50 font-medium"
                          >
                            {submitParallel.isPending ? 'Submitting...' : 'Submit ENISA + CSIRT'}
                          </button>
                          <button
                            onClick={() => setSubmitForm({ subId: sub.id, reference: '' })}
                            className="px-2 py-1 text-xs text-blue-600 border border-blue-300 rounded hover:bg-blue-50"
                          >
                            Mark Submitted
                          </button>
                        </>
                      )}
                    </div>
                  </div>

                  {/* Validation errors */}
                  {(() => {
                    const errors = sub.validationErrors;
                    if (!Array.isArray(errors) || errors.length === 0) return null;
                    return (
                      <div role="alert" className="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg">
                        <p className="text-xs font-medium text-red-700 mb-1">Validation Errors:</p>
                        <ul className="text-xs text-red-600 list-disc pl-4 space-y-0.5">
                          {errors.map((err: string, i: number) => (
                            <li key={i}>{err}</li>
                          ))}
                        </ul>
                      </div>
                    );
                  })()}

                  {/* Content preview */}
                  {sub.contentJson != null && (
                    <details className="mt-3">
                      <summary className="text-xs text-gray-500 cursor-pointer hover:text-gray-700">
                        View content JSON
                      </summary>
                      <pre className="mt-2 p-3 bg-gray-50 rounded text-xs text-gray-600 overflow-x-auto max-h-64">
                        {typeof sub.contentJson === 'string'
                          ? sub.contentJson
                          : JSON.stringify(sub.contentJson, null, 2)}
                      </pre>
                    </details>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Edit Modal */}
      <Modal open={editing} onClose={() => setEditing(false)} maxWidth="max-w-md">
          <div className="p-6">
            <h2 className="text-lg font-semibold mb-4">Edit Event</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Title</label>
                <input
                  type="text"
                  value={editForm.title ?? event.title}
                  onChange={(e) => setEditForm({ ...editForm, title: e.target.value })}
                  aria-label="Titre de l'événement"
                  aria-required="true"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                <textarea
                  value={editForm.description ?? event.description ?? ''}
                  onChange={(e) => setEditForm({ ...editForm, description: e.target.value })}
                  rows={3}
                  aria-label="Description de l'événement"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
                <select
                  value={editForm.status ?? event.status}
                  onChange={(e) => setEditForm({ ...editForm, status: e.target.value as CraEventStatus })}
                  aria-label="Statut de l'événement"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                >
                  <option value="DRAFT">DRAFT</option>
                  <option value="IN_REVIEW">IN REVIEW</option>
                  <option value="SUBMITTED">SUBMITTED</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Patch Available At</label>
                <input
                  type="datetime-local"
                  value={editForm.patchAvailableAt ? editForm.patchAvailableAt.slice(0, 16) : (event.patchAvailableAt ? event.patchAvailableAt.slice(0, 16) : '')}
                  onChange={(e) => setEditForm({ ...editForm, patchAvailableAt: e.target.value ? new Date(e.target.value).toISOString() : undefined })}
                  aria-label="Date de disponibilité du correctif"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Resolved At</label>
                <input
                  type="datetime-local"
                  value={editForm.resolvedAt ? editForm.resolvedAt.slice(0, 16) : (event.resolvedAt ? event.resolvedAt.slice(0, 16) : '')}
                  onChange={(e) => setEditForm({ ...editForm, resolvedAt: e.target.value ? new Date(e.target.value).toISOString() : undefined })}
                  aria-label="Date de résolution"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button onClick={() => setEditing(false)} className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200">Cancel</button>
              <button onClick={handleUpdate} disabled={updateEvent.isPending} className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50">
                {updateEvent.isPending ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>
      </Modal>

      {/* Mark Submitted Modal */}
      <Modal open={!!submitForm} onClose={() => setSubmitForm(null)} maxWidth="max-w-md">
          <div className="p-6">
            <h2 className="text-lg font-semibold mb-4">Mark as Submitted</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">External Reference *</label>
                <input
                  type="text"
                  value={submitForm?.reference ?? ''}
                  onChange={(e) => submitForm && setSubmitForm({ ...submitForm, reference: e.target.value })}
                  placeholder="e.g. SRP-2026-001234"
                  aria-label="Référence externe"
                  aria-required="true"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button onClick={() => setSubmitForm(null)} className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200">Cancel</button>
              <button
                onClick={handleMarkSubmitted}
                disabled={!submitForm?.reference || markSubmitted.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {markSubmitted.isPending ? 'Saving...' : 'Confirm Submission'}
              </button>
            </div>
          </div>
      </Modal>
    </div>
  );
}
