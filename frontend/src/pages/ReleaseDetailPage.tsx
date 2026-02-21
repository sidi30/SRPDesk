import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useEvidences, useUploadEvidence, useUploadSbom, useComponents } from '../hooks/useEvidences';
import { useReleaseFindings, useAddDecision, useTriggerScan } from '../hooks/useFindings';
import { useExportPack } from '../hooks/useReleases';
import { evidencesApi } from '../api/evidences';
import { StatusBadge } from '../components/StatusBadge';
import { FileUpload } from '../components/FileUpload';
import { FindingCard } from '../components/FindingCard';
import type { Evidence, ComponentItem, Finding, EvidenceType, FindingDecisionRequest } from '../types';

const EVIDENCE_TYPES: EvidenceType[] = [
  'SBOM',
  'TEST_REPORT',
  'VULNERABILITY_SCAN',
  'PENTEST_REPORT',
  'DESIGN_DOC',
  'INCIDENT_RESPONSE_PLAN',
  'UPDATE_POLICY',
  'CONFORMITY_DECLARATION',
  'OTHER',
];

const DECISION_TYPES = ['NOT_AFFECTED', 'PATCH_PLANNED', 'MITIGATED', 'FIXED'] as const;
const FINDING_STATUSES = ['', 'OPEN', 'NOT_AFFECTED', 'PATCH_PLANNED', 'MITIGATED', 'FIXED'] as const;

type TabId = 'evidences' | 'components' | 'findings';

