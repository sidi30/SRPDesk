import apiClient from './client';
import type { DashboardData } from '../types';

export const dashboardApi = {
  get: async (): Promise<DashboardData> => {
    const { data } = await apiClient.get('/dashboard');
    return data;
  },
};
