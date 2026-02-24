import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { cvdPolicyApi } from '../api/cvdPolicy';
import type { CvdPolicyRequest } from '../types';

export function useCvdPolicy(productId: string) {
  return useQuery({
    queryKey: ['cvd-policy', productId],
    queryFn: () => cvdPolicyApi.get(productId),
    enabled: !!productId,
    retry: false,
  });
}

export function useSaveCvdPolicy() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ productId, data }: { productId: string; data: CvdPolicyRequest }) =>
      cvdPolicyApi.createOrUpdate(productId, data),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cvd-policy', vars.productId] }),
  });
}

export function usePublishCvdPolicy() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (productId: string) => cvdPolicyApi.publish(productId),
    onSuccess: (_, productId) => qc.invalidateQueries({ queryKey: ['cvd-policy', productId] }),
  });
}
