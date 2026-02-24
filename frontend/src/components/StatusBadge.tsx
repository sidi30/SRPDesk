import { memo } from 'react';

const statusColors: Record<string, string> = {
  // Release statuses
  DRAFT: 'bg-gray-100 text-gray-800',
  RELEASED: 'bg-green-100 text-green-800',
  DEPRECATED: 'bg-yellow-100 text-yellow-800',
  REVOKED: 'bg-red-100 text-red-800',
  // Finding statuses
  OPEN: 'bg-red-100 text-red-800',
  NOT_AFFECTED: 'bg-gray-100 text-gray-800',
  PATCH_PLANNED: 'bg-yellow-100 text-yellow-800',
  MITIGATED: 'bg-blue-100 text-blue-800',
  FIXED: 'bg-green-100 text-green-800',
  // Severity
  CRITICAL: 'bg-red-100 text-red-800',
  HIGH: 'bg-orange-100 text-orange-800',
  MEDIUM: 'bg-yellow-100 text-yellow-800',
  LOW: 'bg-blue-100 text-blue-800',
  UNKNOWN: 'bg-gray-100 text-gray-800',
  // Audit actions
  CREATE: 'bg-green-100 text-green-800',
  UPDATE: 'bg-blue-100 text-blue-800',
  DELETE: 'bg-red-100 text-red-800',
  // Product types
  DEFAULT: 'bg-blue-100 text-blue-800',
  CLASS_I: 'bg-indigo-100 text-indigo-800',
  CLASS_II: 'bg-purple-100 text-purple-800',
  IMPORTANT_CLASS_I: 'bg-orange-100 text-orange-800',
  IMPORTANT_CLASS_II: 'bg-red-100 text-red-800',
  // CRA Event statuses
  IN_REVIEW: 'bg-yellow-100 text-yellow-800',
  SUBMITTED: 'bg-blue-100 text-blue-800',
  CLOSED: 'bg-gray-100 text-gray-800',
  // CRA Event types
  EXPLOITED_VULNERABILITY: 'bg-red-100 text-red-800',
  SEVERE_INCIDENT: 'bg-orange-100 text-orange-800',
  // SRP Submission statuses
  READY: 'bg-green-100 text-green-800',
  EXPORTED: 'bg-indigo-100 text-indigo-800',
  // SRP Submission types
  EARLY_WARNING: 'bg-orange-100 text-orange-800',
  NOTIFICATION: 'bg-yellow-100 text-yellow-800',
  FINAL_REPORT: 'bg-blue-100 text-blue-800',
};

interface StatusBadgeProps {
  status: string;
  label?: string;
  className?: string;
}

export const StatusBadge = memo(function StatusBadge({ status, label, className = '' }: StatusBadgeProps) {
  const colors = statusColors[status] || 'bg-gray-100 text-gray-800';
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${colors} ${className}`}>
      {label ?? status.replace(/_/g, ' ')}
    </span>
  );
});
