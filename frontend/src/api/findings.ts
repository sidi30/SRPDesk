import apiClient from './client';
import type { Finding, FindingDecisionRequest, FindingDecision } from '../types';

export const findingsApi = {
  listByProduct: async (productId: string, status?: string): Promise<Finding[]> => {
    const params = status ? { status } : {};
    const { data } = await apiClient.get(`/products/${productId}/findings`, { params });
    return data;
  },
  listByRelease: async (releaseId: string, status?: string): Promise<Finding[]> => {
    const params = status ? { status } : {};
    const { data } = await apiClient.get(`/releases/${releaseId}/findings`, { params });
    return data;
  },
  addDecision: async (findingId: string, req: FindingDecisionRequest): Promise<FindingDecision> => {
    const { data } = await apiClient.post(`/findings/${findingId}/decisions`, req);
    return data;
  },
  triggerScan: async (releaseId: string): Promise<{ newFindings: number }> => {
    const { data } = await apiClient.post(`/releases/${releaseId}/scan`);
    return data;
  },
};
