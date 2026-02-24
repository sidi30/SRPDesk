import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useProduct, useUpdateProduct } from '../hooks/useProducts';
import { useReleases, useCreateRelease, useExportPack } from '../hooks/useReleases';
import { useProductFindings } from '../hooks/useFindings';
import { useReadinessScore, useSnapshotReadiness } from '../hooks/useReadiness';
import { useCraChecklist, useInitializeCraChecklist, useUpdateCraChecklistItem } from '../hooks/useCraChecklist';
import { StatusBadge } from '../components/StatusBadge';
import { ReadinessGauge } from '../components/ReadinessGauge';
import { CraChecklistTable } from '../components/CraChecklistTable';
import { CvdPolicyCard } from '../components/CvdPolicyCard';
import { useAuth } from '../auth/AuthProvider';
import { FR } from '../i18n/fr';
import { FEATURES } from '../config/features';
import { useEuDocs, useCreateEuDoc, useSignEuDoc, usePublishEuDoc } from '../hooks/useEuDoc';
import { useConformityAssessment, useInitiateAssessment, useCompleteStep, useApproveAssessment } from '../hooks/useConformityAssessment';
import { useRiskAssessments, useCreateRiskAssessment } from '../hooks/useRiskAssessment';
import { useAppliedStandards, useCreateStandard, useDeleteStandard } from '../hooks/useAppliedStandards';
import type { ReleaseCreateRequest, ProductUpdateRequest, Release, Finding, EuDocRequest, ConformityStep, AppliedStandardRequest } from '../types';
import { getErrorMessage } from '../types';

const PRODUCT_TYPES = ['DEFAULT', 'CLASS_I', 'CLASS_II', 'IMPORTANT_CLASS_I', 'IMPORTANT_CLASS_II', 'CRITICAL'] as const;
const CRITICALITY_LEVELS = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as const;
const FINDING_STATUSES = ['', 'OPEN', 'NOT_AFFECTED', 'PATCH_PLANNED', 'MITIGATED', 'FIXED'] as const;

