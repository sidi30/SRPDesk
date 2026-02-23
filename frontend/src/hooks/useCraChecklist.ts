import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { craChecklistApi } from '../api/craChecklist';
import type { CraChecklistUpdateRequest } from '../types';

export function useCraChecklist(productId: string) {
  return useQuery({
    queryKey: ['cra-checklist', productId],
    queryFn: () => craChecklistApi.list(productId),
    enabled: !!productId,
  });
}

export function useCraChecklistSummary(productId: string) {
  return useQuery({
    queryKey: ['cra-checklist-summary', productId],
    queryFn: () => craChecklistApi.summary(productId),
    enabled: !!productId,
  });
}

export function useInitializeCraChecklist(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => craChecklistApi.initialize(productId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['cra-checklist', productId] });
      qc.invalidateQueries({ queryKey: ['cra-checklist-summary', productId] });
    },
  });
}

export function useUpdateCraChecklistItem(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ itemId, data }: { itemId: string; data: CraChecklistUpdateRequest }) =>
      craChecklistApi.update(productId, itemId, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['cra-checklist', productId] });
      qc.invalidateQueries({ queryKey: ['cra-checklist-summary', productId] });
      qc.invalidateQueries({ queryKey: ['readiness', productId] });
    },
  });
}

export function useLinkEvidence(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ itemId, evidenceId }: { itemId: string; evidenceId: string }) =>
      craChecklistApi.linkEvidence(productId, itemId, evidenceId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['cra-checklist', productId] });
    },
  });
}
