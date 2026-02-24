import { useState } from 'react';
import {
  useCvdReports,
  useCvdAcknowledge,
  useCvdStartTriage,
  useCvdConfirm,
  useCvdReject,
  useCvdStartFix,
  useCvdMarkFixed,
  useCvdDisclose,
} from '../hooks/useCvdReports';
import { getErrorMessage } from '../types';
import type { VulnerabilityReportResponse, VulnerabilityReportStatus } from '../types';

const STATUS_COLORS: Record<VulnerabilityReportStatus, string> = {
  NEW: 'bg-red-100 text-red-800',
  ACKNOWLEDGED: 'bg-yellow-100 text-yellow-800',
  TRIAGING: 'bg-blue-100 text-blue-800',
  CONFIRMED: 'bg-orange-100 text-orange-800',
  REJECTED: 'bg-gray-100 text-gray-800',
  FIXING: 'bg-indigo-100 text-indigo-800',
  FIXED: 'bg-green-100 text-green-800',
  DISCLOSED: 'bg-purple-100 text-purple-800',
};

const STATUS_LABELS: Record<VulnerabilityReportStatus, string> = {
  NEW: 'Nouveau',
  ACKNOWLEDGED: 'Acquitté',
  TRIAGING: 'En triage',
  CONFIRMED: 'Confirmé',
  REJECTED: 'Rejeté',
  FIXING: 'En correction',
  FIXED: 'Corrigé',
  DISCLOSED: 'Divulgué',
};

const ALL_STATUSES: VulnerabilityReportStatus[] = [
  'NEW', 'ACKNOWLEDGED', 'TRIAGING', 'CONFIRMED', 'REJECTED', 'FIXING', 'FIXED', 'DISCLOSED',
];

