import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ciPolicyApi } from '../api/ciPolicy';
import type { CiPolicyRequest } from '../types';

export function useCiPolicy() {
  return useQuery({
    queryKey: ['ci-policy'],
    queryFn: ciPolicyApi.get,
    retry: false,
  });
}

export function useUpsertCiPolicy() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: CiPolicyRequest) => ciPolicyApi.upsert(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ci-policy'] });
    },
  });
}
