import { useState, useMemo } from 'react';
import { useDashboard } from '../hooks/useDashboard';
import { useAuditVerify } from '../hooks/useAuditEvents';
import { Link } from 'react-router-dom';
import { StatusBadge } from '../components/StatusBadge';
import { SbomFreshnessBadge } from '../components/SbomFreshnessBadge';
import { AlertBanner } from '../components/AlertBanner';
import { ConformityBadge } from '../components/ConformityBadge';
import { RiskBadge } from '../components/RiskBadge';
import { EuDocBadge } from '../components/EuDocBadge';
import { AutomationGauge } from '../components/AutomationGauge';
import { FR } from '../i18n/fr';
import type { ProductReadiness } from '../types';

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

type SortOption = 'score' | 'alerts' | 'name';
type FilterOption = 'all' | 'withAlerts' | 'ciFailing' | 'incomplete';

export function DashboardPage() {
  const { data: dashboard, isLoading } = useDashboard();
  const { data: auditResult } = useAuditVerify();
  const [sortBy, setSortBy] = useState<SortOption>('score');
  const [filter, setFilter] = useState<FilterOption>('all');

  const alertsByProduct = useMemo(() => {
    const map = new Map<string, number>();
    dashboard?.alerts?.forEach(a => {
      map.set(a.productId, (map.get(a.productId) || 0) + 1);
    });
    return map;
  }, [dashboard?.alerts]);

  const filteredAndSorted = useMemo(() => {
    if (!dashboard?.productReadiness) return [];
    let items = [...dashboard.productReadiness];

    // Filter
    if (filter === 'withAlerts') {
      items = items.filter(pr => (alertsByProduct.get(pr.productId) || 0) > 0);
    } else if (filter === 'ciFailing') {
      items = items.filter(pr => pr.lastPolicyResult === 'FAIL');
    } else if (filter === 'incomplete') {
      items = items.filter(pr => pr.conformityStatus !== 'APPROVED' || pr.riskStatus !== 'APPROVED');
    }

    // Sort
    if (sortBy === 'score') {
      items.sort((a, b) => b.readinessScore - a.readinessScore);
    } else if (sortBy === 'alerts') {
      items.sort((a, b) => (alertsByProduct.get(b.productId) || 0) - (alertsByProduct.get(a.productId) || 0));
    } else if (sortBy === 'name') {
      items.sort((a, b) => a.productName.localeCompare(b.productName));
    }

    return items;
  }, [dashboard?.productReadiness, sortBy, filter, alertsByProduct]);

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

      {/* Alerts Banner */}
      <AlertBanner alerts={dashboard?.alerts || []} />

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

      {/* Control Center Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
        <div className="bg-white rounded-lg border p-4 flex items-center gap-4">
          <AutomationGauge score={dashboard?.automationScore || 0} />
          <div>
            <p className="text-sm text-gray-500">{FR.dashboard.automationScore}</p>
            <p className="text-xs text-gray-400">SBOM + CI actifs</p>
          </div>
        </div>
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">{FR.dashboard.productsCompliant}</p>
          <div className="flex items-baseline gap-1 mt-1">
            <p className="text-2xl font-bold text-green-600">{dashboard?.productsFullyCompliant || 0}</p>
            <span className="text-sm text-gray-400">/ {dashboard?.totalProducts || 0}</span>
          </div>
          {(dashboard?.totalProducts || 0) > 0 && (
            <div className="mt-2 bg-gray-200 rounded-full h-2">
              <div
                className="h-2 rounded-full bg-green-500 transition-all duration-500"
                style={{ width: `${Math.round(((dashboard?.productsFullyCompliant || 0) / (dashboard?.totalProducts || 1)) * 100)}%` }}
              />
            </div>
          )}
        </div>
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">{FR.dashboard.euDocIssued}</p>
          <div className="flex items-baseline gap-1 mt-1">
            <p className="text-2xl font-bold text-blue-600">{dashboard?.productsWithEuDoc || 0}</p>
            <span className="text-sm text-gray-400">/ {dashboard?.totalProducts || 0}</span>
          </div>
        </div>
        <div className="bg-white rounded-lg border p-4">
          <p className="text-sm text-gray-500">{FR.dashboard.activeVulns}</p>
          <p className="text-2xl font-bold text-orange-600">{dashboard?.totalVulnerabilities || 0}</p>
          {(dashboard?.criticalHighFindings || 0) > 0 && (
            <span className="inline-flex mt-1 px-2 py-0.5 text-xs font-medium rounded-full bg-red-100 text-red-800">
              {dashboard?.criticalHighFindings} critiques/hautes
            </span>
          )}
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

      {/* Product Readiness Header with Sort/Filter */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
        <h2 className="text-lg font-semibold text-gray-900">{FR.dashboard.productReadiness}</h2>
        <div className="flex flex-wrap gap-2 items-center">
          {/* Filter buttons */}
          <div className="flex gap-1">
            {([
              ['all', FR.dashboard.filterAll],
              ['withAlerts', FR.dashboard.filterWithAlerts],
              ['ciFailing', FR.dashboard.filterCiFailing],
              ['incomplete', FR.dashboard.filterIncomplete],
            ] as [FilterOption, string][]).map(([key, label]) => (
              <button
                key={key}
                onClick={() => setFilter(key)}
                className={`text-xs px-3 py-1 rounded-full border transition-colors ${
                  filter === key
                    ? 'bg-gray-900 text-white border-gray-900'
                    : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'
                }`}
              >
                {label}
              </button>
            ))}
          </div>
          {/* Sort dropdown */}
          <select
            value={sortBy}
            onChange={e => setSortBy(e.target.value as SortOption)}
            className="text-xs border border-gray-300 rounded-md px-2 py-1 bg-white text-gray-600"
          >
            <option value="score">{FR.dashboard.sortByScore}</option>
            <option value="alerts">{FR.dashboard.sortByAlerts}</option>
            <option value="name">{FR.dashboard.sortByName}</option>
          </select>
        </div>
      </div>

      {/* Product Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredAndSorted.map(pr => (
          <ProductCard key={pr.productId} pr={pr} alertCount={alertsByProduct.get(pr.productId) || 0} />
        ))}
        {filteredAndSorted.length === 0 && (
          <p className="text-gray-400 col-span-3">{FR.dashboard.noProducts}</p>
        )}
      </div>
    </div>
  );
}

function ProductCard({ pr, alertCount }: { pr: ProductReadiness; alertCount: number }) {
  return (
    <Link
      to={`/products/${pr.productId}`}
      className="block bg-white rounded-lg border p-4 hover:shadow-md transition-shadow"
    >
      {/* Header */}
      <div className="flex justify-between items-start mb-3">
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <h3 className="font-medium text-gray-900 truncate">{pr.productName}</h3>
            {alertCount > 0 && (
              <span className="inline-flex items-center justify-center w-5 h-5 text-xs font-bold rounded-full bg-red-500 text-white">
                {alertCount}
              </span>
            )}
          </div>
          {/* Badges row 1: type + conformity path */}
          <div className="flex gap-1.5 mt-1.5 flex-wrap">
            <StatusBadge status={pr.type} />
            {pr.conformityPath && (
              <span className="text-xs px-2 py-0.5 rounded bg-purple-100 text-purple-700">
                {FR.conformityPath[pr.conformityPath] || pr.conformityPath}
              </span>
            )}
          </div>
          {/* Badges row 2: CI, SBOM, conformity, risk, EU DoC */}
          <div className="flex gap-1.5 mt-1.5 flex-wrap">
            <SbomFreshnessBadge freshness={pr.sbomFreshness} />
            {pr.lastPolicyResult && (
              <span className={`inline-flex px-2 py-0.5 text-xs font-medium rounded-full ${
                pr.lastPolicyResult === 'PASS' ? 'bg-green-100 text-green-800' :
                pr.lastPolicyResult === 'WARN' ? 'bg-yellow-100 text-yellow-800' :
                'bg-red-100 text-red-800'
              }`}>
                CI: {pr.lastPolicyResult}
              </span>
            )}
            <ConformityBadge status={pr.conformityStatus} />
            <RiskBadge level={pr.riskLevel} />
            <EuDocBadge status={pr.euDocStatus} />
          </div>
        </div>
        <div className="text-right ml-2">
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

      {/* Findings info */}
      {(pr.openFindingsCount > 0 || pr.criticalFindingsCount > 0) && (
        <div className="flex gap-3 mb-2 text-xs">
          <span className="text-orange-600">{pr.openFindingsCount} {FR.dashboard.openCount}</span>
          {pr.criticalFindingsCount > 0 && (
            <span className="text-red-600 font-medium">{pr.criticalFindingsCount} {FR.dashboard.criticalCount}</span>
          )}
        </div>
      )}

      {/* Score bars */}
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

      {/* Conformity progress bar */}
      {pr.conformityStatus && pr.conformityStatus !== 'NOT_STARTED' && (
        <div className="mt-2">
          <ScoreBar
            score={pr.conformityProgress}
            max={100}
            label="Conformit&#233;"
          />
        </div>
      )}

      {/* Footer: version + support */}
      <div className="mt-3 pt-2 border-t border-gray-100 flex justify-between text-xs text-gray-500">
        <span>
          {pr.latestVersion
            ? `${FR.dashboard.version} ${pr.latestVersion}`
            : FR.dashboard.noRelease}
          {pr.releaseCount > 0 && ` (${pr.releaseCount} releases)`}
        </span>
        {pr.supportedUntil && (
          <span>
            {FR.dashboard.supportUntil} {new Date(pr.supportedUntil).toLocaleDateString('fr-FR')}
          </span>
        )}
      </div>
    </Link>
  );
}
