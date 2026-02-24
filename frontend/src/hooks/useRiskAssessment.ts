import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { riskAssessmentApi } from '../api/riskAssessment';
import type { RiskAssessmentRequest, RiskItemRequest } from '../types';

export function useRiskAssessments(productId: string) {
  return useQuery({
    queryKey: ['risk-assessments', productId],
    queryFn: () => riskAssessmentApi.list(productId),
    enabled: !!productId,
  });
}

export function useRiskAssessment(productId: string, assessmentId: string) {
  return useQuery({
    queryKey: ['risk-assessment', productId, assessmentId],
    queryFn: () => riskAssessmentApi.getById(productId, assessmentId),
    enabled: !!productId && !!assessmentId,
  });
}

export function useCreateRiskAssessment(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: RiskAssessmentRequest) => riskAssessmentApi.create(productId, req),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['risk-assessments', productId] }),
  });
}

export function useAddRiskItem(productId: string, assessmentId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (req: RiskItemRequest) => riskAssessmentApi.addItem(productId, assessmentId, req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['risk-assessments', productId] });
      qc.invalidateQueries({ queryKey: ['risk-assessment', productId, assessmentId] });
    },
  });
}

export function useSubmitRiskReview(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (assessmentId: string) => riskAssessmentApi.submitForReview(productId, assessmentId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['risk-assessments', productId] }),
  });
}

export function useApproveRisk(productId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (assessmentId: string) => riskAssessmentApi.approve(productId, assessmentId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['risk-assessments', productId] }),
  });
}
