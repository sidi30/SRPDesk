import apiClient from './client';
import type { VulnerabilityReportResponse, VulnerabilityReportTriageRequest } from '../types';

export const cvdReportsApi = {
  list: async (status?: string): Promise<VulnerabilityReportResponse[]> => {
    const params = status ? { status } : {};
    const { data } = await apiClient.get('/cvd/reports', { params });
    return data;
  },
  getById: async (id: string): Promise<VulnerabilityReportResponse> => {
    const { data } = await apiClient.get(`/cvd/reports/${id}`);
    return data;
  },
  triage: async (id: string, req: VulnerabilityReportTriageRequest): Promise<VulnerabilityReportResponse> => {
    const { data } = await apiClient.put(`/cvd/reports/${id}/triage`, req);
    return data;
  },
  countNew: async (): Promise<number> => {
    const { data } = await apiClient.get('/cvd/reports/count/new');
    return data.count;
  },
};