export function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { hasRole } = useAuth();
  const { data: product, isLoading: productLoading } = useProduct(id!);
  const { data: releases, isLoading: releasesLoading } = useReleases(id!);
  const [findingStatusFilter, setFindingStatusFilter] = useState<string>('');
  const { data: findings } = useProductFindings(id!, findingStatusFilter || undefined);

  // Readiness & Checklist
  const { data: readiness } = useReadinessScore(id!);
  const { data: checklistItems } = useCraChecklist(id!);
  const initChecklist = useInitializeCraChecklist(id!);
  const updateChecklistItem = useUpdateCraChecklistItem(id!);
  const snapshotReadiness = useSnapshotReadiness(id!);

  // New CRA modules
  const { data: euDocs } = useEuDocs(id!);
  const createEuDoc = useCreateEuDoc(id!);
  const signEuDoc = useSignEuDoc(id!);
  const publishEuDoc = usePublishEuDoc(id!);
  const conformityModule = product?.conformityPath?.includes('THIRD_PARTY') ? 'MODULE_H' : 'MODULE_A';
  const { data: conformityAssessment } = useConformityAssessment(id!, conformityModule);
  const initiateAssessment = useInitiateAssessment(id!);
  const completeStep = useCompleteStep(id!);
  const approveAssessment = useApproveAssessment(id!);
  const { data: riskAssessments } = useRiskAssessments(id!);
  const createRiskAssessment = useCreateRiskAssessment(id!);
  const { data: appliedStandards } = useAppliedStandards(id!);
  const createStandard = useCreateStandard(id!);
  const deleteStandard = useDeleteStandard(id!);

  const createRelease = useCreateRelease(id!);
  const updateProduct = useUpdateProduct();
  const exportPack = useExportPack();

  const [showCreateRelease, setShowCreateRelease] = useState(false);
  const [showEditProduct, setShowEditProduct] = useState(false);
  const [releaseForm, setReleaseForm] = useState<ReleaseCreateRequest>({ version: '', gitRef: '' });
  const [editForm, setEditForm] = useState<ProductUpdateRequest>({ name: '', type: '', criticality: '' });
  const [error, setError] = useState<string | null>(null);
  type TabType = 'releases' | 'findings' | 'readiness' | 'checklist' | 'eu-doc' | 'conformity' | 'risk' | 'standards';
  const [activeTab, setActiveTab] = useState<TabType>('releases');

  if (productLoading) return <div className="text-gray-500">Loading product...</div>;
  if (!product) return <div className="text-red-500">Product not found</div>;

  const handleCreateRelease = () => {
    setError(null);
    createRelease.mutate(releaseForm, {
      onSuccess: () => {
        setShowCreateRelease(false);
        setReleaseForm({ version: '', gitRef: '' });
      },
      onError: (err: unknown) => {
        setError(getErrorMessage(err, 'Failed to create release'));
      },
    });
  };

  const handleEditProduct = () => {
    setError(null);
    updateProduct.mutate(
      { id: id!, data: editForm },
      {
        onSuccess: () => setShowEditProduct(false),
        onError: (err: unknown) => {
          setError(getErrorMessage(err, 'Failed to update product'));
        },
      }
    );
  };

  const openEditForm = () => {
    setEditForm({
      name: product.name,
      type: product.type,
      criticality: product.criticality,
      contacts: product.contacts,
    });
    setShowEditProduct(true);
  };

  const isManager = hasRole('ADMIN') || hasRole('COMPLIANCE_MANAGER');

  return (
    <div>
      <button
        onClick={() => navigate('/products')}
        className="text-sm text-primary-600 hover:text-primary-800 mb-4 inline-block"
      >
        &larr; Back to Products
      </button>

      {/* Product Info */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{product.name}</h1>
            <div className="mt-2 flex gap-2">
              <StatusBadge status={product.type} />
              <StatusBadge status={product.criticality} />
              {product.conformityPath && (
                <span className="px-2 py-0.5 text-xs rounded bg-purple-100 text-purple-700 font-medium">
                  {FR.conformityPath[product.conformityPath] || product.conformityPath}
                </span>
              )}
            </div>
          </div>
          <div className="flex items-center gap-4">
            {/* Mini readiness gauge */}
            {readiness && (
              <div className="relative" style={{ width: 64, height: 64 }}>
                <ReadinessGauge score={readiness.overallScore} size={64} />
              </div>
            )}
            {isManager && (
              <button
                onClick={openEditForm}
                className="px-3 py-1.5 text-sm text-primary-600 border border-primary-300 rounded-lg hover:bg-primary-50"
              >
                Edit
              </button>
            )}
          </div>
        </div>

        {product.contacts && product.contacts.length > 0 && (
          <div className="mt-4">
            <h3 className="text-sm font-medium text-gray-500 mb-2">Contacts</h3>
            <div className="flex flex-wrap gap-3">
              {product.contacts.map((c, i) => (
                <div key={i} className="text-sm bg-gray-50 rounded px-3 py-1.5">
                  <span className="font-medium">{c.name}</span>
                  <span className="text-gray-400 mx-1">-</span>
                  <span className="text-gray-500">{c.email}</span>
                  {c.role && <span className="text-gray-400 ml-1">({c.role})</span>}
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="mt-4 grid grid-cols-2 gap-4 text-sm">
          <div>
            <span className="text-gray-500">Created:</span>{' '}
            <span className="text-gray-900">{new Date(product.createdAt).toLocaleString()}</span>
          </div>
          <div>
            <span className="text-gray-500">Updated:</span>{' '}
            <span className="text-gray-900">{new Date(product.updatedAt).toLocaleString()}</span>
          </div>
        </div>
      </div>

      {/* CVD Policy (CRA Annexe I §2(5)) */}
      <div className="mb-6">
        <CvdPolicyCard productId={id!} />
      </div>

      {/* Tabs */}
      <div className="border-b mb-6">
        <nav className="flex gap-6">
          {([
            'releases', 'findings', 'readiness',
            ...(FEATURES.REQUIREMENTS ? ['checklist'] : []),
            'eu-doc', 'conformity', 'risk', 'standards',
          ] as TabType[]).map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`pb-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                activeTab === tab
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              {tab === 'releases' ? 'Releases' :
               tab === 'findings' ? 'Findings' :
               tab === 'readiness' ? FR.readiness.title :
               tab === 'checklist' ? FR.checklist.title :
               tab === 'eu-doc' ? 'EU DoC' :
               tab === 'conformity' ? 'Conformity' :
               tab === 'risk' ? 'Risk Assessment' :
               'Standards'}
            </button>
          ))}
        </nav>
      </div>

      {/* Releases Tab */}
      {activeTab === 'releases' && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Releases</h2>
            {isManager && (
              <button
                onClick={() => setShowCreateRelease(true)}
                className="px-3 py-1.5 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700"
              >
                New Release
              </button>
            )}
          </div>

          {releasesLoading ? (
            <div className="text-gray-500 text-sm">Loading releases...</div>
          ) : !releases || releases.length === 0 ? (
            <div className="text-gray-400 text-sm py-4">
              No releases yet. Create a release to start tracking compliance artifacts.
            </div>
          ) : (
            <div className="space-y-3">
              {releases.map((release: Release) => (
                <div
                  key={release.id}
                  className="flex items-center justify-between border rounded-lg p-4 hover:bg-gray-50 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <Link
                      to={`/releases/${release.id}`}
                      className="font-medium text-primary-600 hover:text-primary-800"
                    >
                      v{release.version}
                    </Link>
                    <StatusBadge status={release.status} />
                    {release.gitRef && (
                      <span className="text-xs text-gray-400 font-mono">{release.gitRef}</span>
                    )}
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-xs text-gray-400">
                      {new Date(release.createdAt).toLocaleDateString()}
                    </span>
                    <button
                      onClick={() => exportPack.mutate(release.id)}
                      disabled={exportPack.isPending}
                      className="px-2 py-1 text-xs text-gray-600 border border-gray-300 rounded hover:bg-gray-100 disabled:opacity-50"
                      title="Export compliance pack"
                    >
                      {exportPack.isPending ? 'Exporting...' : 'Export'}
                    </button>
                    <Link
                      to={`/releases/${release.id}`}
                      className="px-2 py-1 text-xs text-primary-600 border border-primary-300 rounded hover:bg-primary-50"
                    >
                      Details
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Findings Tab */}
      {activeTab === 'findings' && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Findings</h2>
            <select
              value={findingStatusFilter}
              onChange={(e) => setFindingStatusFilter(e.target.value)}
              aria-label="Filtrer les vulnérabilités par statut"
              className="text-sm border border-gray-300 rounded-lg px-3 py-1.5 focus:ring-primary-500 focus:border-primary-500"
            >
              {FINDING_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s || 'All Statuses'}
                </option>
              ))}
            </select>
          </div>

          {!findings || findings.length === 0 ? (
            <div className="text-gray-400 text-sm py-4">
              No findings found{findingStatusFilter ? ` with status ${findingStatusFilter}` : ''}.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Vulnerability</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Component</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Severity</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Detected</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {findings.map((f: Finding) => (
                    <tr key={f.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 text-sm">
                        <div className="font-medium text-gray-900">{f.vulnerabilityId}</div>
                        {f.osvId && <div className="text-xs text-gray-400">{f.osvId}</div>}
                        {f.summary && <div className="text-xs text-gray-500 mt-0.5 max-w-xs truncate">{f.summary}</div>}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {f.componentName || f.componentPurl || '-'}
                      </td>
                      <td className="px-4 py-3">
                        {f.severity ? <StatusBadge status={f.severity} /> : <span className="text-gray-400 text-xs">-</span>}
                      </td>
                      <td className="px-4 py-3">
                        <StatusBadge status={f.status} />
                      </td>
                      <td className="px-4 py-3 text-xs text-gray-500">
                        {new Date(f.detectedAt).toLocaleDateString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Readiness Tab */}
      {activeTab === 'readiness' && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">{FR.readiness.title}</h2>
              <p className="text-sm text-gray-500">{FR.readiness.subtitle}</p>
            </div>
            {isManager && (
              <button
                onClick={() => snapshotReadiness.mutate()}
                disabled={snapshotReadiness.isPending}
                className="px-3 py-1.5 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {snapshotReadiness.isPending ? FR.readiness.snapshotting : FR.readiness.snapshot}
              </button>
            )}
          </div>

          {readiness ? (
            <div>
              <div className="flex items-center gap-8 mb-8">
                <div className="relative" style={{ width: 120, height: 120 }}>
                  <ReadinessGauge score={readiness.overallScore} size={120} label={FR.readiness.overallScore} />
                </div>
                <div className="flex-1 space-y-3">
                  <h3 className="text-sm font-medium text-gray-700 mb-2">{FR.readiness.categories}</h3>
                  {readiness.categories.map(cat => (
                    <div key={cat.name} className="flex items-center gap-3">
                      <span className="text-xs text-gray-600 w-40 truncate">
                        {FR.readinessCategory[cat.name] || cat.label}
                      </span>
                      <div className="flex-1 bg-gray-200 rounded-full h-2">
                        <div
                          className={`h-2 rounded-full transition-all duration-500 ${
                            cat.score >= cat.maxScore * 0.8 ? 'bg-green-500' :
                            cat.score >= cat.maxScore * 0.6 ? 'bg-yellow-500' :
                            cat.score >= cat.maxScore * 0.4 ? 'bg-orange-500' : 'bg-red-500'
                          }`}
                          style={{ width: `${cat.maxScore > 0 ? (cat.score / cat.maxScore) * 100 : 0}%` }}
                        />
                      </div>
                      <span className="text-xs font-medium text-gray-700 w-12 text-right">
                        {cat.score}/{cat.maxScore}
                      </span>
                    </div>
                  ))}
                </div>
              </div>

              {readiness.actionItems.length > 0 && (
                <div>
                  <h3 className="text-sm font-medium text-gray-700 mb-2">{FR.readiness.actionItems}</h3>
                  <ul className="space-y-1.5">
                    {readiness.actionItems.map((item, i) => (
                      <li key={i} className="flex items-start gap-2 text-sm text-gray-600">
                        <span className="text-orange-500 mt-0.5">&#9679;</span>
                        {item}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ) : (
            <div className="text-gray-400 text-sm py-4">
              {FR.checklist.noItems}
            </div>
          )}
        </div>
      )}

      {/* Checklist Tab */}
      {activeTab === 'checklist' && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">{FR.checklist.title}</h2>
              <p className="text-sm text-gray-500">{FR.checklist.subtitle}</p>
            </div>
            {isManager && (!checklistItems || checklistItems.length === 0) && (
              <button
                onClick={() => initChecklist.mutate()}
                disabled={initChecklist.isPending}
                className="px-3 py-1.5 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {initChecklist.isPending ? FR.checklist.initializing : FR.checklist.initialize}
              </button>
            )}
          </div>

          {checklistItems && checklistItems.length > 0 ? (
            <CraChecklistTable
              items={checklistItems}
              onUpdate={(itemId, data) => updateChecklistItem.mutate({ itemId, data })}
              isUpdating={updateChecklistItem.isPending}
            />
          ) : (
            <div className="text-gray-400 text-sm py-4">
              {FR.checklist.noItems}
            </div>
          )}
        </div>
      )}

      {/* EU Declaration of Conformity Tab */}
      {activeTab === 'eu-doc' && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">EU Declaration of Conformity</h2>
              <p className="text-sm text-gray-500">CRA Annexe V — Generate and manage your EU DoC</p>
            </div>
            {isManager && (
              <button
                onClick={() => {
                  const docReq: EuDocRequest = {
                    declarationNumber: `DoC-${product.name}-${Date.now()}`,
                    manufacturerName: product.name,
                    manufacturerAddress: 'To be completed',
                    productName: product.name,
                    productIdentification: `Product ID: ${product.id}`,
                    conformityAssessmentModule: conformityModule,
                    declarationText: `The manufacturer hereby declares that the product "${product.name}" conforms to the essential requirements of the EU Cyber Resilience Act (Regulation (EU) 2024/2847).`,
                    signedBy: 'To be completed',
                    signedRole: 'To be completed',
                  };
                  createEuDoc.mutate(docReq);
                }}
                disabled={createEuDoc.isPending}
                className="px-3 py-1.5 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {createEuDoc.isPending ? 'Creating...' : 'New EU DoC'}
              </button>
            )}
          </div>

          {!euDocs || euDocs.length === 0 ? (
            <div className="text-gray-400 text-sm py-4">No EU Declaration of Conformity yet.</div>
          ) : (
            <div className="space-y-3">
              {euDocs.map(doc => (
                <div key={doc.id} className="border rounded-lg p-4">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="font-medium text-gray-900">{doc.declarationNumber}</h3>
                      <p className="text-sm text-gray-500 mt-1">{doc.manufacturerName} - {doc.conformityAssessmentModule.replace('_', ' ')}</p>
                      <p className="text-xs text-gray-400 mt-1">Signed by: {doc.signedBy} ({doc.signedRole})</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className={`px-2 py-0.5 text-xs rounded-full font-medium ${
                        doc.status === 'PUBLISHED' ? 'bg-green-100 text-green-800' :
                        doc.status === 'SIGNED' ? 'bg-blue-100 text-blue-800' :
                        'bg-gray-100 text-gray-700'
                      }`}>{doc.status}</span>
                      {doc.status === 'DRAFT' && isManager && (
                        <button onClick={() => signEuDoc.mutate(doc.id)} className="px-2 py-1 text-xs text-blue-600 border border-blue-300 rounded hover:bg-blue-50">Sign</button>
                      )}
                      {doc.status === 'SIGNED' && isManager && (
                        <button onClick={() => publishEuDoc.mutate(doc.id)} className="px-2 py-1 text-xs text-green-600 border border-green-300 rounded hover:bg-green-50">Publish</button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Conformity Assessment Tab */}
      {activeTab === 'conformity' && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">Conformity Assessment</h2>
              <p className="text-sm text-gray-500">CRA Art. 32 — {conformityModule.replace('_', ' ')} procedure</p>
            </div>
            {!conformityAssessment && isManager && (
              <button
                onClick={() => initiateAssessment.mutate(conformityModule)}
                disabled={initiateAssessment.isPending}
                className="px-3 py-1.5 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {initiateAssessment.isPending ? 'Initiating...' : 'Start Assessment'}
              </button>
            )}
          </div>

          {!conformityAssessment ? (
            <div className="text-gray-400 text-sm py-4">No conformity assessment started yet.</div>
          ) : (
            <div>
              <div className="flex items-center gap-4 mb-6">
                <span className={`px-2.5 py-0.5 text-xs rounded-full font-medium ${
                  conformityAssessment.status === 'APPROVED' ? 'bg-green-100 text-green-800' :
                  conformityAssessment.status === 'COMPLETED' ? 'bg-blue-100 text-blue-800' :
                  conformityAssessment.status === 'IN_PROGRESS' ? 'bg-yellow-100 text-yellow-800' :
                  'bg-gray-100 text-gray-700'
                }`}>{conformityAssessment.status}</span>
                <span className="text-sm text-gray-500">Step {conformityAssessment.currentStep + 1} of {conformityAssessment.totalSteps}</span>
                <div className="flex-1 bg-gray-200 rounded-full h-2">
                  <div className="bg-primary-500 h-2 rounded-full transition-all" style={{ width: `${(conformityAssessment.currentStep / conformityAssessment.totalSteps) * 100}%` }} />
                </div>
                {conformityAssessment.status === 'COMPLETED' && isManager && (
                  <button onClick={() => approveAssessment.mutate(conformityAssessment.id)} className="px-3 py-1 text-xs text-green-600 border border-green-300 rounded hover:bg-green-50">Approve</button>
                )}
              </div>

              <div className="space-y-2">
                {(() => {
                  try {
                    const steps: ConformityStep[] = JSON.parse(conformityAssessment.stepsData);
                    return steps.map((step, i) => (
                      <div key={i} className={`flex items-center justify-between p-3 rounded-lg border ${step.status === 'COMPLETED' ? 'bg-green-50 border-green-200' : i === conformityAssessment.currentStep ? 'bg-blue-50 border-blue-200' : 'bg-gray-50 border-gray-200'}`}>
                        <div className="flex items-center gap-3">
                          <span className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${step.status === 'COMPLETED' ? 'bg-green-500 text-white' : i === conformityAssessment.currentStep ? 'bg-blue-500 text-white' : 'bg-gray-300 text-gray-600'}`}>
                            {step.status === 'COMPLETED' ? '\u2713' : i + 1}
                          </span>
                          <div>
                            <span className="text-sm font-medium text-gray-900">{step.name}</span>
                            {step.notes && <p className="text-xs text-gray-500 mt-0.5">{step.notes}</p>}
                          </div>
                        </div>
                        {step.status === 'PENDING' && i === conformityAssessment.currentStep && isManager && (
                          <button
                            onClick={() => completeStep.mutate({ assessmentId: conformityAssessment.id, stepIndex: i })}
                            disabled={completeStep.isPending}
                            className="px-2 py-1 text-xs text-primary-600 border border-primary-300 rounded hover:bg-primary-50"
                          >
                            Complete
                          </button>
                        )}
                      </div>
                    ));
                  } catch { return <div className="text-gray-400 text-sm">Unable to parse steps data.</div>; }
                })()}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Risk Assessment Tab */}
      {activeTab === 'risk' && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">Risk Assessment</h2>
              <p className="text-sm text-gray-500">CRA Art. 13(1) — Cybersecurity risk analysis</p>
            </div>
            {isManager && (
              <button
                onClick={() => createRiskAssessment.mutate({ title: `Risk Assessment - ${product.name}`, methodology: 'STRIDE' })}
                disabled={createRiskAssessment.isPending}
                className="px-3 py-1.5 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {createRiskAssessment.isPending ? 'Creating...' : 'New Assessment'}
              </button>
            )}
          </div>

          {!riskAssessments || riskAssessments.length === 0 ? (
            <div className="text-gray-400 text-sm py-4">No risk assessments yet.</div>
          ) : (
            <div className="space-y-4">
              {riskAssessments.map(ra => (
                <div key={ra.id} className="border rounded-lg p-4">
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <h3 className="font-medium text-gray-900">{ra.title}</h3>
                      <div className="flex gap-2 mt-1">
                        <span className="text-xs text-gray-500">{ra.methodology}</span>
                        <span className={`px-2 py-0.5 text-xs rounded-full font-medium ${
                          ra.status === 'APPROVED' ? 'bg-green-100 text-green-800' :
                          ra.status === 'IN_REVIEW' ? 'bg-yellow-100 text-yellow-800' :
                          'bg-gray-100 text-gray-700'
                        }`}>{ra.status}</span>
                        {ra.overallRiskLevel && (
                          <span className={`px-2 py-0.5 text-xs rounded-full font-medium ${
                            ra.overallRiskLevel === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                            ra.overallRiskLevel === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                            ra.overallRiskLevel === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                            'bg-green-100 text-green-800'
                          }`}>Risk: {ra.overallRiskLevel}</span>
                        )}
                      </div>
                    </div>
                  </div>
                  {ra.items && ra.items.length > 0 && (
                    <div className="mt-3 overflow-x-auto">
                      <table className="min-w-full divide-y divide-gray-200 text-sm">
                        <thead className="bg-gray-50">
                          <tr>
                            <th className="px-3 py-2 text-left text-xs font-medium text-gray-500">Threat</th>
                            <th className="px-3 py-2 text-left text-xs font-medium text-gray-500">Asset</th>
                            <th className="px-3 py-2 text-xs font-medium text-gray-500">L</th>
                            <th className="px-3 py-2 text-xs font-medium text-gray-500">I</th>
                            <th className="px-3 py-2 text-xs font-medium text-gray-500">Risk</th>
                            <th className="px-3 py-2 text-xs font-medium text-gray-500">Mitigation</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                          {ra.items.map(item => (
                            <tr key={item.id}>
                              <td className="px-3 py-2"><span className="text-xs font-medium text-gray-600">{item.threatCategory}</span><br/><span className="text-xs text-gray-500">{item.threatDescription.slice(0, 60)}</span></td>
                              <td className="px-3 py-2 text-xs text-gray-500">{item.affectedAsset || '-'}</td>
                              <td className="px-3 py-2 text-xs text-center">{item.likelihood.slice(0, 1)}</td>
                              <td className="px-3 py-2 text-xs text-center">{item.impact.slice(0, 1)}</td>
                              <td className="px-3 py-2 text-center"><span className={`px-1.5 py-0.5 text-xs rounded ${
                                item.riskLevel === 'CRITICAL' ? 'bg-red-100 text-red-800' :
                                item.riskLevel === 'HIGH' ? 'bg-orange-100 text-orange-800' :
                                item.riskLevel === 'MEDIUM' ? 'bg-yellow-100 text-yellow-800' :
                                'bg-green-100 text-green-800'
                              }`}>{item.riskLevel}</span></td>
                              <td className="px-3 py-2 text-xs text-gray-500">{item.mitigationStatus}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Applied Standards Tab */}
      {activeTab === 'standards' && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">Applied Standards</h2>
              <p className="text-sm text-gray-500">CRA Art. 27 — Harmonised standards presumption of conformity</p>
            </div>
            {isManager && (
              <button
                onClick={() => {
                  const req: AppliedStandardRequest = {
                    standardCode: 'EN 303 645',
                    standardTitle: 'Cyber Security for Consumer IoT: Baseline Requirements',
                    version: '2.1.1',
                    complianceStatus: 'CLAIMED',
                  };
                  createStandard.mutate(req);
                }}
                disabled={createStandard.isPending}
                className="px-3 py-1.5 text-sm bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {createStandard.isPending ? 'Adding...' : 'Add Standard'}
              </button>
            )}
          </div>

          {!appliedStandards || appliedStandards.length === 0 ? (
            <div className="text-gray-400 text-sm py-4">No harmonised standards applied yet.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Standard</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Title</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Version</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {appliedStandards.map(std => (
                    <tr key={std.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 text-sm font-medium text-gray-900">{std.standardCode}</td>
                      <td className="px-4 py-3 text-sm text-gray-600">{std.standardTitle}</td>
                      <td className="px-4 py-3 text-sm text-gray-500">{std.version || '-'}</td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-0.5 text-xs rounded-full font-medium ${
                          std.complianceStatus === 'FULL' ? 'bg-green-100 text-green-800' :
                          std.complianceStatus === 'PARTIAL' ? 'bg-yellow-100 text-yellow-800' :
                          std.complianceStatus === 'CLAIMED' ? 'bg-blue-100 text-blue-800' :
                          'bg-gray-100 text-gray-700'
                        }`}>{std.complianceStatus}</span>
                      </td>
                      <td className="px-4 py-3">
                        {isManager && (
                          <button onClick={() => deleteStandard.mutate(std.id)} className="text-xs text-red-500 hover:text-red-700">Remove</button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Create Release Modal */}
      {showCreateRelease && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50" role="dialog" aria-modal="true" aria-labelledby="create-release-title">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <h2 id="create-release-title" className="text-lg font-semibold mb-4">New Release</h2>

            {error && (
              <div id="release-error-msg" role="alert" className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
                {error}
              </div>
            )}

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Version *</label>
                <input
                  type="text"
                  value={releaseForm.version}
                  onChange={(e) => setReleaseForm({ ...releaseForm, version: e.target.value })}
                  placeholder="e.g. 1.0.0"
                  aria-label="Version de la release"
                  aria-required="true"
                  {...(error ? { 'aria-invalid': true, 'aria-describedby': 'release-error-msg' } : {})}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Git Ref</label>
                <input
                  type="text"
                  value={releaseForm.gitRef || ''}
                  onChange={(e) => setReleaseForm({ ...releaseForm, gitRef: e.target.value })}
                  placeholder="e.g. abc1234 or v1.0.0"
                  aria-label="Référence Git"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm font-mono focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Build ID</label>
                <input
                  type="text"
                  value={releaseForm.buildId || ''}
                  onChange={(e) => setReleaseForm({ ...releaseForm, buildId: e.target.value })}
                  placeholder="e.g. build-42"
                  aria-label="Identifiant de build"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => {
                  setShowCreateRelease(false);
                  setError(null);
                }}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
              >
                Cancel
              </button>
              <button
                onClick={handleCreateRelease}
                disabled={!releaseForm.version || createRelease.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {createRelease.isPending ? 'Creating...' : 'Create'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Product Modal */}
      {showEditProduct && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50" role="dialog" aria-modal="true" aria-labelledby="edit-product-title">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <h2 id="edit-product-title" className="text-lg font-semibold mb-4">Edit Product</h2>

            {error && (
              <div id="edit-product-error-msg" role="alert" className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
                {error}
              </div>
            )}

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Name *</label>
                <input
                  type="text"
                  value={editForm.name}
                  onChange={(e) => setEditForm({ ...editForm, name: e.target.value })}
                  aria-label="Nom du produit"
                  aria-required="true"
                  {...(error ? { 'aria-invalid': true, 'aria-describedby': 'edit-product-error-msg' } : {})}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Type</label>
                <select
                  value={editForm.type}
                  onChange={(e) => setEditForm({ ...editForm, type: e.target.value })}
                  aria-label="Type de produit"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                >
                  {PRODUCT_TYPES.map((t) => (
                    <option key={t} value={t}>
                      {t.replace(/_/g, ' ')}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Criticality</label>
                <select
                  value={editForm.criticality}
                  onChange={(e) => setEditForm({ ...editForm, criticality: e.target.value })}
                  aria-label="Niveau de criticité"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                >
                  {CRITICALITY_LEVELS.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => {
                  setShowEditProduct(false);
                  setError(null);
                }}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
              >
                Cancel
              </button>
              <button
                onClick={handleEditProduct}
                disabled={!editForm.name || updateProduct.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {updateProduct.isPending ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
