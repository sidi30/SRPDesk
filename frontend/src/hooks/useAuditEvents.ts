import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../api/audit';

export function useAuditVerify() {
  return useQuery({
    queryKey: ['audit-verify'],
    queryFn: auditApi.verify,
    refetchInterval: 60000,
  });
}

export function useAuditEvents(entityType?: string) {
  return useQuery({
    queryKey: ['audit-events', entityType],
    queryFn: () => auditApi.getEvents(entityType),
  });
}
