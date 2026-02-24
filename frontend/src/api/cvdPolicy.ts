import apiClient from './client';
import type { CvdPolicy, CvdPolicyRequest } from '../types';

export const cvdPolicyApi = {
  get: async (productId: string): Promise<CvdPolicy> => {
    const { data } = await apiClient.get(`/products/${productId}/cvd-policy`);
    return data;
  },

  createOrUpdate: async (productId: string, req: CvdPolicyRequest): Promise<CvdPolicy> => {
    const { data } = await apiClient.put(`/products/${productId}/cvd-policy`, req);
    return data;
  },

  publish: async (productId: string): Promise<CvdPolicy> => {
    const { data } = await apiClient.post(`/products/${productId}/cvd-policy/publish`);
    return data;
  },
};
