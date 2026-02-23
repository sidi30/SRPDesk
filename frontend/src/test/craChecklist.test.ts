import { describe, it, expect } from 'vitest';
import { validate, craChecklistUpdateSchema } from '../validation/schemas';

describe('craChecklistUpdateSchema', () => {
  it('accepts valid COMPLIANT status', () => {
    const result = validate(craChecklistUpdateSchema, {
      status: 'COMPLIANT',
      notes: 'Evidence attached and verified',
    });
    expect(result.success).toBe(true);
  });

  it('accepts all valid statuses', () => {
    const statuses = ['NOT_ASSESSED', 'COMPLIANT', 'PARTIALLY_COMPLIANT', 'NON_COMPLIANT', 'NOT_APPLICABLE'];
    for (const status of statuses) {
      const result = validate(craChecklistUpdateSchema, { status });
      expect(result.success).toBe(true);
    }
  });

  it('rejects invalid status', () => {
    const result = validate(craChecklistUpdateSchema, {
      status: 'INVALID_STATUS',
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors['status']).toBeDefined();
    }
  });

  it('accepts empty notes', () => {
    const result = validate(craChecklistUpdateSchema, {
      status: 'COMPLIANT',
      notes: '',
    });
    expect(result.success).toBe(true);
  });

  it('rejects notes exceeding 2000 characters', () => {
    const result = validate(craChecklistUpdateSchema, {
      status: 'COMPLIANT',
      notes: 'x'.repeat(2001),
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors['notes']).toBeDefined();
    }
  });

  it('accepts evidence IDs array', () => {
    const result = validate(craChecklistUpdateSchema, {
      status: 'COMPLIANT',
      evidenceIds: ['550e8400-e29b-41d4-a716-446655440000'],
    });
    expect(result.success).toBe(true);
  });

  it('rejects non-UUID evidence IDs', () => {
    const result = validate(craChecklistUpdateSchema, {
      status: 'COMPLIANT',
      evidenceIds: ['not-a-uuid'],
    });
    expect(result.success).toBe(false);
  });
});
