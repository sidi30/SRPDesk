import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { readinessApi } from '../api/readiness';

export function useReadinessScore(productId: string) {
  return useQuery({
    queryKey: ['readiness', productId],
    queryFn: () => readinessApi.getScore(productId),
    enabled: !!productId,
  });
}

export function useSnapshotReadiness(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => readinessApi.snapshot(productId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['readiness', productId] });
    },
  });
}
