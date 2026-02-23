import { z } from 'zod';

export const productCreateSchema = z.object({
  name: z.string().min(1, 'Le nom du produit est requis').max(255, 'Le nom ne peut pas dépasser 255 caractères'),
  type: z.enum(['DEFAULT', 'CLASS_I', 'CLASS_II', 'IMPORTANT_CLASS_I', 'IMPORTANT_CLASS_II', 'CRITICAL']),
  criticality: z.enum(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']),
  contacts: z.array(z.object({
    name: z.string().min(1),
    email: z.string().email('Email invalide'),
    role: z.string().optional(),
  })).optional(),
});

export const productUpdateSchema = productCreateSchema;

export const releaseCreateSchema = z.object({
  version: z.string()
    .min(1, 'La version est requise')
    .max(100, 'La version ne peut pas dépasser 100 caractères')
    .regex(/^[\w.\-+]+$/, 'Format de version invalide (ex: 1.0.0, v2.1-beta)'),
  gitRef: z.string().max(255).optional().or(z.literal('')),
  buildId: z.string().max(255).optional().or(z.literal('')),
  releasedAt: z.string().optional().or(z.literal('')),
  supportedUntil: z.string().optional().or(z.literal('')),
});

export const craEventCreateSchema = z.object({
  productId: z.string().uuid('Sélectionnez un produit'),
  eventType: z.enum(['EXPLOITED_VULNERABILITY', 'SEVERE_INCIDENT']),
  title: z.string().min(1, 'Le titre est requis').max(500, 'Le titre ne peut pas dépasser 500 caractères'),
  description: z.string().max(5000).optional().or(z.literal('')),
  detectedAt: z.string().min(1, 'La date de détection est requise'),
});

export const findingDecisionSchema = z.object({
  decisionType: z.enum(['NOT_AFFECTED', 'PATCH_PLANNED', 'MITIGATED', 'FIXED']),
  rationale: z.string().min(1, 'La justification est requise').max(2000, 'La justification ne peut pas dépasser 2000 caractères'),
  dueDate: z.string().optional().or(z.literal('')),
  fixReleaseId: z.string().uuid().optional().or(z.literal('')),
});

export const craChecklistUpdateSchema = z.object({
  status: z.enum(['NOT_ASSESSED', 'COMPLIANT', 'PARTIALLY_COMPLIANT', 'NON_COMPLIANT', 'NOT_APPLICABLE']),
  notes: z.string().max(2000, 'Les notes ne peuvent pas dépasser 2000 caractères').optional().or(z.literal('')),
  evidenceIds: z.array(z.string().uuid()).optional(),
});

export type ValidationErrors = Record<string, string>;

export function validate<T>(schema: z.ZodSchema<T>, data: unknown): { success: true; data: T } | { success: false; errors: ValidationErrors } {
  const result = schema.safeParse(data);
  if (result.success) {
    return { success: true, data: result.data };
  }
  const errors: ValidationErrors = {};
  for (const issue of result.error.issues) {
    const key = issue.path.join('.');
    if (!errors[key]) {
      errors[key] = issue.message;
    }
  }
  return { success: false, errors };
}
