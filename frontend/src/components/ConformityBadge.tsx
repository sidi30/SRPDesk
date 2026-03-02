import { FR } from '../i18n/fr';

type Status = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'APPROVED';

const config: Record<Status, { label: string; bg: string; text: string }> = {
  NOT_STARTED: { label: FR.conformityStatus.NOT_STARTED, bg: 'bg-gray-100', text: 'text-gray-600' },
  IN_PROGRESS: { label: FR.conformityStatus.IN_PROGRESS, bg: 'bg-blue-100', text: 'text-blue-800' },
  COMPLETED:   { label: FR.conformityStatus.COMPLETED,   bg: 'bg-green-100', text: 'text-green-800' },
  APPROVED:    { label: FR.conformityStatus.APPROVED,     bg: 'bg-green-100', text: 'text-green-800' },
};

export function ConformityBadge({ status }: { status?: string }) {
  if (!status) return null;
  const c = config[status as Status];
  if (!c) return null;
  return (
    <span className={`inline-flex items-center gap-1 px-2 py-0.5 text-xs font-medium rounded-full ${c.bg} ${c.text}`}>
      {status === 'APPROVED' && <span>&#10003;</span>}
      {c.label}
    </span>
  );
}
