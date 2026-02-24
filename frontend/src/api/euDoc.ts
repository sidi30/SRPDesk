import apiClient from './client';
import type { EuDeclarationOfConformity, EuDocRequest } from '../types';

export const euDocApi = {
  list: async (productId: string): Promise<EuDeclarationOfConformity[]> => {
    const { data } = await apiClient.get(`/products/${productId}/eu-doc`);
    return data;
  },
  getById: async (productId: string, docId: string): Promise<EuDeclarationOfConformity> => {
    const { data } = await apiClient.get(`/products/${productId}/eu-doc/${docId}`);
    return data;
  },
  create: async (productId: string, req: EuDocRequest): Promise<EuDeclarationOfConformity> => {
    const { data } = await apiClient.post(`/products/${productId}/eu-doc`, req);
    return data;
  },
  update: async (productId: string, docId: string, req: EuDocRequest): Promise<EuDeclarationOfConformity> => {
    const { data } = await apiClient.put(`/products/${productId}/eu-doc/${docId}`, req);
    return data;
  },
  sign: async (productId: string, docId: string): Promise<EuDeclarationOfConformity> => {
    const { data } = await apiClient.post(`/products/${productId}/eu-doc/${docId}/sign`);
    return data;
  },
  publish: async (productId: string, docId: string): Promise<EuDeclarationOfConformity> => {
    const { data } = await apiClient.post(`/products/${productId}/eu-doc/${docId}/publish`);
    return data;
  },
};
