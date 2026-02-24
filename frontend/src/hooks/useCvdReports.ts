import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { cvdReportsApi } from '../api/cvdReports';
import type { VulnerabilityReportTriageRequest } from '../types';

export function useCvdReports(status?: string) {
  return useQuery({
    queryKey: ['cvd-reports', status],
    queryFn: () => cvdReportsApi.list(status),
  });
}

export function useCvdReport(id: string) {
  return useQuery({
    queryKey: ['cvd-report', id],
    queryFn: () => cvdReportsApi.getById(id),
    enabled: !!id,
  });
}

export function useCvdReportTriage() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, req }: { id: string; req: VulnerabilityReportTriageRequest }) =>
      cvdReportsApi.triage(id, req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['cvd-reports'] });
      qc.invalidateQueries({ queryKey: ['cvd-report'] });
      qc.invalidateQueries({ queryKey: ['cvd-new-count'] });
    },
  });
}

export function useCvdNewCount() {
  return useQuery({
    queryKey: ['cvd-new-count'],
    queryFn: () => cvdReportsApi.countNew(),
    refetchInterval: 30000, // refresh every 30s for badge
  });
}
