import { describe, it, expect } from 'vitest';
import { getErrorMessage } from '../types';

describe('getErrorMessage', () => {
  it('returns default fallback for null', () => {
    expect(getErrorMessage(null)).toBe('Une erreur est survenue');
  });

  it('returns default fallback for undefined', () => {
    expect(getErrorMessage(undefined)).toBe('Une erreur est survenue');
  });

  it('returns detail from axios-like error response', () => {
    const err = {
      response: {
        data: {
          title: 'Bad Request',
          status: 400,
          detail: 'Le nom du produit est requis',
        },
      },
    };
    expect(getErrorMessage(err)).toBe('Le nom du produit est requis');
  });

  it('returns message from Error-like object when no response data', () => {
    const err = { message: 'Network Error' };
    expect(getErrorMessage(err)).toBe('Network Error');
  });

  it('returns message from native Error', () => {
    const err = new Error('Something went wrong');
    expect(getErrorMessage(err)).toBe('Something went wrong');
  });

  it('returns custom fallback string when provided', () => {
    expect(getErrorMessage(null, 'Custom fallback')).toBe('Custom fallback');
  });

  it('returns custom fallback for non-object error', () => {
    expect(getErrorMessage('string error', 'Fallback')).toBe('Fallback');
  });

  it('prefers response.data.detail over message', () => {
    const err = {
      response: {
        data: {
          title: 'Conflict',
          status: 409,
          detail: 'Product already exists',
        },
      },
      message: 'Request failed with status code 409',
    };
    expect(getErrorMessage(err)).toBe('Product already exists');
  });

  it('falls back to message when response.data has no detail', () => {
    const err = {
      response: {
        data: {
          title: 'Internal Server Error',
          status: 500,
        },
      },
      message: 'Request failed with status code 500',
    };
    expect(getErrorMessage(err)).toBe('Request failed with status code 500');
  });

  it('returns fallback when object has neither response nor message', () => {
    const err = { code: 'ERR_NETWORK' };
    expect(getErrorMessage(err)).toBe('Une erreur est survenue');
  });
});
