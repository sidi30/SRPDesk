import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { webhooksApi } from '../api/webhooks';
import type { WebhookCreateRequest } from '../types';

export function useWebhooks() {
  return useQuery({
    queryKey: ['webhooks'],
    queryFn: webhooksApi.list,
  });
}

export function useCreateWebhook() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: WebhookCreateRequest) => webhooksApi.create(req),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['webhooks'] }),
  });
}

export function useDeleteWebhook() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => webhooksApi.delete(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['webhooks'] }),
  });
}

export function useToggleWebhook() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
      webhooksApi.toggle(id, enabled),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['webhooks'] }),
  });
}
