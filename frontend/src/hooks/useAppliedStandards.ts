import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { appliedStandardsApi } from '../api/appliedStandards';
import type { AppliedStandardRequest } from '../types';

export function useAppliedStandards(productId: string) {
  return useQuery({
    queryKey: ['applied-standards', productId],
    queryFn: () => appliedStandardsApi.list(productId),
    enabled: !!productId,
  });
}

export function useCreateStandard(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: AppliedStandardRequest) => appliedStandardsApi.create(productId, req),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['applied-standards', productId] }),
  });
}

export function useUpdateStandard(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ standardId, req }: { standardId: string; req: AppliedStandardRequest }) =>
      appliedStandardsApi.update(productId, standardId, req),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['applied-standards', productId] }),
  });
}

export function useDeleteStandard(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (standardId: string) => appliedStandardsApi.delete(productId, standardId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['applied-standards', productId] }),
  });
}
