import { FR } from '../i18n/fr';

type Status = 'DRAFT' | 'SIGNED' | 'PUBLISHED';

const config: Record<Status, { bg: string; text: string }> = {
  DRAFT:     { bg: 'bg-gray-100', text: 'text-gray-600' },
  SIGNED:    { bg: 'bg-blue-100', text: 'text-blue-800' },
  PUBLISHED: { bg: 'bg-green-100', text: 'text-green-800' },
};

export function EuDocBadge({ status }: { status?: string }) {
  if (!status) return null;
  const c = config[status as Status];
  if (!c) return null;
  return (
    <span className={`inline-flex px-2 py-0.5 text-xs font-medium rounded-full ${c.bg} ${c.text}`}>
      DoC: {FR.euDocStatus[status] || status}
    </span>
  );
}
