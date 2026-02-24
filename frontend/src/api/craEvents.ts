import apiClient from './client';
import type {
  CraEvent, CraEventCreateRequest, CraEventUpdateRequest,
  CraEventParticipant, SlaResponse, SrpSubmission,
  SrpSubmissionCreateRequest, MarkSubmittedRequest,
} from '../types';

export const craEventsApi = {
  list: async (productId?: string, status?: string): Promise<CraEvent[]> => {
    const params = new URLSearchParams();
    if (productId) params.set('productId', productId);
    if (status) params.set('status', status);
    const { data } = await apiClient.get(`/cra-events?${params}`);
    return data;
  },

  get: async (id: string): Promise<CraEvent> => {
    const { data } = await apiClient.get(`/cra-events/${id}`);
    return data;
  },

  create: async (req: CraEventCreateRequest): Promise<CraEvent> => {
    const { data } = await apiClient.post('/cra-events', req);
    return data;
  },

  update: async (id: string, req: CraEventUpdateRequest): Promise<CraEvent> => {
    const { data } = await apiClient.patch(`/cra-events/${id}`, req);
    return data;
  },

  close: async (id: string): Promise<CraEvent> => {
    const { data } = await apiClient.post(`/cra-events/${id}/close`);
    return data;
  },

  addLinks: async (id: string, body: { releaseIds?: string[]; findingIds?: string[]; evidenceIds?: string[] }): Promise<void> => {
    await apiClient.post(`/cra-events/${id}/links`, body);
  },

  addParticipant: async (id: string, userId: string, role: string): Promise<CraEventParticipant> => {
    const { data } = await apiClient.post(`/cra-events/${id}/participants`, { userId, role });
    return data;
  },

  getSla: async (id: string): Promise<SlaResponse> => {
    const { data } = await apiClient.get(`/cra-events/${id}/sla`);
    return data;
  },

  // Submissions
  listSubmissions: async (eventId: string): Promise<SrpSubmission[]> => {
    const { data } = await apiClient.get(`/cra-events/${eventId}/submissions`);
    return data;
  },

  createSubmission: async (eventId: string, req: SrpSubmissionCreateRequest): Promise<SrpSubmission> => {
    const { data } = await apiClient.post(`/cra-events/${eventId}/submissions`, req);
    return data;
  },

  validateSubmission: async (eventId: string, subId: string): Promise<SrpSubmission> => {
    const { data } = await apiClient.post(`/cra-events/${eventId}/submissions/${subId}/validate`);
    return data;
  },

  markReady: async (eventId: string, subId: string): Promise<SrpSubmission> => {
    const { data } = await apiClient.post(`/cra-events/${eventId}/submissions/${subId}/mark-ready`);
    return data;
  },

  exportBundle: async (eventId: string, subId: string): Promise<void> => {
    const response = await apiClient.get(`/cra-events/${eventId}/submissions/${subId}/export`, {
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `srp_bundle_${subId}.zip`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  markSubmitted: async (eventId: string, subId: string, req: MarkSubmittedRequest, ackEvidenceId?: string): Promise<SrpSubmission> => {
    const params = ackEvidenceId ? `?ackEvidenceId=${ackEvidenceId}` : '';
    const { data } = await apiClient.post(`/cra-events/${eventId}/submissions/${subId}/mark-submitted${params}`, req);
    return data;
  },

  submitParallel: async (eventId: string, subId: string, csirtCountryCode?: string): Promise<SrpSubmission> => {
    const params = csirtCountryCode ? `?csirtCountryCode=${csirtCountryCode}` : '';
    const { data } = await apiClient.post(`/cra-events/${eventId}/submissions/${subId}/submit-parallel${params}`);
    return data;
  },
};
