import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProtectedRoute } from '../auth/ProtectedRoute';

vi.mock('../auth/AuthProvider', () => ({
  useAuth: vi.fn(),
}));

import { useAuth } from '../auth/AuthProvider';
const mockUseAuth = vi.mocked(useAuth);

describe('ProtectedRoute', () => {
  it('shows "Authentification requise" when not authenticated', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: false,
      token: undefined,
      userId: undefined,
      orgId: undefined,
      roles: [],
      userName: undefined,
      login: vi.fn(),
      logout: vi.fn(),
      hasRole: vi.fn().mockReturnValue(false),
    });

    render(
      <ProtectedRoute>
        <div>Protected content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Authentification requise')).toBeInTheDocument();
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
  });

  it('renders children when authenticated with no role requirement', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      token: 'test-token',
      userId: 'user-1',
      orgId: 'org-1',
      roles: ['CONTRIBUTOR'],
      userName: 'testuser',
      login: vi.fn(),
      logout: vi.fn(),
      hasRole: vi.fn().mockReturnValue(true),
    });

    render(
      <ProtectedRoute>
        <div>Protected content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });

  it('shows access denied when role requirement is not met', () => {
    const hasRoleFn = vi.fn((role: string) => role === 'CONTRIBUTOR');
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      token: 'test-token',
      userId: 'user-1',
      orgId: 'org-1',
      roles: ['CONTRIBUTOR'],
      userName: 'testuser',
      login: vi.fn(),
      logout: vi.fn(),
      hasRole: hasRoleFn,
    });

    render(
      <ProtectedRoute requiredRoles={['ADMIN']}>
        <div>Admin content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Accès refusé')).toBeInTheDocument();
    expect(screen.queryByText('Admin content')).not.toBeInTheDocument();
  });

  it('renders children when role requirement is met', () => {
    const hasRoleFn = vi.fn((role: string) => role === 'ADMIN');
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      token: 'test-token',
      userId: 'user-1',
      orgId: 'org-1',
      roles: ['ADMIN'],
      userName: 'testuser',
      login: vi.fn(),
      logout: vi.fn(),
      hasRole: hasRoleFn,
    });

    render(
      <ProtectedRoute requiredRoles={['ADMIN']}>
        <div>Admin content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Admin content')).toBeInTheDocument();
  });

  it('grants access when user has one of multiple required roles', () => {
    const hasRoleFn = vi.fn((role: string) => role === 'COMPLIANCE_MANAGER');
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      token: 'test-token',
      userId: 'user-1',
      orgId: 'org-1',
      roles: ['COMPLIANCE_MANAGER'],
      userName: 'testuser',
      login: vi.fn(),
      logout: vi.fn(),
      hasRole: hasRoleFn,
    });

    render(
      <ProtectedRoute requiredRoles={['ADMIN', 'COMPLIANCE_MANAGER']}>
        <div>Manager content</div>
      </ProtectedRoute>
    );

    expect(screen.getByText('Manager content')).toBeInTheDocument();
  });
});
