import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useProduct, useUpdateProduct } from '../hooks/useProducts';
import { useReleases, useCreateRelease, useExportPack } from '../hooks/useReleases';
import { useProductFindings } from '../hooks/useFindings';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../auth/AuthProvider';
import type { ReleaseCreateRequest, ProductUpdateRequest, Release, Finding } from '../types';
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

  const createRelease = useCreateRelease(id!);
  const updateProduct = useUpdateProduct();
  const exportPack = useExportPack();

  const [showCreateRelease, setShowCreateRelease] = useState(false);
  const [showEditProduct, setShowEditProduct] = useState(false);
  const [releaseForm, setReleaseForm] = useState<ReleaseCreateRequest>({ version: '', gitRef: '' });
  const [editForm, setEditForm] = useState<ProductUpdateRequest>({ name: '', type: '', criticality: '' });
  const [error, setError] = useState<string | null>(null);

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
            </div>
          </div>
          {(hasRole('ADMIN') || hasRole('COMPLIANCE_MANAGER')) && (
            <button
              onClick={openEditForm}
              className="px-3 py-1.5 text-sm text-primary-600 border border-primary-300 rounded-lg hover:bg-primary-50"
            >
              Edit
            </button>
          )}
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

      {/* Releases Section */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-semibold text-gray-900">Releases</h2>
          {(hasRole('ADMIN') || hasRole('COMPLIANCE_MANAGER')) && (
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

      {/* Findings Section */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-semibold text-gray-900">Findings</h2>
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

      {/* Create Release Modal */}
      {showCreateRelease && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <h2 className="text-lg font-semibold mb-4">New Release</h2>

            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
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
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <h2 className="text-lg font-semibold mb-4">Edit Product</h2>

            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
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
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Type</label>
                <select
                  value={editForm.type}
                  onChange={(e) => setEditForm({ ...editForm, type: e.target.value })}
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
