import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { craEventsApi } from '../api/craEvents';
import type { CraEventCreateRequest, CraEventUpdateRequest, SrpSubmissionCreateRequest, MarkSubmittedRequest } from '../types';

export function useCraEvents(productId?: string, status?: string) {
  return useQuery({
    queryKey: ['cra-events', productId, status],
    queryFn: () => craEventsApi.list(productId, status),
  });
}

export function useCraEvent(id: string) {
  return useQuery({
    queryKey: ['cra-events', id],
    queryFn: () => craEventsApi.get(id),
    enabled: !!id,
  });
}

export function useCraEventSla(id: string) {
  return useQuery({
    queryKey: ['cra-events', id, 'sla'],
    queryFn: () => craEventsApi.getSla(id),
    enabled: !!id,
    refetchInterval: 60000,
  });
}

export function useCreateCraEvent() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CraEventCreateRequest) => craEventsApi.create(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['cra-events'] }),
  });
}

export function useUpdateCraEvent() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: CraEventUpdateRequest }) => craEventsApi.update(id, data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: ['cra-events'] });
      qc.invalidateQueries({ queryKey: ['cra-events', vars.id] });
    },
  });
}

export function useCloseCraEvent() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => craEventsApi.close(id),
    onSuccess: (_, id) => {
      qc.invalidateQueries({ queryKey: ['cra-events'] });
      qc.invalidateQueries({ queryKey: ['cra-events', id] });
    },
  });
}

export function useAddCraEventLinks() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: { releaseIds?: string[]; findingIds?: string[]; evidenceIds?: string[] } }) =>
      craEventsApi.addLinks(id, body),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cra-events', vars.id] }),
  });
}

export function useAddParticipant() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ eventId, userId, role }: { eventId: string; userId: string; role: string }) =>
      craEventsApi.addParticipant(eventId, userId, role),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cra-events', vars.eventId] }),
  });
}

// Submissions
export function useSubmissions(eventId: string) {
  return useQuery({
    queryKey: ['cra-events', eventId, 'submissions'],
    queryFn: () => craEventsApi.listSubmissions(eventId),
    enabled: !!eventId,
  });
}

export function useCreateSubmission() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ eventId, data }: { eventId: string; data: SrpSubmissionCreateRequest }) =>
      craEventsApi.createSubmission(eventId, data),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cra-events', vars.eventId, 'submissions'] }),
  });
}

export function useValidateSubmission() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ eventId, subId }: { eventId: string; subId: string }) =>
      craEventsApi.validateSubmission(eventId, subId),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cra-events', vars.eventId, 'submissions'] }),
  });
}

export function useMarkReady() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ eventId, subId }: { eventId: string; subId: string }) =>
      craEventsApi.markReady(eventId, subId),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cra-events', vars.eventId, 'submissions'] }),
  });
}

export function useExportBundle() {
  return useMutation({
    mutationFn: ({ eventId, subId }: { eventId: string; subId: string }) =>
      craEventsApi.exportBundle(eventId, subId),
  });
}

export function useMarkSubmitted() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ eventId, subId, data, ackEvidenceId }: {
      eventId: string; subId: string; data: MarkSubmittedRequest; ackEvidenceId?: string
    }) => craEventsApi.markSubmitted(eventId, subId, data, ackEvidenceId),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cra-events', vars.eventId, 'submissions'] }),
  });
}

export function useSubmitParallel() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ eventId, subId, csirtCountryCode }: {
      eventId: string; subId: string; csirtCountryCode?: string
    }) => craEventsApi.submitParallel(eventId, subId, csirtCountryCode),
    onSuccess: (_, vars) => qc.invalidateQueries({ queryKey: ['cra-events', vars.eventId, 'submissions'] }),
  });
}