export function ReleaseDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const releaseId = id!;

  const [activeTab, setActiveTab] = useState<TabId>('evidences');
  const [uploadType, setUploadType] = useState<EvidenceType>('TEST_REPORT');
  const [findingStatusFilter, setFindingStatusFilter] = useState<string>('');

  // Decision form state
  const [decisionFindingId, setDecisionFindingId] = useState<string | null>(null);
  const [decisionForm, setDecisionForm] = useState<FindingDecisionRequest>({
    decisionType: 'NOT_AFFECTED',
    rationale: '',
  });
  const [error, setError] = useState<string | null>(null);

  const { data: evidences, isLoading: evidencesLoading } = useEvidences(releaseId);
  const { data: components, isLoading: componentsLoading } = useComponents(releaseId);
  const { data: findings, isLoading: findingsLoading } = useReleaseFindings(releaseId, findingStatusFilter || undefined);

  const uploadEvidence = useUploadEvidence(releaseId);
  const uploadSbom = useUploadSbom(releaseId);
  const exportPack = useExportPack();
  const addDecision = useAddDecision();
  const triggerScan = useTriggerScan(releaseId);

  const handleEvidenceUpload = (file: File) => {
    uploadEvidence.mutate(
      { file, type: uploadType },
      {
        onError: (err: any) => {
          setError(err.response?.data?.detail || err.message || 'Upload failed');
        },
      }
    );
  };

  const handleSbomUpload = (file: File) => {
    uploadSbom.mutate(file, {
      onError: (err: any) => {
        setError(err.response?.data?.detail || err.message || 'SBOM upload failed');
      },
    });
  };

  const handleDownload = (evidence: Evidence) => {
    evidencesApi.download(evidence.id, evidence.filename);
  };

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

  const handleTriggerScan = () => {
    setError(null);
    triggerScan.mutate(undefined, {
      onError: (err: any) => {
        setError(err.response?.data?.detail || err.message || 'Scan failed');
      },
    });
  };

  const tabs: { id: TabId; label: string; count?: number }[] = [
    { id: 'evidences', label: 'Evidences', count: evidences?.length },
    { id: 'components', label: 'Components', count: components?.length },
    { id: 'findings', label: 'Findings', count: findings?.length },
  ];

  const formatBytes = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  return (
    <div>
      <button
        onClick={() => navigate(-1)}
        className="text-sm text-primary-600 hover:text-primary-800 mb-4 inline-block"
      >
        &larr; Back
      </button>

      {/* Release Header */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Release {releaseId.slice(0, 8)}...</h1>
            <p className="text-sm text-gray-500 mt-1">Release ID: {releaseId}</p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => exportPack.mutate(releaseId)}
              disabled={exportPack.isPending}
              className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-100 disabled:opacity-50"
            >
              {exportPack.isPending ? 'Exporting...' : 'Export Compliance Pack'}
            </button>
          </div>
        </div>
      </div>

      {/* Error Display */}
      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700 flex justify-between items-center">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="text-red-500 hover:text-red-700 ml-2">
            &times;
          </button>
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
                <span className="ml-2 bg-gray-100 text-gray-600 py-0.5 px-2 rounded-full text-xs">
                  {tab.count}
                </span>
              )}
            </button>
          ))}
        </nav>
      </div>

      {/* Evidences Tab */}
      {activeTab === 'evidences' && (
        <div>
          {/* Upload Section */}
          <div className="bg-white rounded-lg shadow p-6 mb-6">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">Upload Evidence</h3>
            <div className="flex items-end gap-4 mb-4">
              <div className="flex-1">
                <label className="block text-xs font-medium text-gray-500 mb-1">Evidence Type</label>
                <select
                  value={uploadType}
                  onChange={(e) => setUploadType(e.target.value as EvidenceType)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                >
                  {EVIDENCE_TYPES.map((t) => (
                    <option key={t} value={t}>
                      {t.replace(/_/g, ' ')}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <FileUpload
              onUpload={handleEvidenceUpload}
              isLoading={uploadEvidence.isPending}
            />
          </div>

          {/* SBOM Upload */}
          <div className="bg-white rounded-lg shadow p-6 mb-6">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">Upload SBOM</h3>
            <p className="text-xs text-gray-500 mb-3">
              Upload a CycloneDX or SPDX SBOM file. Components will be extracted automatically.
            </p>
            <FileUpload
              onUpload={handleSbomUpload}
              isLoading={uploadSbom.isPending}
              accept=".json,.xml"
            />
            {uploadSbom.isSuccess && uploadSbom.data && (
              <div className="mt-3 p-3 bg-green-50 border border-green-200 rounded-lg text-sm text-green-700">
                SBOM uploaded successfully. {uploadSbom.data.componentCount} components extracted.
              </div>
            )}
          </div>

          {/* Evidence List */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-sm font-semibold text-gray-700 mb-4">Uploaded Evidences</h3>
            {evidencesLoading ? (
              <div className="text-gray-500 text-sm">Loading...</div>
            ) : !evidences || evidences.length === 0 ? (
              <div className="text-gray-400 text-sm py-4">No evidences uploaded yet.</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Filename</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Size</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">SHA-256</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Uploaded</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {evidences.map((ev: Evidence) => (
                      <tr key={ev.id} className="hover:bg-gray-50">
                        <td className="px-4 py-3 text-sm font-medium text-gray-900">{ev.filename}</td>
                        <td className="px-4 py-3">
                          <StatusBadge status={ev.type} />
                        </td>
                        <td className="px-4 py-3 text-sm text-gray-500">{formatBytes(ev.size)}</td>
                        <td className="px-4 py-3 text-xs text-gray-400 font-mono truncate max-w-[120px]" title={ev.sha256}>
                          {ev.sha256.slice(0, 16)}...
                        </td>
                        <td className="px-4 py-3 text-xs text-gray-500">
                          {new Date(ev.createdAt).toLocaleString()}
                        </td>
                        <td className="px-4 py-3">
                          <button
                            onClick={() => handleDownload(ev)}
                            className="text-sm text-primary-600 hover:text-primary-800"
                          >
                            Download
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Components Tab */}
      {activeTab === 'components' && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">SBOM Components</h3>
          {componentsLoading ? (
            <div className="text-gray-500 text-sm">Loading...</div>
          ) : !components || components.length === 0 ? (
            <div className="text-gray-400 text-sm py-4">
              No components found. Upload an SBOM to populate the component list.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Version</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">PURL</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {components.map((comp: ComponentItem, idx: number) => (
                    <tr key={idx} className="hover:bg-gray-50">
                      <td className="px-4 py-3 text-sm font-medium text-gray-900">{comp.name}</td>
                      <td className="px-4 py-3 text-sm text-gray-600">{comp.version}</td>
                      <td className="px-4 py-3 text-sm text-gray-500">{comp.type}</td>
                      <td className="px-4 py-3 text-xs text-gray-400 font-mono truncate max-w-[300px]" title={comp.purl}>
                        {comp.purl}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div className="mt-3 text-xs text-gray-400">
                {components.length} component{components.length !== 1 ? 's' : ''} total
              </div>
            </div>
          )}
        </div>
      )}

      {/* Findings Tab */}
      {activeTab === 'findings' && (
        <div>
          {/* SBOM → Scan → Review Stepper */}
          {(() => {
            const hasComponents = !!(components && components.length > 0);
            const hasFindings = !!(findings && findings.length > 0);
            const decidedCount = findings?.filter((f: Finding) => f.decisions && f.decisions.length > 0).length || 0;
            const totalFindings = findings?.length || 0;
            const allDecided = totalFindings > 0 && decidedCount === totalFindings;

            const steps = [
              {
                label: 'Upload SBOM',
                done: hasComponents,
                detail: hasComponents ? `${components!.length} components` : 'No SBOM yet',
              },
              {
                label: 'Trigger Scan',
                done: hasFindings,
                active: hasComponents && !hasFindings,
                detail: hasFindings ? `${totalFindings} findings` : hasComponents ? 'Ready to scan' : 'Upload SBOM first',
              },
              {
                label: 'Review & Decide',
                done: allDecided,
                active: hasFindings && !allDecided,
                detail: hasFindings ? `${decidedCount}/${totalFindings} decided` : 'Scan first',
              },
            ];

            return (
              <div className="bg-white rounded-lg shadow p-6 mb-6">
                <div className="flex items-center justify-between">
                  {steps.map((step, i) => (
                    <div key={step.label} className="flex items-center flex-1">
                      <div className="flex flex-col items-center">
                        <div
                          className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold ${
                            step.done
                              ? 'bg-green-500 text-white'
                              : step.active
                                ? 'bg-primary-500 text-white'
                                : 'bg-gray-200 text-gray-500'
                          }`}
                        >
                          {step.done ? (
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            </svg>
                          ) : (
                            i + 1
                          )}
                        </div>
                        <span className={`mt-1 text-xs font-medium ${step.done ? 'text-green-700' : step.active ? 'text-primary-700' : 'text-gray-400'}`}>
                          {step.label}
                        </span>
                        <span className="text-[10px] text-gray-400">{step.detail}</span>
                      </div>
                      {i < steps.length - 1 && (
                        <div className={`flex-1 h-0.5 mx-3 ${step.done ? 'bg-green-400' : 'bg-gray-200'}`} />
                      )}
                    </div>
                  ))}
                </div>
              </div>
            );
          })()}

          {/* Controls */}
          <div className="flex justify-between items-center mb-4">
            <select
              value={findingStatusFilter}
              onChange={(e) => setFindingStatusFilter(e.target.value)}
              className="text-sm border border-gray-300 rounded-lg px-3 py-1.5 focus:ring-primary-500 focus:border-primary-500"
            >
              {FINDING_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s || 'All Statuses'}
                </option>
              ))}
            </select>
            <div className="relative group">
              <button
                onClick={handleTriggerScan}
                disabled={triggerScan.isPending || !components?.length}
                className="inline-flex items-center gap-2 px-4 py-2.5 text-sm font-medium bg-primary-600 text-white rounded-lg shadow-sm hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                {triggerScan.isPending ? 'Scanning...' : 'Trigger Vulnerability Scan'}
              </button>
              {!components?.length && (
                <div className="absolute bottom-full right-0 mb-2 px-3 py-1.5 bg-gray-800 text-white text-xs rounded-lg whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
                  Upload an SBOM first to enable scanning
                </div>
              )}
            </div>
          </div>

          {triggerScan.isSuccess && triggerScan.data && (
            <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg text-sm text-blue-700">
              Scan complete. {triggerScan.data.newFindings} new finding{triggerScan.data.newFindings !== 1 ? 's' : ''} detected.
            </div>
          )}

          <div className="bg-white rounded-lg shadow p-6">
            {findingsLoading ? (
              <div className="text-gray-500 text-sm">Loading...</div>
            ) : !findings || findings.length === 0 ? (
              <div className="text-gray-400 text-sm py-4">
                No findings found{findingStatusFilter ? ` with status ${findingStatusFilter}` : ''}. Upload an SBOM and trigger a scan to check for vulnerabilities.
              </div>
            ) : (
              <div className="space-y-4">
                {findings.map((f: Finding) => (
                  <FindingCard
                    key={f.id}
                    finding={f}
                    onAddDecision={setDecisionFindingId}
                  />
                ))}
              </div>
            )}
          </div>
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
