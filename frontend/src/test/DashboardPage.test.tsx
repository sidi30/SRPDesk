import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { DashboardPage } from '../pages/DashboardPage';
import type { ProductReadiness } from '../types';

vi.mock('../hooks/useDashboard', () => ({
  useDashboard: vi.fn(),
}));

vi.mock('../hooks/useAuditEvents', () => ({
  useAuditVerify: vi.fn(),
}));

import { useDashboard } from '../hooks/useDashboard';
import { useAuditVerify } from '../hooks/useAuditEvents';

const mockedUseDashboard = vi.mocked(useDashboard);
const mockedUseAuditVerify = vi.mocked(useAuditVerify);

describe('DashboardPage', () => {
  beforeEach(() => {
    mockedUseAuditVerify.mockReturnValue({
      data: undefined,
      isLoading: false,
    } as ReturnType<typeof useAuditVerify>);
  });

  it('shows loading state', () => {
    mockedUseDashboard.mockReturnValue({
      data: undefined,
      isLoading: true,
    } as ReturnType<typeof useDashboard>);

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(screen.getByText('Chargement...')).toBeDefined();
  });

  it('renders dashboard with metrics', () => {
    mockedUseDashboard.mockReturnValue({
      data: {
        totalProducts: 3,
        totalReleases: 5,
        totalFindings: 10,
        openFindings: 4,
        criticalHighFindings: 2,
        totalCraEvents: 1,
        activeCraEvents: 1,
        averageReadinessScore: 72.5,
        productReadiness: [
          {
            productId: '1',
            productName: 'Smart Sensor',
            type: 'CLASS_I',
            conformityPath: 'HARMONISED_STANDARD_OR_THIRD_PARTY',
            readinessScore: 72,
            checklistTotal: 21,
            checklistCompliant: 15,
          },
        ],
      },
      isLoading: false,
    } as ReturnType<typeof useDashboard>);

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(screen.getByText('3')).toBeDefined(); // products
    expect(screen.getByText('5')).toBeDefined(); // releases
    expect(screen.getByText('4')).toBeDefined(); // open findings
    expect(screen.getByText('2')).toBeDefined(); // critical
    expect(screen.getByText('Smart Sensor')).toBeDefined();
    expect(screen.getByText('72.5')).toBeDefined(); // avg readiness
  });

  it('shows no products message when empty', () => {
    mockedUseDashboard.mockReturnValue({
      data: {
        totalProducts: 0,
        totalReleases: 0,
        totalFindings: 0,
        openFindings: 0,
        criticalHighFindings: 0,
        totalCraEvents: 0,
        activeCraEvents: 0,
        averageReadinessScore: 0,
        productReadiness: [] as ProductReadiness[],
      },
      isLoading: false,
    } as ReturnType<typeof useDashboard>);

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>
    );

    expect(screen.getByText(/Aucun produit/)).toBeDefined();
  });
});
