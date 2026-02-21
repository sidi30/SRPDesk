import apiClient from './client';
import type { AiJobResponse } from '../types';

export const aiApi = {
  generateSrpDraft: async (craEventId: string, submissionType: string): Promise<AiJobResponse> => {
    const { data } = await apiClient.post('/ai/srp-draft', { craEventId, submissionType });
    return data;
  },

  generateCommPack: async (craEventId: string): Promise<AiJobResponse> => {
    const { data } = await apiClient.post('/ai/comm-pack', { craEventId });
    return data;
  },

  parseQuestionnaire: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);
    const { data } = await apiClient.post('/ai/questionnaire/parse', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data;
  },

  fillQuestionnaire: async (questionnaireText: string, productId?: string): Promise<AiJobResponse> => {
    const { data } = await apiClient.post('/ai/questionnaire/fill', { questionnaireText, productId });
    return data;
  },

  getJob: async (id: string): Promise<AiJobResponse> => {
    const { data } = await apiClient.get(`/ai/jobs/${id}`);
    return data;
  },
};
