import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { findingsApi } from '../api/findings';
import type { FindingDecisionRequest } from '../types';

export function useProductFindings(productId: string, status?: string) {
  return useQuery({
    queryKey: ['findings', 'product', productId, status],
    queryFn: () => findingsApi.listByProduct(productId, status),
    enabled: !!productId,
  });
}

export function useReleaseFindings(releaseId: string, status?: string) {
  return useQuery({
    queryKey: ['findings', 'release', releaseId, status],
    queryFn: () => findingsApi.listByRelease(releaseId, status),
    enabled: !!releaseId,
  });
}

export function useAddDecision() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ findingId, data }: { findingId: string; data: FindingDecisionRequest }) =>
      findingsApi.addDecision(findingId, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['findings'] }),
  });
}

export function useTriggerScan(releaseId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => findingsApi.triggerScan(releaseId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['findings'] }),
  });
}
