// Shared constants extracted from multiple components/pages
// to avoid duplication and ensure consistency.

// --- Finding-related constants ---

/** Filter options for finding status dropdowns (includes empty string for "all") */
export const FINDING_STATUSES = ['', 'OPEN', 'NOT_AFFECTED', 'PATCH_PLANNED', 'MITIGATED', 'FIXED'] as const;

/** Decision types for VEX/finding triage */
export const DECISION_TYPES = ['NOT_AFFECTED', 'PATCH_PLANNED', 'MITIGATED', 'FIXED'] as const;

/** Severity indicator colors (bg-only, used for dots and sidebar bars) */
export const SEVERITY_COLORS: Record<string, string> = {
  CRITICAL: 'bg-red-600',
  HIGH: 'bg-orange-500',
  MEDIUM: 'bg-yellow-500',
  LOW: 'bg-blue-500',
  UNKNOWN: 'bg-gray-400',
};

/** Severity badge colors (bg + text, used for advisory severity badges) */
export const SEVERITY_BADGE_COLORS: Record<string, string> = {
  CRITICAL: 'bg-red-100 text-red-800',
  HIGH: 'bg-orange-100 text-orange-800',
  MEDIUM: 'bg-yellow-100 text-yellow-800',
  LOW: 'bg-blue-100 text-blue-800',
};

// --- Product-related constants ---

/** CRA product classification types */
export const PRODUCT_TYPES = ['DEFAULT', 'CLASS_I', 'CLASS_II', 'IMPORTANT_CLASS_I', 'IMPORTANT_CLASS_II', 'CRITICAL'] as const;

/** Product criticality levels */
export const CRITICALITY_LEVELS = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as const;

// --- Evidence-related constants ---

/** Evidence document types for upload */
export const EVIDENCE_TYPES = [
  'SBOM',
  'TEST_REPORT',
  'VULNERABILITY_SCAN',
  'PENTEST_REPORT',
  'DESIGN_DOC',
  'INCIDENT_RESPONSE_PLAN',
  'UPDATE_POLICY',
  'CONFORMITY_DECLARATION',
  'OTHER',
] as const;
