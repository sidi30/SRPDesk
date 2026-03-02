type Freshness = 'FRESH' | 'STALE' | 'OUTDATED' | 'NONE';

const config: Record<Freshness, { label: string; bg: string; text: string }> = {
  FRESH:    { label: 'SBOM a jour',   bg: 'bg-green-100',  text: 'text-green-800' },
  STALE:    { label: 'SBOM ancien',   bg: 'bg-yellow-100', text: 'text-yellow-800' },
  OUTDATED: { label: 'SBOM obsolete', bg: 'bg-red-100',    text: 'text-red-800' },
  NONE:     { label: 'Pas de SBOM',   bg: 'bg-gray-100',   text: 'text-gray-500' },
};

export function SbomFreshnessBadge({ freshness }: { freshness?: Freshness }) {
  const f = freshness ?? 'NONE';
  const c = config[f];
  return (
    <span className={`inline-flex px-2 py-0.5 text-xs font-medium rounded-full ${c.bg} ${c.text}`}>
      {c.label}
    </span>
  );
}
