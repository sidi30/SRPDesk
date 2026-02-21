import { useState } from 'react';
import { useAuditVerify, useAuditEvents } from '../hooks/useAuditEvents';
import { StatusBadge } from '../components/StatusBadge';
import { DataTable } from '../components/DataTable';
import type { AuditEvent } from '../types';

const ENTITY_TYPES = ['', 'PRODUCT', 'RELEASE', 'EVIDENCE', 'FINDING_DECISION', 'ORGANIZATION', 'ORG_MEMBER'] as const;

export function AuditTrailPage() {
  const [entityTypeFilter, setEntityTypeFilter] = useState<string>('');
  const { data: auditResult } = useAuditVerify();
  const { data: events, isLoading } = useAuditEvents(entityTypeFilter || undefined);

  const columns = [
    {
      header: 'Date',
      accessor: (e: AuditEvent) => (
        <span className="text-gray-600">{new Date(e.createdAt).toLocaleString()}</span>
      ),
    },
    {
      header: 'Entity Type',
      accessor: (e: AuditEvent) => (
        <span className="text-gray-700 font-medium">{e.entityType}</span>
      ),
    },
    {
      header: 'Entity ID',
      accessor: (e: AuditEvent) => (
        <span className="text-gray-500 font-mono text-xs" title={e.entityId}>
          {e.entityId.slice(0, 8)}...
        </span>
      ),
    },
    {
      header: 'Action',
      accessor: (e: AuditEvent) => <StatusBadge status={e.action} />,
    },
    {
      header: 'Actor',
      accessor: (e: AuditEvent) => (
        <span className="text-gray-600 text-xs" title={e.actor}>
          {e.actor.length > 20 ? e.actor.slice(0, 20) + '...' : e.actor}
        </span>
      ),
    },
    {
      header: 'Hash',
      accessor: (e: AuditEvent) => (
        <span className="text-gray-400 font-mono text-xs" title={e.hash}>
          {e.hash.slice(0, 12)}...
        </span>
      ),
    },
  ];

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Audit Trail</h1>
        <p className="mt-1 text-sm text-gray-500">Tamper-evident log of all compliance activities</p>
      </div>

      {/* Verification Banner */}
      {auditResult && (
        <div className={`mb-6 p-4 rounded-lg border ${auditResult.valid ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
          <div className="flex items-center">
            <div className={`w-3 h-3 rounded-full mr-3 ${auditResult.valid ? 'bg-green-500' : 'bg-red-500'}`} />
            <div>
              <p className={`text-sm font-medium ${auditResult.valid ? 'text-green-800' : 'text-red-800'}`}>
                {auditResult.valid ? 'Chain Integrity Verified' : 'Integrity Issue Detected'}
              </p>
              <p className="text-xs text-gray-500">
                {auditResult.verifiedEvents} / {auditResult.totalEvents} events verified &mdash; {auditResult.message}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Filter */}
      <div className="mb-4">
        <select
          value={entityTypeFilter}
          onChange={(e) => setEntityTypeFilter(e.target.value)}
          className="text-sm border border-gray-300 rounded-lg px-3 py-2 focus:ring-primary-500 focus:border-primary-500"
        >
          {ENTITY_TYPES.map((t) => (
            <option key={t} value={t}>
              {t || 'All Entity Types'}
            </option>
          ))}
        </select>
      </div>

      {/* Events Table */}
      {isLoading ? (
        <div className="text-gray-500 text-sm">Loading events...</div>
      ) : (
        <DataTable
          columns={columns}
          data={events || []}
          emptyMessage="No audit events found."
        />
      )}
    </div>
  );
}
