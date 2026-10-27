import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from "recharts";
import type { TripStatusGroup } from "@/features/admin/dashboard/trip-overview";

export function TripStatusChart({ groups, total }: { groups: TripStatusGroup[]; total: number }) {
  return (
    <>
      <div className="relative h-48" role="img" aria-label={groups.map((s) => `${s.name}: ${s.value}`).join(", ")}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              isAnimationActive={false}
              data={groups}
              dataKey="value"
              nameKey="name"
              innerRadius={60}
              outerRadius={80}
              paddingAngle={4}
              stroke="none"
            >
              {groups.map((s) => (
                <Cell key={s.status} fill={s.color} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{
                background: "var(--card)",
                border: "1px solid var(--border)",
                borderRadius: 8,
              }}
            />
          </PieChart>
        </ResponsiveContainer>
        <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
          <strong className="text-3xl">{total}</strong>
          <span className="text-xs text-muted-foreground">chuyến xe</span>
        </div>
      </div>
      <div className="grid grid-cols-2 gap-3 px-6 pb-6">
        {groups.map((s) => (
          <div key={s.status} className="flex items-center justify-between gap-2 text-xs">
            <span className="flex items-center gap-2 text-muted-foreground">
              <i className="h-2 w-2 rounded-full" style={{ background: s.color }} />
              {s.name}
            </span>
            <strong>{s.value}</strong>
          </div>
        ))}
      </div>
    </>
  );
}
