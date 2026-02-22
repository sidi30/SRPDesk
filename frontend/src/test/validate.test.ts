import { describe, it, expect } from 'vitest';
import {
  validate,
  productCreateSchema,
  craEventCreateSchema,
  findingDecisionSchema,
} from '../validation/schemas';

describe('productCreateSchema', () => {
  it('accepts valid product data', () => {
    const data = {
      name: 'IoT Gateway v2',
      type: 'DEFAULT',
      criticality: 'MEDIUM',
    };
    const result = validate(productCreateSchema, data);
    expect(result.success).toBe(true);
    if (result.success) {
      expect(result.data.name).toBe('IoT Gateway v2');
    }
  });

  it('accepts valid product data with contacts', () => {
    const data = {
      name: 'Smart Sensor',
      type: 'CLASS_I',
      criticality: 'HIGH',
      contacts: [{ name: 'John Doe', email: 'john@example.com', role: 'Owner' }],
    };
    const result = validate(productCreateSchema, data);
    expect(result.success).toBe(true);
  });

  it('fails when name is missing', () => {
    const data = {
      name: '',
      type: 'DEFAULT',
      criticality: 'MEDIUM',
    };
    const result = validate(productCreateSchema, data);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors['name']).toBeDefined();
    }
  });

  it('fails when type is invalid', () => {
    const data = {
      name: 'Test Product',
      type: 'INVALID_TYPE',
      criticality: 'MEDIUM',
    };
    const result = validate(productCreateSchema, data);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors['type']).toBeDefined();
    }
  });

  it('fails when criticality is invalid', () => {
    const data = {
      name: 'Test Product',
      type: 'DEFAULT',
      criticality: 'VERY_HIGH',
    };
    const result = validate(productCreateSchema, data);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors['criticality']).toBeDefined();
    }
  });
});

describe('craEventCreateSchema', () => {
  it('accepts valid CRA event data', () => {
    const data = {
      productId: '550e8400-e29b-41d4-a716-446655440000',
      eventType: 'EXPLOITED_VULNERABILITY',
      title: 'Critical vulnerability in firmware',
      detectedAt: '2026-01-15T10:00:00Z',
    };
    const result = validate(craEventCreateSchema, data);
    expect(result.success).toBe(true);
  });

  it('fails when title is missing', () => {
    const data = {
      productId: '550e8400-e29b-41d4-a716-446655440000',
      eventType: 'EXPLOITED_VULNERABILITY',
      title: '',
      detectedAt: '2026-01-15T10:00:00Z',
    };
    const result = validate(craEventCreateSchema, data);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors['title']).toBeDefined();
    }
  });

  it('fails when productId is not a valid UUID', () => {
    const data = {
      productId: 'not-a-uuid',
      eventType: 'SEVERE_INCIDENT',
      title: 'Severe incident detected',
      detectedAt: '2026-01-15T10:00:00Z',
    };
    const result = validate(craEventCreateSchema, data);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors['productId']).toBeDefined();
    }
  });

  it('fails when detectedAt is missing', () => {
    const data = {
      productId: '550e8400-e29b-41d4-a716-446655440000',
      eventType: 'EXPLOITED_VULNERABILITY',
      title: 'Vulnerability found',
      detectedAt: '',
    };
    const result = validate(craEventCreateSchema, data);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors['detectedAt']).toBeDefined();
    }
  });
});

describe('findingDecisionSchema', () => {
  it('accepts valid decision data', () => {
    const data = {
      decisionType: 'NOT_AFFECTED',
      rationale: 'Component is not used in production environment',
    };
    const result = validate(findingDecisionSchema, data);
    expect(result.success).toBe(true);
  });

  it('fails when rationale is missing', () => {
    const data = {
      decisionType: 'PATCH_PLANNED',
      rationale: '',
    };
    const result = validate(findingDecisionSchema, data);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors['rationale']).toBeDefined();
    }
  });

  it('fails when decisionType is invalid', () => {
    const data = {
      decisionType: 'UNKNOWN_DECISION',
      rationale: 'Some rationale',
    };
    const result = validate(findingDecisionSchema, data);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors['decisionType']).toBeDefined();
    }
  });

  it('accepts optional dueDate and fixReleaseId', () => {
    const data = {
      decisionType: 'PATCH_PLANNED',
      rationale: 'Patch scheduled for next sprint',
      dueDate: '2026-03-01',
      fixReleaseId: '550e8400-e29b-41d4-a716-446655440000',
    };
    const result = validate(findingDecisionSchema, data);
    expect(result.success).toBe(true);
  });
});

describe('validate()', () => {
  it('returns success: true with parsed data for valid input', () => {
    const result = validate(productCreateSchema, {
      name: 'My Product',
      type: 'DEFAULT',
      criticality: 'LOW',
    });
    expect(result.success).toBe(true);
    if (result.success) {
      expect(result.data).toEqual({
        name: 'My Product',
        type: 'DEFAULT',
        criticality: 'LOW',
      });
    }
  });

  it('returns success: false with errors map for invalid input', () => {
    const result = validate(productCreateSchema, {
      name: '',
      type: 'INVALID',
      criticality: 'INVALID',
    });
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(typeof result.errors).toBe('object');
      expect(Object.keys(result.errors).length).toBeGreaterThan(0);
    }
  });
});
