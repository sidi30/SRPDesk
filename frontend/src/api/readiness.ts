import apiClient from './client';
import type { ReadinessScore } from '../types';

export const readinessApi = {
  getScore: async (productId: string): Promise<ReadinessScore> => {
    const { data } = await apiClient.get(`/products/${productId}/readiness`);
    return data;
  },

  snapshot: async (productId: string): Promise<ReadinessScore> => {
    const { data } = await apiClient.post(`/products/${productId}/readiness/snapshot`);
    return data;
  },
};
