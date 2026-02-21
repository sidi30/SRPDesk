import apiClient from './client';
import type { Evidence, SbomUploadResponse, ComponentItem } from '../types';

export const evidencesApi = {
  listByRelease: async (releaseId: string): Promise<Evidence[]> => {
    const { data } = await apiClient.get(`/releases/${releaseId}/evidences`);
    return data;
  },
  upload: async (releaseId: string, file: File, type: string): Promise<Evidence> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    const { data } = await apiClient.post(`/releases/${releaseId}/evidences`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data;
  },
  download: async (evidenceId: string, filename: string): Promise<void> => {
    const response = await apiClient.get(`/evidences/${evidenceId}/download`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },
  uploadSbom: async (releaseId: string, file: File): Promise<SbomUploadResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    const { data } = await apiClient.post(`/releases/${releaseId}/sbom`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data;
  },
  getComponents: async (releaseId: string): Promise<ComponentItem[]> => {
    const { data } = await apiClient.get(`/releases/${releaseId}/components`);
    return data;
  },
};
