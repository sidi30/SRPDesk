import apiClient from './client';
import type { RiskAssessment, RiskAssessmentRequest, RiskItemRequest } from '../types';

export const riskAssessmentApi = {
  list: async (productId: string): Promise<RiskAssessment[]> => {
    const { data } = await apiClient.get(`/products/${productId}/risk-assessments`);
    return data;
  },
  getById: async (productId: string, assessmentId: string): Promise<RiskAssessment> => {
    const { data } = await apiClient.get(`/products/${productId}/risk-assessments/${assessmentId}`);
    return data;
  },
  create: async (productId: string, req: RiskAssessmentRequest): Promise<RiskAssessment> => {
    const { data } = await apiClient.post(`/products/${productId}/risk-assessments`, req);
    return data;
  },
  addItem: async (productId: string, assessmentId: string, req: RiskItemRequest): Promise<RiskAssessment> => {
    const { data } = await apiClient.post(`/products/${productId}/risk-assessments/${assessmentId}/items`, req);
    return data;
  },
  submitForReview: async (productId: string, assessmentId: string): Promise<RiskAssessment> => {
    const { data } = await apiClient.post(`/products/${productId}/risk-assessments/${assessmentId}/submit-review`);
    return data;
  },
  approve: async (productId: string, assessmentId: string): Promise<RiskAssessment> => {
    const { data } = await apiClient.post(`/products/${productId}/risk-assessments/${assessmentId}/approve`);
    return data;
  },
};
