import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { evidencesApi } from '../api/evidences';

export function useEvidences(releaseId: string) {
  return useQuery({
    queryKey: ['evidences', releaseId],
    queryFn: () => evidencesApi.listByRelease(releaseId),
    enabled: !!releaseId,
  });
}

export function useUploadEvidence(releaseId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ file, type }: { file: File; type: string }) => evidencesApi.upload(releaseId, file, type),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['evidences', releaseId] }),
  });
}

export function useUploadSbom(releaseId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (file: File) => evidencesApi.uploadSbom(releaseId, file),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['evidences', releaseId] });
      qc.invalidateQueries({ queryKey: ['components', releaseId] });
    },
  });
}

export function useComponents(releaseId: string) {
  return useQuery({
    queryKey: ['components', releaseId],
    queryFn: () => evidencesApi.getComponents(releaseId),
    enabled: !!releaseId,
  });
}
