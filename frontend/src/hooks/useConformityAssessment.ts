import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { conformityAssessmentApi } from '../api/conformityAssessment';

export function useConformityAssessment(productId: string, module: string) {
  return useQuery({
    queryKey: ['conformity-assessment', productId, module],
    queryFn: () => conformityAssessmentApi.get(productId, module),
    enabled: !!productId && !!module,
    retry: false,
  });
}

export function useInitiateAssessment(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (module: string) => conformityAssessmentApi.initiate(productId, module),
    onSuccess: (_, module) => qc.invalidateQueries({ queryKey: ['conformity-assessment', productId, module] }),
  });
}

export function useCompleteStep(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ assessmentId, stepIndex, notes }: { assessmentId: string; stepIndex: number; notes?: string }) =>
      conformityAssessmentApi.completeStep(productId, assessmentId, stepIndex, notes),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['conformity-assessment', productId] }),
  });
}

export function useApproveAssessment(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (assessmentId: string) => conformityAssessmentApi.approve(productId, assessmentId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['conformity-assessment', productId] }),
  });
}
