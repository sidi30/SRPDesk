import apiClient from './client';
import type { ConformityAssessment } from '../types';

export const conformityAssessmentApi = {
  get: async (productId: string, module: string): Promise<ConformityAssessment> => {
    const { data } = await apiClient.get(`/products/${productId}/conformity-assessment`, { params: { module } });
    return data;
  },
  getById: async (productId: string, assessmentId: string): Promise<ConformityAssessment> => {
    const { data } = await apiClient.get(`/products/${productId}/conformity-assessment/${assessmentId}`);
    return data;
  },
  initiate: async (productId: string, module: string): Promise<ConformityAssessment> => {
    const { data } = await apiClient.post(`/products/${productId}/conformity-assessment/initiate`, null, { params: { module } });
    return data;
  },
  completeStep: async (productId: string, assessmentId: string, stepIndex: number, notes?: string): Promise<ConformityAssessment> => {
    const { data } = await apiClient.post(
      `/products/${productId}/conformity-assessment/${assessmentId}/steps/${stepIndex}/complete`,
      notes ? { notes } : {}
    );
    return data;
  },
  approve: async (productId: string, assessmentId: string): Promise<ConformityAssessment> => {
    const { data } = await apiClient.post(`/products/${productId}/conformity-assessment/${assessmentId}/approve`);
    return data;
  },
};
