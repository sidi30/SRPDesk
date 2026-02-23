import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { advisoriesApi } from '../api/advisories';
import type { SecurityAdvisoryCreateRequest } from '../types';

export function useAdvisories() {
  return useQuery({
    queryKey: ['security-advisories'],
    queryFn: advisoriesApi.list,
  });
}

export function useAdvisory(id: string) {
  return useQuery({
    queryKey: ['security-advisories', id],
    queryFn: () => advisoriesApi.get(id),
    enabled: !!id,
  });
}

export function useCreateAdvisory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: SecurityAdvisoryCreateRequest) => advisoriesApi.create(req),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['security-advisories'] }),
  });
}

export function usePublishAdvisory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => advisoriesApi.publish(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['security-advisories'] }),
  });
}

export function useNotifyAdvisoryUsers() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, recipients }: { id: string; recipients: string[] }) =>
      advisoriesApi.notifyUsers(id, recipients),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['security-advisories'] }),
  });
}
