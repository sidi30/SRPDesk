import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useProducts, useCreateProduct, useDeleteProduct } from '../hooks/useProducts';
import { DataTable } from '../components/DataTable';
import { StatusBadge } from '../components/StatusBadge';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { useAuth } from '../auth/AuthProvider';
import type { Product, ProductCreateRequest } from '../types';
import { getErrorMessage } from '../types';

const PRODUCT_TYPES = ['DEFAULT', 'CLASS_I', 'CLASS_II', 'IMPORTANT_CLASS_I', 'IMPORTANT_CLASS_II', 'CRITICAL'] as const;
const CRITICALITY_LEVELS = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as const;

export function ProductsPage() {
  const navigate = useNavigate();
  const { hasRole } = useAuth();
  const { data: products, isLoading } = useProducts();
  const createProduct = useCreateProduct();
  const deleteProduct = useDeleteProduct();

  const [showCreate, setShowCreate] = useState(false);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [form, setForm] = useState<ProductCreateRequest>({
    name: '',
    type: 'DEFAULT',
    criticality: 'MEDIUM',
  });
  const [error, setError] = useState<string | null>(null);

  const handleCreate = () => {
    setError(null);
    createProduct.mutate(form, {
      onSuccess: () => {
        setShowCreate(false);
        setForm({ name: '', type: 'DEFAULT', criticality: 'MEDIUM' });
      },
      onError: (err: unknown) => {
        setError(getErrorMessage(err, 'Failed to create product'));
      },
    });
  };

  if (isLoading) return <div className="text-gray-500">Loading products...</div>;

  const columns = [
    {
      header: 'Name',
      accessor: (p: Product) => <span className="font-medium text-gray-900">{p.name}</span>,
    },
    {
      header: 'Type',
      accessor: (p: Product) => <StatusBadge status={p.type} />,
    },
    {
      header: 'Criticality',
      accessor: (p: Product) => <StatusBadge status={p.criticality} />,
    },
    {
      header: 'Contacts',
      accessor: (p: Product) => (
        <span className="text-gray-500">{p.contacts?.length || 0}</span>
      ),
    },
    {
      header: 'Created',
      accessor: (p: Product) => (
        <span className="text-gray-500">{new Date(p.createdAt).toLocaleDateString()}</span>
      ),
    },
    ...(hasRole('ADMIN') || hasRole('COMPLIANCE_MANAGER')
      ? [
          {
            header: 'Actions',
            accessor: (p: Product) => (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setDeleteId(p.id);
                }}
                className="text-red-600 hover:text-red-800 text-sm"
              >
                Delete
              </button>
            ),
          },
        ]
      : []),
  ];

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Products</h1>
          <p className="mt-1 text-sm text-gray-500">Manage your digital products for CRA compliance</p>
        </div>
        {(hasRole('ADMIN') || hasRole('COMPLIANCE_MANAGER')) && (
          <button
            onClick={() => setShowCreate(true)}
            className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 text-sm font-medium"
          >
            Add Product
          </button>
        )}
      </div>

      <DataTable
        columns={columns}
        data={products || []}
        onRowClick={(p) => navigate(`/products/${p.id}`)}
        emptyMessage="No products yet. Add your first digital product to begin CRA compliance tracking."
      />

      {/* Create Product Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50" role="dialog" aria-modal="true" aria-labelledby="create-product-title">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <h2 id="create-product-title" className="text-lg font-semibold mb-4">New Product</h2>

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
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  placeholder="e.g. IoT Gateway v2"
                  aria-label="Nom du produit"
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:ring-primary-500 focus:border-primary-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Type</label>
                <select
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value })}
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
                  value={form.criticality}
                  onChange={(e) => setForm({ ...form, criticality: e.target.value })}
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
                  setShowCreate(false);
                  setError(null);
                }}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
              >
                Cancel
              </button>
              <button
                onClick={handleCreate}
                disabled={!form.name || createProduct.isPending}
                className="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 disabled:opacity-50"
              >
                {createProduct.isPending ? 'Creating...' : 'Create'}
              </button>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog
        isOpen={!!deleteId}
        onClose={() => setDeleteId(null)}
        onConfirm={() => deleteId && deleteProduct.mutate(deleteId)}
        title="Delete Product"
        message="Are you sure you want to delete this product? This action cannot be undone. All releases, evidences, and findings associated with this product will also be removed."
        confirmLabel="Delete"
        variant="danger"
      />
    </div>
  );
}
