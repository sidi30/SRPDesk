import apiClient from './client';
import type { ApiKeyCreateRequest, ApiKeyCreateResponse, ApiKeyResponse } from '../types';

export const apiKeysApi = {
  list: async (): Promise<ApiKeyResponse[]> => {
    const { data } = await apiClient.get('/api-keys');
    return data;
  },

  create: async (req: ApiKeyCreateRequest): Promise<ApiKeyCreateResponse> => {
    const { data } = await apiClient.post('/api-keys', req);
    return data;
  },

  revoke: async (id: string): Promise<void> => {
    await apiClient.delete(`/api-keys/${id}`);
  },
};
