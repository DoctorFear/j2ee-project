import { tripStatisticsApi } from "@/features/admin/transport/trip-statistics/api";
import { tripsApi } from "@/features/admin/transport/trips/api";
// Hiển thị snapshot thống kê có thời gian cập nhật mới nhất.
import { Link } from "react-router-dom";
import { DataTable } from "@/components/shared/DataTable";
import { EmptyState, ErrorState, LoadingState } from "@/components/shared/AsyncState";
import { EntityValue } from "@/features/admin/transport/shared/components/EntityValue";
import { useEntityList } from "@/features/admin/transport/shared/hooks/useEntityList";
import type { TripStatistic } from "@/features/admin/transport/types";

export function recentStatistics(statistics: TripStatistic[]) {
  return statistics
    .filter((s) => s.updatedAt !== null)
    .sort((a, b) => b.updatedAt!.localeCompare(a.updatedAt!))
    .slice(0, 5);
}
export function RecentStatistics() {
  const query = useEntityList(tripStatisticsApi);
  const records = recentStatistics(query.data ?? []);
  return (
    <section className="admin-panel mb-6" aria-labelledby="recent-statistics-title">
      <div className="admin-panel-heading">
        <div>
          <h2 id="recent-statistics-title">Thống kê cập nhật gần đây</h2>
          <p>Năm bản ghi có thời gian cập nhật mới nhất</p>
        </div>
        <Link className="text-xs font-medium text-primary" to="/admin/trip-statistics">
          Tất cả thống kê →
        </Link>
      </div>
      {query.isPending ? (
        <LoadingState />
      ) : query.isError ? (
        <ErrorState error={query.error} retry={() => void query.refetch()} />
      ) : records.length === 0 ? (
        <EmptyState
          message="Chưa có thống kê đã cập nhật"
          detail="Các bản ghi chưa có thời gian cập nhật sẽ không xuất hiện tại đây."
        />
      ) : (
        <DataTable
          caption="Thống kê cập nhật gần đây"
          rows={records}
          rowKey={(s) => s.tripId}
          columns={[
            {
              key: "tripId",
              label: "Chuyến xe",
              render: (s) => <EntityValue field="tripId" value={s.tripId} relation={tripsApi} />,
            },
            { key: "validTicketCount", label: "Vé hợp lệ", render: (s) => s.validTicketCount },
            {
              key: "collectedAmount",
              label: "Tiền đã thu",
              render: (s) => <EntityValue field="collectedAmount" value={s.collectedAmount} />,
            },
            {
              key: "refundedAmount",
              label: "Tiền hoàn",
              render: (s) => <EntityValue field="refundedAmount" value={s.refundedAmount} />,
            },
            {
              key: "updatedAt",
              label: "Cập nhật lúc",
              render: (s) => <EntityValue field="updatedAt" value={s.updatedAt} />,
            },
          ]}
          actions={(s) => (
            <Link className="text-xs font-medium text-primary" to={`/admin/trip-statistics/${s.tripId}`}>
              Chi tiết
            </Link>
          )}
        />
      )}
    </section>
  );
}
