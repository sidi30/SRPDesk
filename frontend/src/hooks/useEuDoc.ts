import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { euDocApi } from '../api/euDoc';
import type { EuDocRequest } from '../types';

export function useEuDocs(productId: string) {
  return useQuery({
    queryKey: ['eu-docs', productId],
    queryFn: () => euDocApi.list(productId),
    enabled: !!productId,
  });
}

export function useCreateEuDoc(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: EuDocRequest) => euDocApi.create(productId, req),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['eu-docs', productId] }),
  });
}

export function useSignEuDoc(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (docId: string) => euDocApi.sign(productId, docId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['eu-docs', productId] }),
  });
}

export function usePublishEuDoc(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (docId: string) => euDocApi.publish(productId, docId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['eu-docs', productId] }),
  });
}
