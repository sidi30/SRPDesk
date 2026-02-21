import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { releasesApi } from '../api/releases';
import type { ReleaseCreateRequest } from '../types';

export function useReleases(productId: string) {
  return useQuery({
    queryKey: ['releases', productId],
    queryFn: () => releasesApi.listByProduct(productId),
    enabled: !!productId,
  });
}

export function useCreateRelease(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: ReleaseCreateRequest) => releasesApi.create(productId, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['releases', productId] }),
  });
}

export function useExportPack() {
  return useMutation({
    mutationFn: (releaseId: string) => releasesApi.exportPack(releaseId),
  });
}
