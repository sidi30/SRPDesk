import apiClient from './client';
import type { WebhookCreateRequest, WebhookResponse } from '../types';

export const webhooksApi = {
  list: async (): Promise<WebhookResponse[]> => {
    const { data } = await apiClient.get('/webhooks');
    return data;
  },

  create: async (req: WebhookCreateRequest): Promise<WebhookResponse> => {
    const { data } = await apiClient.post('/webhooks', req);
    return data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/webhooks/${id}`);
  },

  toggle: async (id: string, enabled: boolean): Promise<void> => {
    await apiClient.patch(`/webhooks/${id}/toggle?enabled=${enabled}`);
  },
};
