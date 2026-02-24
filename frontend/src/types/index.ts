export interface Product {
  id: string;
  orgId: string;
  name: string;
  type: string;
  criticality: string;
  conformityPath?: string;
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

export type SecurityUpdateType = 'SECURITY_CRITICAL' | 'SECURITY_HIGH' | 'SECURITY' | 'FUNCTIONALITY' | 'MAINTENANCE';

export interface Release {
  id: string;
  productId: string;
  version: string;
  gitRef?: string;
  buildId?: string;
  releasedAt?: string;
  supportedUntil?: string;
  status: ReleaseStatus;
  updateType?: SecurityUpdateType;
  securityImpact?: string;
  cveIds?: string;
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

export function getErrorMessage(err: unknown, fallback = 'Une erreur est survenue'): string {
  if (err && typeof err === 'object') {
    const axiosErr = err as { response?: { data?: ProblemDetail }; message?: string };
    return axiosErr.response?.data?.detail || axiosErr.message || fallback;
  }
  return fallback;
}

// ── API Keys ────────────────────────────────────────────

export interface ApiKeyCreateRequest {
  name: string;
}

export interface ApiKeyCreateResponse {
  id: string;
  name: string;
  keyPrefix: string;
  plainTextKey: string;
}

export interface ApiKeyResponse {
  id: string;
  name: string;
  keyPrefix: string;
  scopes: string;
  createdAt: string;
  lastUsedAt: string | null;
  revoked: boolean;
  revokedAt: string | null;
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
  // ENISA SRP fields
  enisaReference?: string;
  enisaSubmittedAt?: string;
  enisaStatus?: string;
  // CSIRT fields (Art. 14 parallel notification)
  csirtReference?: string;
  csirtSubmittedAt?: string;
  csirtStatus?: string;
  csirtCountryCode?: string;
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

// ── CRA Checklist ──────────────────────────────────────────

export type ChecklistStatus = 'NOT_ASSESSED' | 'COMPLIANT' | 'PARTIALLY_COMPLIANT' | 'NON_COMPLIANT' | 'NOT_APPLICABLE';

export interface CraChecklistItem {
  id: string;
  productId: string;
  requirementRef: string;
  category: string;
  title: string;
  description?: string;
  status: ChecklistStatus;
  evidenceIds: string[];
  notes?: string;
  assessedBy?: string;
  assessedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CraChecklistUpdateRequest {
  status?: ChecklistStatus;
  notes?: string;
  evidenceIds?: string[];
}

export interface CraChecklistSummary {
  productId: string;
  totalItems: number;
  compliant: number;
  partiallyCompliant: number;
  nonCompliant: number;
  notAssessed: number;
  categories: Record<string, {
    total: number;
    compliant: number;
    partiallyCompliant: number;
    nonCompliant: number;
    notAssessed: number;
  }>;
}

// ── Readiness Score ────────────────────────────────────────

export interface ReadinessScore {
  productId: string;
  overallScore: number;
  categories: ReadinessCategoryScore[];
  actionItems: string[];
}

export interface ReadinessCategoryScore {
  name: string;
  score: number;
  maxScore: number;
  label: string;
}

// ── Webhooks ──────────────────────────────────────────────

export interface WebhookCreateRequest {
  name: string;
  url: string;
  secret?: string;
  eventTypes?: string;
  channelType: string;
}

export interface WebhookResponse {
  id: string;
  name: string;
  url: string;
  eventTypes: string;
  channelType: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

// ── Security Advisories (Art.14.3) ───────────────────────

export type AdvisoryStatus = 'DRAFT' | 'PUBLISHED' | 'NOTIFIED';

export interface SecurityAdvisoryCreateRequest {
  craEventId: string;
  title: string;
  severity: string;
  affectedVersions?: string;
  description: string;
  remediation?: string;
}

export interface SecurityAdvisoryResponse {
  id: string;
  craEventId: string;
  productId: string;
  title: string;
  severity: string;
  affectedVersions?: string;
  description: string;
  remediation?: string;
  advisoryUrl?: string;
  status: AdvisoryStatus;
  publishedAt?: string;
  notifiedAt?: string;
  createdAt: string;
  updatedAt: string;
}

// ── Dashboard ──────────────────────────────────────────────

export interface DashboardData {
  totalProducts: number;
  totalReleases: number;
  totalFindings: number;
  openFindings: number;
  criticalHighFindings: number;
  totalCraEvents: number;
  activeCraEvents: number;
  averageReadinessScore: number;
  productReadiness: ProductReadiness[];
}

export interface ProductReadiness {
  productId: string;
  productName: string;
  type: string;
  conformityPath?: string;
  readinessScore: number;
  checklistTotal: number;
  checklistCompliant: number;
}

// ── CVD Policy (CRA Annexe I §2(5)) ───────────────────

export type CvdPolicyStatus = 'DRAFT' | 'PUBLISHED';

export interface CvdPolicy {
  id: string;
  productId: string;
  contactEmail: string;
  contactUrl?: string;
  pgpKeyUrl?: string;
  policyUrl?: string;
  disclosureTimelineDays: number;
  acceptsAnonymous: boolean;
  bugBountyUrl?: string;
  acceptedLanguages?: string;
  scopeDescription?: string;
  status: CvdPolicyStatus;
  publishedAt?: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface CvdPolicyRequest {
  contactEmail: string;
  contactUrl?: string;
  pgpKeyUrl?: string;
  policyUrl?: string;
  disclosureTimelineDays?: number;
  acceptsAnonymous?: boolean;
  bugBountyUrl?: string;
  acceptedLanguages?: string;
  scopeDescription?: string;
}

// ── EU Declaration of Conformity (CRA Annexe V) ─────────

export type EuDocStatus = 'DRAFT' | 'SIGNED' | 'PUBLISHED';

export interface EuDeclarationOfConformity {
  id: string;
  productId: string;
  declarationNumber: string;
  manufacturerName: string;
  manufacturerAddress: string;
  authorizedRepName?: string;
  authorizedRepAddress?: string;
  productName: string;
  productIdentification: string;
  conformityAssessmentModule: string;
  notifiedBodyName?: string;
  notifiedBodyNumber?: string;
  notifiedBodyCertificate?: string;
  harmonisedStandards?: string;
  additionalInfo?: string;
  declarationText: string;
  signedBy: string;
  signedRole: string;
  signedAt: string;
  status: EuDocStatus;
  publishedAt?: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface EuDocRequest {
  declarationNumber: string;
  manufacturerName: string;
  manufacturerAddress: string;
  authorizedRepName?: string;
  authorizedRepAddress?: string;
  productName: string;
  productIdentification: string;
  conformityAssessmentModule?: string;
  notifiedBodyName?: string;
  notifiedBodyNumber?: string;
  notifiedBodyCertificate?: string;
  harmonisedStandards?: string;
  additionalInfo?: string;
  declarationText: string;
  signedBy: string;
  signedRole: string;
}

// ── Conformity Assessment (CRA Art. 32) ──────────────────

export type ConformityAssessmentStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'APPROVED';

export interface ConformityAssessment {
  id: string;
  productId: string;
  module: string;
  status: ConformityAssessmentStatus;
  currentStep: number;
  totalSteps: number;
  stepsData: string; // JSON string
  startedAt?: string;
  completedAt?: string;
  approvedBy?: string;
  approvedAt?: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface ConformityStep {
  name: string;
  description: string;
  status: 'PENDING' | 'COMPLETED';
  completedAt?: string;
  notes?: string;
  evidenceIds: string[];
}

// ── Risk Assessment (CRA Art. 13(1)) ─────────────────────

export type RiskLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
export type RiskAssessmentStatus = 'DRAFT' | 'IN_REVIEW' | 'APPROVED';

export interface RiskAssessment {
  id: string;
  productId: string;
  title: string;
  methodology: string;
  status: RiskAssessmentStatus;
  overallRiskLevel?: RiskLevel;
  summary?: string;
  approvedBy?: string;
  approvedAt?: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  items: RiskItem[];
}

export interface RiskItem {
  id: string;
  riskAssessmentId: string;
  threatCategory: string;
  threatDescription: string;
  affectedAsset?: string;
  likelihood: string;
  impact: string;
  riskLevel: RiskLevel;
  existingControls?: string;
  mitigationPlan?: string;
  mitigationStatus: string;
  residualRiskLevel?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RiskAssessmentRequest {
  title: string;
  methodology?: string;
  summary?: string;
}

export interface RiskItemRequest {
  threatCategory: string;
  threatDescription: string;
  affectedAsset?: string;
  likelihood: string;
  impact: string;
  existingControls?: string;
  mitigationPlan?: string;
}

// ── Applied Standards (CRA Art. 27) ──────────────────────

export type ComplianceStatus = 'CLAIMED' | 'PARTIAL' | 'FULL' | 'NOT_APPLICABLE';

export interface AppliedStandard {
  id: string;
  productId: string;
  standardCode: string;
  standardTitle: string;
  version?: string;
  complianceStatus: ComplianceStatus;
  notes?: string;
  evidenceIds?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AppliedStandardRequest {
  standardCode: string;
  standardTitle: string;
  version?: string;
  complianceStatus?: string;
  notes?: string;
  evidenceIds?: string;
}

// ── CVD Vulnerability Reports (CRA Art. 13(6)) ──────────

export type VulnerabilityReportStatus =
  | 'NEW' | 'ACKNOWLEDGED' | 'TRIAGING' | 'CONFIRMED'
  | 'REJECTED' | 'FIXING' | 'FIXED' | 'DISCLOSED';

export interface VulnerabilityReportResponse {
  id: string;
  orgId: string;
  productId?: string;
  trackingId: string;
  status: VulnerabilityReportStatus;
  reporterName?: string;
  reporterEmail?: string;
  anonymous: boolean;
  title: string;
  description: string;
  severityEstimate?: string;
  affectedComponent?: string;
  affectedVersions?: string;
  stepsToReproduce?: string;
  assignedTo?: string;
  internalNotes?: string;
  internalSeverity?: string;
  cvssScore?: number;
  cveId?: string;
  submittedAt: string;
  acknowledgedAt?: string;
  triagedAt?: string;
  fixedAt?: string;
  disclosedAt?: string;
  disclosureDeadline?: string;
  createdAt: string;
  updatedAt: string;
}

export interface VulnerabilityReportTriageRequest {
  action: string;
  productId?: string;
  assignedTo?: string;
  internalNotes?: string;
  internalSeverity?: string;
  cvssScore?: number;
  cveId?: string;
}
