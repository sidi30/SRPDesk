export interface Product {
  id: string;
  orgId: string;
  name: string;
  type: string;
  criticality: string;
  contacts: ContactInfo[];
  createdAt: string;
  updatedAt: string;
}

export interface ContactInfo {
  name: string;
  email: string;
  role?: string;
}

export interface ProductCreateRequest {
  name: string;
  type: string;
  criticality: string;
  contacts?: ContactInfo[];
}

export interface ProductUpdateRequest {
  name: string;
  type: string;
  criticality: string;
  contacts?: ContactInfo[];
}

export interface Release {
  id: string;
  productId: string;
  version: string;
  gitRef?: string;
  buildId?: string;
  releasedAt?: string;
  supportedUntil?: string;
  status: ReleaseStatus;
  createdAt: string;
  updatedAt: string;
}

export type ReleaseStatus = 'DRAFT' | 'RELEASED' | 'DEPRECATED' | 'REVOKED';

export interface ReleaseCreateRequest {
  version: string;
  gitRef?: string;
  buildId?: string;
  releasedAt?: string;
  supportedUntil?: string;
}

export interface Evidence {
  id: string;
  releaseId: string;
  orgId: string;
  type: EvidenceType;
  filename: string;
  contentType: string;
  size: number;
  sha256: string;
  createdAt: string;
  createdBy: string;
}

export type EvidenceType = 'SBOM' | 'TEST_REPORT' | 'VULNERABILITY_SCAN' | 'PENTEST_REPORT' | 'DESIGN_DOC' | 'INCIDENT_RESPONSE_PLAN' | 'UPDATE_POLICY' | 'CONFORMITY_DECLARATION' | 'OTHER';

export interface ComponentItem {
  purl: string;
  name: string;
  version: string;
  type: string;
}

export interface Finding {
  id: string;
  releaseId: string;
  componentId: string;
  componentName?: string;
  componentPurl?: string;
  vulnerabilityId: string;
  osvId?: string;
  summary?: string;
  details?: string;
  severity?: string;
  aliases?: string[];
  publishedAt?: string;
  osvUrl?: string;
  status: string;
  detectedAt: string;
  source: string;
  decisions: FindingDecision[];
}

export interface FindingDecision {
  id: string;
  decisionType: string;
  rationale: string;
  dueDate?: string;
  decidedBy: string;
  fixReleaseId?: string;
  createdAt: string;
}

export interface FindingDecisionRequest {
  decisionType: string;
  rationale: string;
  dueDate?: string;
  fixReleaseId?: string;
}

export interface SbomUploadResponse {
  evidenceId: string;
  componentCount: number;
  sha256: string;
}

export interface AuditVerifyResponse {
  valid: boolean;
  totalEvents: number;
  verifiedEvents: number;
  message: string;
}

export interface AuditEvent {
  id: string;
  orgId: string;
  entityType: string;
  entityId: string;
  action: string;
  actor: string;
  payloadJson: string;
  createdAt: string;
  prevHash: string | null;
  hash: string;
}

export interface ProblemDetail {
  type?: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
}

// ── CRA War Room ────────────────────────────────────────

export type CraEventType = 'EXPLOITED_VULNERABILITY' | 'SEVERE_INCIDENT';
export type CraEventStatus = 'DRAFT' | 'IN_REVIEW' | 'SUBMITTED' | 'CLOSED';
export type ParticipantRole = 'OWNER' | 'APPROVER' | 'VIEWER';
export type SubmissionType = 'EARLY_WARNING' | 'NOTIFICATION' | 'FINAL_REPORT';
export type SubmissionStatus = 'DRAFT' | 'READY' | 'EXPORTED' | 'SUBMITTED';

export interface CraEvent {
  id: string;
  orgId: string;
  productId: string;
  productName?: string;
  eventType: CraEventType;
  title: string;
  description?: string;
  status: CraEventStatus;
  startedAt?: string;
  detectedAt: string;
  patchAvailableAt?: string;
  resolvedAt?: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  participants: CraEventParticipant[];
  links: CraEventLink[];
}

export interface CraEventParticipant {
  id: string;
  userId: string;
  role: ParticipantRole;
  createdAt: string;
}

export interface CraEventLink {
  id: string;
  linkType: 'RELEASE' | 'FINDING' | 'EVIDENCE';
  targetId: string;
  createdAt: string;
}

export interface CraEventCreateRequest {
  productId: string;
  eventType: CraEventType;
  title: string;
  description?: string;
  startedAt?: string;
  detectedAt: string;
}

export interface CraEventUpdateRequest {
  title?: string;
  description?: string;
  status?: CraEventStatus;
  startedAt?: string;
  detectedAt?: string;
  patchAvailableAt?: string;
  resolvedAt?: string;
}

export interface SlaResponse {
  earlyWarning: SlaDeadline;
  notification: SlaDeadline;
  finalReport: SlaDeadline | null;
}

export interface SlaDeadline {
  dueAt: string;
  remainingSeconds: number;
  overdue: boolean;
}

export interface SrpSubmission {
  id: string;
  craEventId: string;
  submissionType: SubmissionType;
  status: SubmissionStatus;
  contentJson: unknown;
  schemaVersion: string;
  validationErrors: string[] | null;
  submittedReference?: string;
  submittedAt?: string;
  acknowledgmentEvidenceId?: string;
  generatedBy: string;
  generatedAt: string;
  updatedAt: string;
}

export interface SrpSubmissionCreateRequest {
  submissionType: SubmissionType;
}

export interface MarkSubmittedRequest {
  reference: string;
}

// ── AI Module ──────────────────────────────────────────────

export interface AiJobResponse {
  id: string;
  jobType: string;
  status: string;
  model: string;
  error: string | null;
  createdAt: string;
  completedAt: string | null;
  artifacts: AiArtifactResponse[];
}

export interface AiArtifactResponse {
  id: string;
  kind: string;
  contentJson: unknown;
  createdAt: string;
}
