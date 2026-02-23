import apiClient from './client';
import type { CraChecklistItem, CraChecklistUpdateRequest, CraChecklistSummary } from '../types';

export const craChecklistApi = {
  initialize: async (productId: string): Promise<CraChecklistItem[]> => {
    const { data } = await apiClient.post(`/products/${productId}/cra-checklist/initialize`);
    return data;
  },

  list: async (productId: string): Promise<CraChecklistItem[]> => {
    const { data } = await apiClient.get(`/products/${productId}/cra-checklist`);
    return data;
  },

  summary: async (productId: string): Promise<CraChecklistSummary> => {
    const { data } = await apiClient.get(`/products/${productId}/cra-checklist/summary`);
    return data;
  },

  update: async (productId: string, itemId: string, req: CraChecklistUpdateRequest): Promise<CraChecklistItem> => {
    const { data } = await apiClient.put(`/products/${productId}/cra-checklist/${itemId}`, req);
    return data;
  },

  linkEvidence: async (productId: string, itemId: string, evidenceId: string): Promise<CraChecklistItem> => {
    const { data } = await apiClient.post(`/products/${productId}/cra-checklist/${itemId}/evidences/${evidenceId}`);
    return data;
  },
};
