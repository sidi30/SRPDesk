import apiClient from './client';
import type { SecurityAdvisoryCreateRequest, SecurityAdvisoryResponse } from '../types';

export const advisoriesApi = {
  list: async (): Promise<SecurityAdvisoryResponse[]> => {
    const { data } = await apiClient.get('/security-advisories');
    return data;
  },

  get: async (id: string): Promise<SecurityAdvisoryResponse> => {
    const { data } = await apiClient.get(`/security-advisories/${id}`);
    return data;
  },

  create: async (req: SecurityAdvisoryCreateRequest): Promise<SecurityAdvisoryResponse> => {
    const { data } = await apiClient.post('/security-advisories', req);
    return data;
  },

  publish: async (id: string): Promise<SecurityAdvisoryResponse> => {
    const { data } = await apiClient.post(`/security-advisories/${id}/publish`);
    return data;
  },

  notifyUsers: async (id: string, recipients: string[]): Promise<SecurityAdvisoryResponse> => {
    const { data } = await apiClient.post(`/security-advisories/${id}/notify`, recipients);
    return data;
  },
};
