import { useDashboard } from '../hooks/useDashboard';
import { useAuditVerify } from '../hooks/useAuditEvents';
import { Link } from 'react-router-dom';
import { StatusBadge } from '../components/StatusBadge';
import { FR } from '../i18n/fr';

function ScoreBar({ score, max, label }: { score: number; max: number; label: string }) {
  const pct = max > 0 ? Math.round((score / max) * 100) : 0;
  const color = pct >= 80 ? 'bg-green-500' : pct >= 60 ? 'bg-yellow-500' : pct >= 40 ? 'bg-orange-500' : 'bg-red-500';
  return (
    <div className="flex items-center gap-3">
      <span className="text-xs text-gray-600 w-24 truncate" title={label}>{label}</span>
      <div className="flex-1 bg-gray-200 rounded-full h-2">
        <div className={`h-2 rounded-full ${color} transition-all duration-500`} style={{ width: `${pct}%` }} />
      </div>
      <span className="text-xs font-medium text-gray-700 w-8 text-right">{score}%</span>
    </div>
  );
}

export function DashboardPage() {
  const { data: dashboard, isLoading } = useDashboard();
  const { data: auditResult } = useAuditVerify();

  if (isLoading) return <div className="text-gray-500">{FR.findingsPage.loading}</div>;

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">{FR.dashboard.title}</h1>
        <p className="mt-1 text-sm text-gray-500">{FR.dashboard.subtitle}</p>
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

      {/* Stats Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">{FR.dashboard.products}</p>
          <p className="text-2xl font-bold">{dashboard?.totalProducts || 0}</p>
        </div>
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">{FR.dashboard.releases}</p>
          <p className="text-2xl font-bold">{dashboard?.totalReleases || 0}</p>
        </div>
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">{FR.dashboard.openFindings}</p>
          <p className="text-2xl font-bold text-orange-600">{dashboard?.openFindings || 0}</p>
          <p className="text-xs text-gray-400">/ {dashboard?.totalFindings || 0} total</p>
        </div>
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">{FR.dashboard.criticalHigh}</p>
          <p className="text-2xl font-bold text-red-600">{dashboard?.criticalHighFindings || 0}</p>
        </div>
      </div>

      {/* CRA Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">{FR.dashboard.craEvents}</p>
          <p className="text-2xl font-bold">{dashboard?.totalCraEvents || 0}</p>
          <p className="text-xs text-gray-400">{dashboard?.activeCraEvents || 0} {FR.dashboard.activeEvents}</p>
        </div>
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">{FR.dashboard.avgReadiness}</p>
          <div className="flex items-baseline gap-1">
            <p className="text-2xl font-bold" style={{
              color: (dashboard?.averageReadinessScore || 0) >= 80 ? '#22c55e' :
                (dashboard?.averageReadinessScore || 0) >= 60 ? '#eab308' :
                (dashboard?.averageReadinessScore || 0) >= 40 ? '#f97316' : '#ef4444'
            }}>
              {dashboard?.averageReadinessScore?.toFixed(1) || '0.0'}
            </p>
            <span className="text-sm text-gray-400">/100</span>
          </div>
        </div>
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">{FR.dashboard.findings}</p>
          <p className="text-2xl font-bold">{dashboard?.totalFindings || 0}</p>
        </div>
      </div>

      {/* Product Readiness */}
      <h2 className="text-lg font-semibold text-gray-900 mb-4">{FR.dashboard.productReadiness}</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {dashboard?.productReadiness?.map(pr => (
          <Link
            key={pr.productId}
            to={`/products/${pr.productId}`}
            className="block bg-white rounded-lg border p-4 hover:shadow-md transition-shadow"
          >
            <div className="flex justify-between items-start mb-3">
              <div>
                <h3 className="font-medium text-gray-900">{pr.productName}</h3>
                <div className="flex gap-2 mt-1">
                  <StatusBadge status={pr.type} />
                  {pr.conformityPath && (
                    <span className="text-xs px-2 py-0.5 rounded bg-purple-100 text-purple-700">
                      {FR.conformityPath[pr.conformityPath] || pr.conformityPath}
                    </span>
                  )}
                </div>
              </div>
              <div className="text-right">
                <span className="text-lg font-bold" style={{
                  color: pr.readinessScore >= 80 ? '#22c55e' :
                    pr.readinessScore >= 60 ? '#eab308' :
                    pr.readinessScore >= 40 ? '#f97316' : '#ef4444'
                }}>
                  {pr.readinessScore}
                </span>
                <span className="text-xs text-gray-400">/100</span>
              </div>
            </div>

            <ScoreBar score={pr.readinessScore} max={100} label={FR.dashboard.readiness} />

            {pr.checklistTotal > 0 && (
              <div className="mt-2">
                <ScoreBar
                  score={Math.round((pr.checklistCompliant / pr.checklistTotal) * 100)}
                  max={100}
                  label={`${FR.dashboard.checklist} (${pr.checklistCompliant}/${pr.checklistTotal})`}
                />
              </div>
            )}
          </Link>
        ))}
        {(!dashboard?.productReadiness || dashboard.productReadiness.length === 0) && (
          <p className="text-gray-400 col-span-3">{FR.dashboard.noProducts}</p>
        )}
      </div>
    </div>
  );
}
