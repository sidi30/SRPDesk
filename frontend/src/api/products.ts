import apiClient from './client';
import type { Product, ProductCreateRequest, ProductUpdateRequest } from '../types';

export const productsApi = {
  list: async (): Promise<Product[]> => {
    const { data } = await apiClient.get('/products');
    return data;
  },
  get: async (id: string): Promise<Product> => {
    const { data } = await apiClient.get(`/products/${id}`);
    return data;
  },
  create: async (req: ProductCreateRequest): Promise<Product> => {
    const { data } = await apiClient.post('/products', req);
    return data;
  },
  update: async (id: string, req: ProductUpdateRequest): Promise<Product> => {
    const { data } = await apiClient.put(`/products/${id}`, req);
    return data;
  },
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/products/${id}`);
  },
};
