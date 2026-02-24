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
  acknowledge: async (id: string): Promise<VulnerabilityReportResponse> => {
    const { data } = await apiClient.post(`/cvd/reports/${id}/acknowledge`);
    return data;
  },
  startTriage: async (id: string): Promise<VulnerabilityReportResponse> => {
    const { data } = await apiClient.post(`/cvd/reports/${id}/start-triage`);
    return data;
  },
  confirm: async (id: string): Promise<VulnerabilityReportResponse> => {
    const { data } = await apiClient.post(`/cvd/reports/${id}/confirm`);
    return data;
  },
  reject: async (id: string, reason?: string): Promise<VulnerabilityReportResponse> => {
    const { data } = await apiClient.post(`/cvd/reports/${id}/reject`, reason ? { reason } : {});
    return data;
  },
  startFix: async (id: string): Promise<VulnerabilityReportResponse> => {
    const { data } = await apiClient.post(`/cvd/reports/${id}/start-fix`);
    return data;
  },
  markFixed: async (id: string): Promise<VulnerabilityReportResponse> => {
    const { data } = await apiClient.post(`/cvd/reports/${id}/mark-fixed`);
    return data;
  },
  disclose: async (id: string): Promise<VulnerabilityReportResponse> => {
    const { data } = await apiClient.post(`/cvd/reports/${id}/disclose`);
    return data;
  },
  countNew: async (): Promise<number> => {
    const { data } = await apiClient.get('/cvd/reports/count/new');
    return data.count;
  },
};
