import apiClient from './client';
import type { AppliedStandard, AppliedStandardRequest } from '../types';

export const appliedStandardsApi = {
  list: async (productId: string): Promise<AppliedStandard[]> => {
    const { data } = await apiClient.get(`/products/${productId}/standards`);
    return data;
  },
  create: async (productId: string, req: AppliedStandardRequest): Promise<AppliedStandard> => {
    const { data } = await apiClient.post(`/products/${productId}/standards`, req);
    return data;
  },
  update: async (productId: string, standardId: string, req: AppliedStandardRequest): Promise<AppliedStandard> => {
    const { data } = await apiClient.put(`/products/${productId}/standards/${standardId}`, req);
    return data;
  },
  delete: async (productId: string, standardId: string): Promise<void> => {
    await apiClient.delete(`/products/${productId}/standards/${standardId}`);
  },
};
