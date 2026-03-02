import { apiClient } from './client';
import type { CiPolicy, CiPolicyRequest } from '../types';

export const ciPolicyApi = {
  get: () => apiClient.get<CiPolicy>('/ci-policy').then(r => r.data),
  upsert: (req: CiPolicyRequest) => apiClient.put<CiPolicy>('/ci-policy', req).then(r => r.data),
};
