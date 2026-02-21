import { useState, useEffect } from 'react';
import type { SlaDeadline } from '../types';

interface SlaCountdownProps {
  label: string;
  deadline: SlaDeadline | null;
}

export function SlaCountdown({ label, deadline }: SlaCountdownProps) {
  const [remaining, setRemaining] = useState(deadline?.remainingSeconds ?? 0);

  useEffect(() => {
    if (!deadline) return;
    setRemaining(deadline.remainingSeconds);
    const interval = setInterval(() => setRemaining((r) => r - 1), 1000);
    return () => clearInterval(interval);
  }, [deadline]);

  if (!deadline) {
    return (
      <div className="text-center p-3">
        <p className="text-xs text-gray-500 font-medium uppercase">{label}</p>
        <p className="text-sm text-gray-400 mt-1">Not yet determined</p>
      </div>
    );
  }

  const isOverdue = remaining < 0;
  const abs = Math.abs(remaining);
  const days = Math.floor(abs / 86400);
  const hours = Math.floor((abs % 86400) / 3600);
  const minutes = Math.floor((abs % 3600) / 60);
  const secs = abs % 60;

  const isUrgent = !isOverdue && remaining < 7200; // < 2h

  let bgColor = 'bg-green-50 border-green-200';
  let textColor = 'text-green-700';
  if (isOverdue) {
    bgColor = 'bg-red-50 border-red-200';
    textColor = 'text-red-700';
  } else if (isUrgent) {
    bgColor = 'bg-orange-50 border-orange-200';
    textColor = 'text-orange-700';
  } else if (remaining < 21600) { // < 6h
    bgColor = 'bg-yellow-50 border-yellow-200';
    textColor = 'text-yellow-700';
  }

  const timeStr = days > 0
    ? `${days}d ${hours}h ${minutes}m`
    : `${hours}h ${String(minutes).padStart(2, '0')}m ${String(secs).padStart(2, '0')}s`;

  return (
    <div className={`text-center p-3 rounded-lg border ${bgColor}`}>
      <p className="text-xs text-gray-500 font-medium uppercase">{label}</p>
      <p className={`text-lg font-mono font-bold mt-1 ${textColor}`}>
        {isOverdue ? '- ' : ''}{timeStr}
      </p>
      <p className="text-xs text-gray-400 mt-0.5">
        {isOverdue ? 'OVERDUE' : 'remaining'}
      </p>
      <p className="text-[10px] text-gray-400">
        Due: {new Date(deadline.dueAt).toLocaleString()}
      </p>
    </div>
  );
}
