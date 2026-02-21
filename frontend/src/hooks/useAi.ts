import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { aiApi } from '../api/ai';

export function useGenerateSrpDraft() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ craEventId, submissionType }: { craEventId: string; submissionType: string }) =>
      aiApi.generateSrpDraft(craEventId, submissionType),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['ai-jobs'] }),
  });
}

export function useGenerateCommPack() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (craEventId: string) => aiApi.generateCommPack(craEventId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['ai-jobs'] }),
  });
}

export function useParseQuestionnaire() {
  return useMutation({
    mutationFn: (file: File) => aiApi.parseQuestionnaire(file),
  });
}

export function useFillQuestionnaire() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ questionnaireText, productId }: { questionnaireText: string; productId?: string }) =>
      aiApi.fillQuestionnaire(questionnaireText, productId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['ai-jobs'] }),
  });
}

export function useAiJob(id: string) {
  return useQuery({
    queryKey: ['ai-jobs', id],
    queryFn: () => aiApi.getJob(id),
    enabled: !!id,
  });
}
