import apiClient from './client';
import type { Release, ReleaseCreateRequest } from '../types';

export const releasesApi = {
  listByProduct: async (productId: string): Promise<Release[]> => {
    const { data } = await apiClient.get(`/products/${productId}/releases`);
    return data;
  },
  create: async (productId: string, req: ReleaseCreateRequest): Promise<Release> => {
    const { data } = await apiClient.post(`/products/${productId}/releases`, req);
    return data;
  },
  exportPack: async (releaseId: string): Promise<void> => {
    const response = await apiClient.get(`/releases/${releaseId}/export`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `compliance-pack-${releaseId}.zip`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};
