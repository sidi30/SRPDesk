import apiClient from './client';
import type { AuditVerifyResponse, AuditEvent } from '../types';

export const auditApi = {
  verify: async (): Promise<AuditVerifyResponse> => {
    const { data } = await apiClient.get('/audit/verify');
    return data;
  },

  getEvents: async (entityType?: string): Promise<AuditEvent[]> => {
    const { data } = await apiClient.get('/audit/events', {
      params: entityType ? { entityType } : undefined,
    });
    return data;
  },

};
