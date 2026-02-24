import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { cvdReportsApi } from '../api/cvdReports';
import type { VulnerabilityReportResponse, VulnerabilityReportTriageRequest } from '../types';

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

function useCvdAction(actionFn: (id: string) => Promise<VulnerabilityReportResponse>) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => actionFn(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['cvd-reports'] });
      qc.invalidateQueries({ queryKey: ['cvd-report'] });
      qc.invalidateQueries({ queryKey: ['cvd-new-count'] });
    },
  });
}

export function useCvdAcknowledge() {
  return useCvdAction(cvdReportsApi.acknowledge);
}

export function useCvdStartTriage() {
  return useCvdAction(cvdReportsApi.startTriage);
}

export function useCvdConfirm() {
  return useCvdAction(cvdReportsApi.confirm);
}

export function useCvdReject() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason?: string }) =>
      cvdReportsApi.reject(id, reason),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['cvd-reports'] });
      qc.invalidateQueries({ queryKey: ['cvd-report'] });
      qc.invalidateQueries({ queryKey: ['cvd-new-count'] });
    },
  });
}

export function useCvdStartFix() {
  return useCvdAction(cvdReportsApi.startFix);
}

export function useCvdMarkFixed() {
  return useCvdAction(cvdReportsApi.markFixed);
}

export function useCvdDisclose() {
  return useCvdAction(cvdReportsApi.disclose);
}

export function useCvdNewCount() {
  return useQuery({
    queryKey: ['cvd-new-count'],
    queryFn: () => cvdReportsApi.countNew(),
    refetchInterval: 30000,
  });
}
