import { useProducts } from '../hooks/useProducts';
import { useAuditVerify } from '../hooks/useAuditEvents';
import { Link } from 'react-router-dom';
import { StatusBadge } from '../components/StatusBadge';

export function DashboardPage() {
  const { data: products, isLoading } = useProducts();
  const { data: auditResult } = useAuditVerify();

  if (isLoading) return <div className="text-gray-500">Loading...</div>;

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="mt-1 text-sm text-gray-500">CRA Compliance Overview</p>
      </div>

      {/* Audit Status */}
      {auditResult && (
        <Link
          to="/audit"
          className={`block mb-6 p-4 rounded-lg border hover:shadow-sm transition-shadow ${auditResult.valid ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <div className={`w-3 h-3 rounded-full mr-3 ${auditResult.valid ? 'bg-green-500' : 'bg-red-500'}`} />
              <div>
                <p className={`text-sm font-medium ${auditResult.valid ? 'text-green-800' : 'text-red-800'}`}>
                  Audit Trail: {auditResult.valid ? 'Verified' : 'Integrity Issue Detected'}
                </p>
                <p className="text-xs text-gray-500">{auditResult.totalEvents} events verified</p>
              </div>
            </div>
            <span className="text-sm text-gray-500">View details &rarr;</span>
          </div>
        </Link>
      )}

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">Products</p>
          <p className="text-2xl font-bold">{products?.length || 0}</p>
        </div>
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">Types</p>
          <p className="text-2xl font-bold">{new Set(products?.map(p => p.type)).size || 0}</p>
        </div>
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">Critical Products</p>
          <p className="text-2xl font-bold text-red-600">
            {products?.filter(p => p.criticality === 'HIGH' || p.criticality === 'CRITICAL').length || 0}
          </p>
        </div>
      </div>

      {/* Products Grid */}
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Products</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {products?.map(product => (
          <Link
            key={product.id}
            to={`/products/${product.id}`}
            className="block bg-white rounded-lg border p-4 hover:shadow-md transition-shadow"
          >
            <h3 className="font-medium text-gray-900">{product.name}</h3>
            <div className="mt-2 flex gap-2">
              <StatusBadge status={product.type} />
              <StatusBadge status={product.criticality} />
            </div>
            <p className="mt-2 text-xs text-gray-400">
              Created {new Date(product.createdAt).toLocaleDateString()}
            </p>
          </Link>
        ))}
        {(!products || products.length === 0) && (
          <p className="text-gray-400 col-span-3">No products yet. Create one to get started.</p>
        )}
      </div>
    </div>
  );
}
