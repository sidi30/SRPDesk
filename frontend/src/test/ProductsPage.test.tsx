import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ProductsPage } from '../pages/ProductsPage';
import type { Product } from '../types';

vi.mock('../hooks/useProducts', () => ({
  useProducts: vi.fn(),
  useCreateProduct: vi.fn(),
  useDeleteProduct: vi.fn(),
}));

vi.mock('../auth/AuthProvider', () => ({
  useAuth: vi.fn(),
}));

import { useProducts, useCreateProduct, useDeleteProduct } from '../hooks/useProducts';
import { useAuth } from '../auth/AuthProvider';

const mockUseProducts = vi.mocked(useProducts);
const mockUseCreateProduct = vi.mocked(useCreateProduct);
const mockUseDeleteProduct = vi.mocked(useDeleteProduct);
const mockUseAuth = vi.mocked(useAuth);

function renderProductsPage() {
  return render(
    <MemoryRouter>
      <ProductsPage />
    </MemoryRouter>
  );
}

const mockMutationReturn = {
  mutate: vi.fn(),
  mutateAsync: vi.fn(),
  isPending: false,
  isIdle: true,
  isSuccess: false,
  isError: false,
  error: null,
  data: undefined,
  variables: undefined,
  reset: vi.fn(),
  context: undefined,
  failureCount: 0,
  failureReason: null,
  status: 'idle' as const,
  submittedAt: 0,
};

describe('ProductsPage', () => {
  beforeEach(() => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      token: 'test-token',
      userId: 'user-1',
      orgId: 'org-1',
      roles: ['ADMIN'],
      userName: 'admin',
      login: vi.fn(),
      logout: vi.fn(),
      hasRole: vi.fn((role: string) => role === 'ADMIN'),
    });

    mockUseCreateProduct.mockReturnValue(mockMutationReturn as unknown as ReturnType<typeof useCreateProduct>);
    mockUseDeleteProduct.mockReturnValue(mockMutationReturn as unknown as ReturnType<typeof useDeleteProduct>);
  });

  it('shows loading state', () => {
    mockUseProducts.mockReturnValue({
      data: undefined,
      isLoading: true,
    } as unknown as ReturnType<typeof useProducts>);

    renderProductsPage();
    expect(screen.getByText('Loading products...')).toBeInTheDocument();
  });

  it('renders product list with product data', () => {
    const products: Product[] = [
      {
        id: '1',
        orgId: 'org-1',
        name: 'IoT Gateway v2',
        type: 'DEFAULT',
        criticality: 'MEDIUM',
        contacts: [],
        createdAt: '2026-01-01T00:00:00Z',
        updatedAt: '2026-01-01T00:00:00Z',
      },
      {
        id: '2',
        orgId: 'org-1',
        name: 'Smart Sensor',
        type: 'CLASS_I',
        criticality: 'HIGH',
        contacts: [{ name: 'John', email: 'john@example.com' }],
        createdAt: '2026-01-15T00:00:00Z',
        updatedAt: '2026-01-15T00:00:00Z',
      },
    ];

    mockUseProducts.mockReturnValue({
      data: products,
      isLoading: false,
    } as unknown as ReturnType<typeof useProducts>);

    renderProductsPage();

    expect(screen.getByText('Products')).toBeInTheDocument();
    expect(screen.getByText('IoT Gateway v2')).toBeInTheDocument();
    expect(screen.getByText('Smart Sensor')).toBeInTheDocument();
  });

  it('shows empty state when no products', () => {
    mockUseProducts.mockReturnValue({
      data: [],
      isLoading: false,
    } as unknown as ReturnType<typeof useProducts>);

    renderProductsPage();

    expect(
      screen.getByText('No products yet. Add your first digital product to begin CRA compliance tracking.')
    ).toBeInTheDocument();
  });

  it('shows "Add Product" button for ADMIN role', () => {
    mockUseProducts.mockReturnValue({
      data: [],
      isLoading: false,
    } as unknown as ReturnType<typeof useProducts>);

    renderProductsPage();

    expect(screen.getByText('Add Product')).toBeInTheDocument();
  });

  it('hides "Add Product" button for CONTRIBUTOR role', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      token: 'test-token',
      userId: 'user-1',
      orgId: 'org-1',
      roles: ['CONTRIBUTOR'],
      userName: 'contributor',
      login: vi.fn(),
      logout: vi.fn(),
      hasRole: vi.fn().mockReturnValue(false),
    });

    mockUseProducts.mockReturnValue({
      data: [],
      isLoading: false,
    } as unknown as ReturnType<typeof useProducts>);

    renderProductsPage();

    expect(screen.queryByText('Add Product')).not.toBeInTheDocument();
  });

  it('shows page description', () => {
    mockUseProducts.mockReturnValue({
      data: [],
      isLoading: false,
    } as unknown as ReturnType<typeof useProducts>);

    renderProductsPage();

    expect(
      screen.getByText('Manage your digital products for CRA compliance')
    ).toBeInTheDocument();
  });
});
