import { BarChart, Bar, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts";
import { formatMoney } from "@/features/admin/transport/shared/format/format-money";
import type { aggregateStatistics } from "@/features/admin/dashboard/aggregate";

export function RevenueChart({ days }: { days: ReturnType<typeof aggregateStatistics>["days"] }) {
  return (
    <div className="h-72 px-4 pb-4" role="img" aria-label="Biểu đồ số tiền thu và hoàn theo ngày khởi hành">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={days} margin={{ top: 15, right: 12, left: 8 }}>
          <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--border)" />
          <XAxis
            dataKey="date"
            tickFormatter={(date) => date.slice(5).split("-").reverse().join("/")}
            tick={{ fill: "var(--muted-foreground)", fontSize: 11 }}
            axisLine={false}
            tickLine={false}
          />
          <YAxis
            tickFormatter={(value) => new Intl.NumberFormat("vi-VN", { notation: "compact" }).format(value)}
            tick={{ fill: "var(--muted-foreground)", fontSize: 11 }}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip
            contentStyle={{
              background: "var(--card)",
              border: "1px solid var(--border)",
              borderRadius: 8,
            }}
            formatter={(value) => formatMoney(Number(value))}
          />
          <Bar
            isAnimationActive={false}
            name="Tiền đã thu"
            dataKey="collected"
            fill="#ef7647"
            radius={[4, 4, 0, 0]}
            maxBarSize={35}
          />
          <Bar
            isAnimationActive={false}
            name="Tiền hoàn"
            dataKey="refunded"
            fill="#90b5ac"
            radius={[4, 4, 0, 0]}
            maxBarSize={35}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
