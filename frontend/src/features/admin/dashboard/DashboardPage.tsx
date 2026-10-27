import { routesApi } from "@/features/admin/transport/routes/api";
import { vehiclesApi } from "@/features/admin/transport/vehicles/api";
import { PageHeading } from "@/components/shared/PageHeading";
import { useDashboard } from "@/features/admin/dashboard/useDashboard";
import { StatCard } from "@/features/admin/dashboard/components/StatCard";
import { RevenueChart } from "@/features/admin/dashboard/components/RevenueChart";
import { TripStatusChart } from "@/features/admin/dashboard/components/TripStatusChart";
import { Link } from "react-router-dom";
import { ArrowRight, Bus, CalendarDays, CircleDollarSign, MapPin, Plus, RefreshCw, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { EmptyState, ErrorState, LoadingState } from "@/components/shared/AsyncState";
import { DataTable } from "@/components/shared/DataTable";
import { useAuthStore } from "@/features/auth";
import { EntityValue } from "@/features/admin/transport/shared/components/EntityValue";
import { formatMoney } from "@/features/admin/transport/shared/format/format-money";
import { RecentStatistics } from "@/features/admin/dashboard/RecentStatistics";

export default function DashboardPage() {
  const user = useAuthStore((state) => state.user);
  const {
    trips,
    vehicles,
    drivers,
    routes,
    statistics,
    queries,
    fetching,
    aggregate,
    statuses,
    futureTrips,
    lastSync,
    refresh,
  } = useDashboard();
  return (
    <>
      <PageHeading
        className="admin-page-heading"
        title={"Tổng quan vận hành"}
        description={<>Xin chào {user?.fullName || "quản trị viên"}, cùng theo dõi hoạt động vận tải hôm nay.</>}
        eyebrow={<div className="mb-1 text-xs font-medium text-primary">TRUNG TÂM ĐIỀU HÀNH</div>}
        actions={
          <Button variant="outline" disabled={fetching} onClick={() => void refresh()}>
            <RefreshCw size={15} className={fetching ? "animate-spin" : ""} />
            Làm mới dữ liệu
          </Button>
        }
      />
      <div className="admin-stat-grid">
        <StatCard
          label="Tổng số chuyến"
          value={trips.isSuccess ? String(trips.data.length) : "—"}
          detail={
            trips.isSuccess
              ? `${trips.data.filter((t) => t.status === "RUNNING").length} chuyến đang chạy`
              : "Chưa có dữ liệu chuyến"
          }
          icon={<CalendarDays size={18} />}
          color="bg-orange-500/10 text-orange-600"
        />
        <StatCard
          label="Phương tiện hoạt động"
          value={vehicles.isSuccess ? String(vehicles.data.filter((v) => v.status === "ACTIVE").length) : "—"}
          detail={
            vehicles.isSuccess ? `Trong tổng số ${vehicles.data.length} phương tiện` : "Chưa có dữ liệu phương tiện"
          }
          icon={<Bus size={18} />}
          color="bg-blue-500/10 text-blue-600"
        />
        <StatCard
          label="Đội ngũ tài xế"
          value={drivers.isSuccess ? String(drivers.data.length) : "—"}
          detail={
            drivers.isSuccess
              ? `${drivers.data.filter((d) => d.status === "ACTIVE").length} tài xế hoạt động`
              : "Chưa có dữ liệu tài xế"
          }
          icon={<Users size={18} />}
          color="bg-emerald-500/10 text-emerald-600"
        />
        <StatCard
          label="Tiền thu sau hoàn"
          value={statistics.isSuccess ? formatMoney(aggregate.net) : "—"}
          detail={
            statistics.isSuccess
              ? `${aggregate.tickets} vé hợp lệ · ${statistics.data.length} chuyến có thống kê`
              : "Chưa có dữ liệu đối soát"
          }
          icon={<CircleDollarSign size={18} />}
          color="bg-violet-500/10 text-violet-600"
        />
      </div>
      {queries.some((query) => query.isError) && (
        <div className="admin-panel mb-6">
          {queries.map((query, index) =>
            query.isError ? (
              <ErrorState
                key={index}
                error={
                  new Error(
                    `${["Chuyến xe", "Phương tiện", "Tài xế", "Tuyến vận tải", "Thống kê"][index]}: ${query.error.message}`
                  )
                }
                retry={() => void query.refetch()}
              />
            ) : null
          )}
        </div>
      )}
      <div className="admin-chart-grid">
        <section className="admin-panel">
          <div className="admin-panel-heading">
            <div>
              <h2>Thu & hoàn theo ngày khởi hành</h2>
              <p>Thống kê hiện tại của chuyến · tối đa 14 ngày có dữ liệu</p>
            </div>
            <Link to="/admin/trip-statistics" className="text-xs font-medium text-primary">
              Chi tiết <ArrowRight size={13} />
            </Link>
          </div>
          {trips.isPending || statistics.isPending ? (
            <LoadingState />
          ) : trips.isError || statistics.isError ? (
            <EmptyState message="Chưa tải được biểu đồ" detail="Thử tải lại dữ liệu thống kê và chuyến xe." />
          ) : !aggregate.days.length ? (
            <EmptyState message="Chưa có thống kê theo chuyến" detail="Biểu đồ sẽ xuất hiện khi có dữ liệu đối soát." />
          ) : (
            <RevenueChart days={aggregate.days} />
          )}
          <div className="flex justify-center gap-5 border-t py-3 text-xs text-muted-foreground">
            <span>
              <i className="mr-2 inline-block h-2 w-2 rounded-sm bg-[#ef7647]" />
              Tiền đã thu
            </span>
            <span>
              <i className="mr-2 inline-block h-2 w-2 rounded-sm bg-[#90b5ac]" />
              Tiền hoàn
            </span>
          </div>
        </section>
        <section className="admin-panel">
          <div className="admin-panel-heading">
            <div>
              <h2>Trạng thái chuyến xe</h2>
              <p>Phân bố trên toàn bộ lịch chuyến</p>
            </div>
          </div>
          {trips.isPending ? (
            <LoadingState />
          ) : trips.isError || !statuses.length ? (
            <EmptyState message="Chưa có dữ liệu trạng thái" detail="Thêm lịch chuyến để theo dõi vận hành." />
          ) : (
            <TripStatusChart groups={statuses} total={trips.data.length} />
          )}
        </section>
      </div>
      <section className="admin-panel mb-6">
        <div className="admin-panel-heading">
          <div>
            <h2>Lịch khởi hành sắp tới</h2>
            <p>Các chuyến có giờ khởi hành trong tương lai, chưa hoàn thành hoặc hủy</p>
          </div>
          <Button variant="outline" size="sm" asChild>
            <Link to="/admin/trips">
              Tất cả chuyến <ArrowRight size={13} />
            </Link>
          </Button>
        </div>
        {trips.isPending ? (
          <LoadingState />
        ) : trips.isError ? (
          <ErrorState error={trips.error} />
        ) : !futureTrips.length ? (
          <EmptyState message="Chưa có lịch khởi hành sắp tới" detail="Lập lịch chuyến mới tại mục Lịch chuyến." />
        ) : (
          <DataTable
            caption="Lịch khởi hành sắp tới"
            rows={futureTrips}
            rowKey={(trip) => trip.id}
            columns={[
              {
                key: "code",
                label: "Chuyến xe",
                render: (trip) => (
                  <Link className="font-semibold text-primary" to={`/admin/trips/${trip.id}`}>
                    {trip.code}
                  </Link>
                ),
              },
              {
                key: "routeId",
                label: "Tuyến vận tải",
                render: (trip) => <EntityValue field="routeId" value={trip.routeId} relation={routesApi} />,
              },
              {
                key: "vehicleId",
                label: "Phương tiện",
                render: (trip) => <EntityValue field="vehicleId" value={trip.vehicleId} relation={vehiclesApi} />,
              },
              {
                key: "departureAt",
                label: "Giờ khởi hành",
                render: (trip) => <EntityValue field="departureAt" value={trip.departureAt} />,
              },
              {
                key: "status",
                label: "Trạng thái",
                render: (trip) => <EntityValue field="status" value={trip.status} />,
              },
            ]}
          />
        )}
      </section>
      <RecentStatistics />
      <div className="grid gap-4 sm:grid-cols-3">
        {[
          {
            to: "routes",
            title: "Mạng lưới tuyến",
            detail: routes.isSuccess ? `${routes.data.length} tuyến vận tải` : "Quản lý tuyến đường",
            icon: <MapPin size={18} />,
          },
          {
            to: "trips",
            title: "Lập lịch chuyến",
            detail: "Tuyến đường & phương tiện",
            icon: <Plus size={18} />,
          },
          {
            to: "trip-drivers",
            title: "Điều phối tài xế",
            detail: "Phân công theo chuyến",
            icon: <Users size={18} />,
          },
        ].map((item) => (
          <Link
            key={item.to}
            to={`/admin/${item.to}`}
            className="admin-panel flex items-center gap-3 p-5 hover:border-primary/40"
          >
            <span className="rounded-lg bg-muted p-2.5 text-primary">{item.icon}</span>
            <div className="flex-1">
              <strong className="text-sm">{item.title}</strong>
              <p className="m-0 mt-1 text-xs text-muted-foreground">{item.detail}</p>
            </div>
            <ArrowRight size={15} className="text-muted-foreground" />
          </Link>
        ))}
      </div>
      <div className="mt-6 flex flex-wrap justify-between gap-2 text-xs text-muted-foreground">
        <span>FUTA Transport · Hệ thống quản trị vận tải</span>
        <span>
          {queries.every((q) => q.isSuccess) && Number.isFinite(lastSync)
            ? `Đồng bộ lúc ${new Date(lastSync).toLocaleTimeString("vi-VN", { timeZone: "Asia/Ho_Chi_Minh" })}`
            : "Đang chờ dữ liệu từ máy chủ"}
        </span>
      </div>
    </>
  );
}
