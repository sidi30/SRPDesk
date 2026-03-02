export function AutomationGauge({ score }: { score: number }) {
  const radius = 36;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (score / 100) * circumference;
  const color = score >= 80 ? '#22c55e' : score >= 60 ? '#eab308' : score >= 40 ? '#f97316' : '#ef4444';

  return (
    <div className="flex items-center gap-3">
      <svg width="80" height="80" viewBox="0 0 80 80">
        <circle
          cx="40" cy="40" r={radius}
          fill="none" stroke="#e5e7eb" strokeWidth="6"
        />
        <circle
          cx="40" cy="40" r={radius}
          fill="none" stroke={color} strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          transform="rotate(-90 40 40)"
          className="transition-all duration-700 ease-out"
        />
        <text x="40" y="44" textAnchor="middle" className="text-sm font-bold" fill={color}>
          {score}%
        </text>
      </svg>
    </div>
  );
}