export function CvdReportsPage() {
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [selectedReport, setSelectedReport] = useState<VulnerabilityReportResponse | null>(null);
  const { data: reports, isLoading, error } = useCvdReports(statusFilter || undefined);

  const acknowledgeMut = useCvdAcknowledge();
  const startTriageMut = useCvdStartTriage();
  const confirmMut = useCvdConfirm();
  const rejectMut = useCvdReject();
  const startFixMut = useCvdStartFix();
  const markFixedMut = useCvdMarkFixed();
  const discloseMut = useCvdDisclose();

  const isPending = acknowledgeMut.isPending || startTriageMut.isPending || confirmMut.isPending
    || rejectMut.isPending || startFixMut.isPending || markFixedMut.isPending || discloseMut.isPending;

  const mutError = acknowledgeMut.error || startTriageMut.error || confirmMut.error
    || rejectMut.error || startFixMut.error || markFixedMut.error || discloseMut.error;

  const onSuccess = (updated: VulnerabilityReportResponse) => setSelectedReport(updated);

  const handleAction = (report: VulnerabilityReportResponse) => {
    switch (report.status) {
      case 'NEW':
        acknowledgeMut.mutate(report.id, { onSuccess });
        break;
      case 'ACKNOWLEDGED':
        startTriageMut.mutate(report.id, { onSuccess });
        break;
      case 'TRIAGING':
        confirmMut.mutate(report.id, { onSuccess });
        break;
      case 'CONFIRMED':
        startFixMut.mutate(report.id, { onSuccess });
        break;
      case 'FIXING':
        markFixedMut.mutate(report.id, { onSuccess });
        break;
      case 'FIXED':
        discloseMut.mutate(report.id, { onSuccess });
        break;
    }
  };

  const handleReject = (report: VulnerabilityReportResponse) => {
    const reason = prompt('Raison du rejet :');
    if (reason) {
      rejectMut.mutate({ id: report.id, reason }, { onSuccess });
    }
  };

  const WORKFLOW_LABELS: Partial<Record<VulnerabilityReportStatus, string>> = {
    NEW: 'Acquitter',
    ACKNOWLEDGED: 'Démarrer le triage',
    TRIAGING: 'Confirmer',
    CONFIRMED: 'Démarrer la correction',
    FIXING: 'Marquer corrigé',
    FIXED: 'Divulguer',
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Rapports de vulnérabilité (CVD)</h1>
      </div>

      {/* Status filter chips */}
      <div className="flex flex-wrap gap-2">
        <button
          onClick={() => setStatusFilter('')}
          className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
            !statusFilter ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          Tous
        </button>
        {ALL_STATUSES.map((s) => (
          <button
            key={s}
            onClick={() => setStatusFilter(s)}
            className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
              statusFilter === s ? 'bg-primary-600 text-white' : `${STATUS_COLORS[s]} hover:opacity-80`
            }`}
          >
            {STATUS_LABELS[s]}
          </button>
        ))}
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
          {getErrorMessage(error)}
        </div>
      )}

      {isLoading ? (
        <div className="text-center py-12 text-gray-500">Chargement...</div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* List */}
          <div className="lg:col-span-2 space-y-3">
            {reports?.length === 0 && (
              <div className="text-center py-12 text-gray-500">Aucun rapport trouvé</div>
            )}
            {reports?.map((report) => (
              <div
                key={report.id}
                onClick={() => setSelectedReport(report)}
                className={`p-4 bg-white rounded-lg border cursor-pointer transition-colors hover:border-primary-300 ${
                  selectedReport?.id === report.id ? 'border-primary-500 ring-2 ring-primary-200' : 'border-gray-200'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <code className="text-xs font-mono text-gray-500">{report.trackingId}</code>
                      <span className={`px-2 py-0.5 rounded text-xs font-medium ${STATUS_COLORS[report.status]}`}>
                        {STATUS_LABELS[report.status]}
                      </span>
                      {report.severityEstimate && (
                        <span className="px-2 py-0.5 rounded text-xs font-medium bg-gray-100 text-gray-700">
                          {report.severityEstimate}
                        </span>
                      )}
                    </div>
                    <h3 className="text-sm font-semibold text-gray-900 truncate">{report.title}</h3>
                    <p className="text-xs text-gray-500 mt-1">
                      {report.anonymous ? 'Anonyme' : report.reporterName || 'N/A'} &middot;{' '}
                      {new Date(report.submittedAt).toLocaleDateString('fr-FR')}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Detail panel */}
          <div className="lg:col-span-1">
            {selectedReport ? (
              <div className="bg-white rounded-lg border border-gray-200 p-5 space-y-4 sticky top-4">
                <div className="flex items-center justify-between">
                  <code className="text-sm font-mono font-bold">{selectedReport.trackingId}</code>
                  <span className={`px-2 py-1 rounded text-xs font-medium ${STATUS_COLORS[selectedReport.status]}`}>
                    {STATUS_LABELS[selectedReport.status]}
                  </span>
                </div>

                <h2 className="text-lg font-semibold">{selectedReport.title}</h2>
                <p className="text-sm text-gray-600 whitespace-pre-wrap">{selectedReport.description}</p>

                <div className="grid grid-cols-2 gap-3 text-xs">
                  <div>
                    <span className="text-gray-500">Rapporteur</span>
                    <p className="font-medium">{selectedReport.anonymous ? 'Anonyme' : selectedReport.reporterName || '-'}</p>
                  </div>
                  <div>
                    <span className="text-gray-500">Sévérité</span>
                    <p className="font-medium">{selectedReport.severityEstimate || selectedReport.internalSeverity || '-'}</p>
                  </div>
                  {selectedReport.affectedComponent && (
                    <div>
                      <span className="text-gray-500">Composant</span>
                      <p className="font-medium">{selectedReport.affectedComponent}</p>
                    </div>
                  )}
                  {selectedReport.affectedVersions && (
                    <div>
                      <span className="text-gray-500">Versions</span>
                      <p className="font-medium">{selectedReport.affectedVersions}</p>
                    </div>
                  )}
                  {selectedReport.cveId && (
                    <div>
                      <span className="text-gray-500">CVE</span>
                      <p className="font-medium">{selectedReport.cveId}</p>
                    </div>
                  )}
                  {selectedReport.cvssScore != null && (
                    <div>
                      <span className="text-gray-500">CVSS</span>
                      <p className="font-medium">{selectedReport.cvssScore}</p>
                    </div>
                  )}
                  <div>
                    <span className="text-gray-500">Soumis le</span>
                    <p className="font-medium">{new Date(selectedReport.submittedAt).toLocaleDateString('fr-FR')}</p>
                  </div>
                  {selectedReport.disclosureDeadline && (
                    <div>
                      <span className="text-gray-500">Deadline divulgation</span>
                      <p className="font-medium">{new Date(selectedReport.disclosureDeadline).toLocaleDateString('fr-FR')}</p>
                    </div>
                  )}
                </div>

                {selectedReport.internalNotes && (
                  <div>
                    <span className="text-xs text-gray-500">Notes internes</span>
                    <p className="text-sm bg-gray-50 rounded p-2 mt-1">{selectedReport.internalNotes}</p>
                  </div>
                )}

                {/* Workflow actions */}
                <div className="flex flex-wrap gap-2 pt-2 border-t">
                  {WORKFLOW_LABELS[selectedReport.status] && (
                    <button
                      onClick={() => handleAction(selectedReport)}
                      disabled={isPending}
                      className="px-3 py-1.5 bg-primary-600 text-white text-sm rounded-lg hover:bg-primary-700 disabled:opacity-50"
                    >
                      {isPending ? '...' : WORKFLOW_LABELS[selectedReport.status]}
                    </button>
                  )}
                  {selectedReport.status === 'TRIAGING' && (
                    <button
                      onClick={() => handleReject(selectedReport)}
                      disabled={isPending}
                      className="px-3 py-1.5 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 disabled:opacity-50"
                    >
                      Rejeter
                    </button>
                  )}
                  {selectedReport.status === 'REJECTED' && (
                    <button
                      onClick={() => discloseMut.mutate(selectedReport.id, { onSuccess })}
                      disabled={isPending}
                      className="px-3 py-1.5 bg-purple-600 text-white text-sm rounded-lg hover:bg-purple-700 disabled:opacity-50"
                    >
                      Divulguer
                    </button>
                  )}
                </div>

                {mutError && (
                  <div className="text-xs text-red-600">{getErrorMessage(mutError)}</div>
                )}
              </div>
            ) : (
              <div className="bg-gray-50 rounded-lg border border-gray-200 p-8 text-center text-gray-500">
                Sélectionnez un rapport pour voir les détails
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
