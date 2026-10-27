import type { ReactNode } from "react";

export function StatCard({
  label,
  value,
  detail,
  icon,
  color,
}: {
  label: string;
  value: string;
  detail: string;
  icon: ReactNode;
  color: string;
}) {
  return (
    <div className="admin-panel admin-stat">
      <div className="flex items-center justify-between gap-2">
        <span className="text-xs font-medium text-muted-foreground">{label}</span>
        <span className={`grid h-9 w-9 place-items-center rounded-lg ${color}`}>{icon}</span>
      </div>
      <div className="admin-stat-value">{value}</div>
      <p className="m-0 text-xs text-muted-foreground">{detail}</p>
    </div>
  );
}
