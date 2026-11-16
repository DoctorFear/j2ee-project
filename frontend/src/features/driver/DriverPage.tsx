// Màn hình 1: Lịch trình ca trực của tài xế (/driver/trips)
import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Calendar,
  Clock,
  MapPin,
  Users,
  QrCode,
  Route as RouteIcon,
  Play,
  CheckCircle2,
  AlertCircle,
  Truck,
  ChevronRight,
  RefreshCw,
} from "lucide-react";
import { useUser } from "@/features/auth";
import { sessionApi } from "@/features/auth/session-api";
import { DriverHeader } from "./components/DriverHeader";
import { RouteStopModal } from "./components/RouteStopModal";
import { driverApi } from "./api/driverApi";
import type { DriverProfile, DriverTrip, DriverTripStop } from "./types/driver";

export default function DriverPage() {
  const { user } = useUser();
  const navigate = useNavigate();

  const [selectedDate, setSelectedDate] = useState<string>(() => {
    return new Date().toISOString().split("T")[0];
  });
  const [driverProfile, setDriverProfile] = useState<DriverProfile | null>(null);
  const [trips, setTrips] = useState<DriverTrip[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Modal xem lộ trình trạm dừng
  const [selectedModalTrip, setSelectedModalTrip] = useState<{
    code: string;
    routeName: string;
    stops: DriverTripStop[];
  } | null>(null);

  // Tạo danh sách 5 ngày để tài xế cuộn ngang chọn
  const dateTabs = Array.from({ length: 5 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() + i);
    const dateStr = d.toISOString().split("T")[0];
    const label =
      i === 0
        ? "Hôm nay"
        : i === 1
        ? "Ngày mai"
        : d.toLocaleDateString("vi-VN", { weekday: "short", day: "numeric", month: "numeric" });
    return { dateStr, label, dayNumber: d.getDate() };
  });

  const loadTrips = async () => {
    setLoading(true);
    setError(null);
    try {
      let phone = user?.phoneNumber;
      if (!phone) {
        try {
          const profile = await sessionApi.getProfile();
          phone = profile.phoneNumber;
        } catch {
          // ignore
        }
      }

      const [profileData, tripsData] = await Promise.all([
        driverApi.getProfile(undefined, phone).catch(() => null),
        driverApi.getTrips(selectedDate, undefined, phone),
      ]);
      if (profileData && profileData.id) {
        setDriverProfile(profileData);
      }
      setTrips(tripsData || []);
    } catch (err: any) {
      console.error("Lỗi khi tải lịch ca trực:", err);
      setError("Không thể tải danh sách chuyến xe. Vui lòng kiểm tra lại kết nối.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTrips();
  }, [selectedDate, user?.phoneNumber]);

  const handleStartTrip = async (tripCode: string) => {
    if (!window.confirm(`Xác nhận bắt đầu xuất bến cho chuyến ${tripCode}?`)) return;
    try {
      await driverApi.updateTripStatus(tripCode, "RUNNING");
      await loadTrips();
    } catch (err: any) {
      alert("Lỗi khi cập nhật trạng thái chuyến: " + err.message);
    }
  };

  const handleCompleteTrip = async (tripCode: string) => {
    if (!window.confirm(`Xác nhận đã hoàn thành chuyến xe ${tripCode}?`)) return;
    try {
      await driverApi.updateTripStatus(tripCode, "COMPLETED");
      await loadTrips();
    } catch (err: any) {
      alert("Lỗi khi hoàn thành chuyến: " + err.message);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "RUNNING":
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-rose-50 text-rose-600 border border-rose-200 flex items-center gap-1 animate-pulse">
            <span className="w-2 h-2 rounded-full bg-rose-600"></span> Đang chạy
          </span>
        );
      case "COMPLETED":
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-600 border border-slate-200 flex items-center gap-1">
            <CheckCircle2 size={13} /> Đã hoàn thành
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-sky-50 text-sky-700 border border-sky-200 flex items-center gap-1">
            <Clock size={13} /> Sắp xuất bến
          </span>
        );
    }
  };

  const syncedDriverName =
    driverProfile?.fullName || trips[0]?.driverName || user?.fullName || "Tài xế FUTA";
  const syncedDriverRole = trips[0]?.driverRole || driverProfile?.role || "PRIMARY";
  const syncedDriverCode = driverProfile?.id
    ? `TX-${String(driverProfile.id).padStart(4, "0")}`
    : trips[0]?.driverId
    ? `TX-${String(trips[0].driverId).padStart(4, "0")}`
    : undefined;

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col pb-12 font-sans antialiased">
      {/* Header riêng tài xế */}
      <DriverHeader
        title="Lịch trình ca trực"
        subtitle="Phân hệ Tài xế FUTA"
        driverCode={syncedDriverCode}
      />

      {/* Main Container tối ưu mobile max-width 500px */}
      <main className="w-full max-w-xl mx-auto px-4 py-4 space-y-4">
        {/* Banner chào mừng tài xế (đồng bộ từ danh sách Tài xế của Admin) */}
        <div className="bg-gradient-to-r from-emerald-700 to-teal-800 rounded-2xl p-4 text-white shadow-sm flex items-center justify-between">
          <div>
            <div className="text-emerald-200 text-xs font-bold uppercase tracking-wider">
              Chúc Bác tài vạn dặm bình an
            </div>
            <h2 className="text-lg font-extrabold m-0 mt-0.5">{syncedDriverName}</h2>
            <p className="text-xs text-emerald-100 mt-1 m-0">
              Vai trò:{" "}
              <span className="font-bold underline">
                {syncedDriverRole === "RELIEF"
                  ? "Tài xế phụ (RELIEF)"
                  : "Tài xế chính (PRIMARY)"}
              </span>
              {driverProfile?.phone ? ` • SĐT: ${driverProfile.phone}` : ""}
              {driverProfile?.licenseNumber ? ` • GPLX: ${driverProfile.licenseNumber}` : ""}
            </p>
          </div>
          <button
            type="button"
            onClick={loadTrips}
            title="Làm mới lịch trình"
            className="w-10 h-10 rounded-xl bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors cursor-pointer"
          >
            <RefreshCw size={18} className={loading ? "animate-spin" : ""} />
          </button>
        </div>

        {/* Thanh trượt ngày cuộn ngang */}
        <div className="bg-white rounded-2xl p-2.5 border border-slate-200 shadow-2xs">
          <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
            {dateTabs.map((tab) => {
              const isSelected = selectedDate === tab.dateStr;
              return (
                <button
                  key={tab.dateStr}
                  type="button"
                  onClick={() => setSelectedDate(tab.dateStr)}
                  className={`flex-1 min-w-[80px] py-2 px-3 rounded-xl text-center transition-all cursor-pointer ${
                    isSelected
                      ? "bg-emerald-600 text-white font-extrabold shadow-sm"
                      : "bg-slate-50 hover:bg-slate-100 text-slate-600 font-semibold"
                  }`}
                >
                  <div className="text-[11px] opacity-80">{tab.label}</div>
                  <div className="text-base leading-tight mt-0.5">{tab.dayNumber}</div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Danh sách thẻ chuyến xe */}
        {loading ? (
          <div className="py-12 text-center text-slate-500 space-y-2">
            <RefreshCw size={28} className="animate-spin mx-auto text-emerald-600" />
            <p className="text-sm font-medium">Đang tải lịch trình ca trực...</p>
          </div>
        ) : error ? (
          <div className="bg-rose-50 border border-rose-200 rounded-2xl p-5 text-center text-rose-700 space-y-3">
            <AlertCircle size={32} className="mx-auto text-rose-500" />
            <p className="text-sm font-semibold m-0">{error}</p>
            <button
              type="button"
              onClick={loadTrips}
              className="px-4 py-2 bg-rose-600 text-white rounded-xl text-xs font-bold hover:bg-rose-700"
            >
              Thử lại
            </button>
          </div>
        ) : trips.length === 0 ? (
          <div className="bg-white rounded-2xl p-8 border border-slate-200 text-center text-slate-500 space-y-3 shadow-2xs">
            <div className="w-14 h-14 rounded-full bg-slate-100 flex items-center justify-center mx-auto text-slate-400">
              <Calendar size={28} />
            </div>
            <h3 className="text-base font-bold text-slate-700 m-0">Không có ca trực nào trong ngày này</h3>
            <p className="text-xs text-slate-500 m-0 max-w-xs mx-auto">
              Trung tâm điều hành chưa phân công chuyến xe cho bạn vào ngày đã chọn. Vui lòng chuyển ngày khác để xem ca trực.
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {trips.map((trip) => {
              const isCompleted = trip.tripStatus === "COMPLETED";
              const isRunning = trip.tripStatus === "RUNNING";

              const depTime = trip.departureAt
                ? new Date(trip.departureAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
                : "--:--";
              const arrTime = trip.arrivalAt
                ? new Date(trip.arrivalAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" })
                : "--:--";

              return (
                <div
                  key={trip.tripCode}
                  className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden transition-all hover:border-slate-300"
                >
                  {/* Header thẻ chuyến */}
                  <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
                    <div className="flex items-center gap-2">
                      <span className="font-extrabold text-sm text-slate-900">{trip.tripCode}</span>
                      <span className="text-xs font-semibold px-2 py-0.5 rounded bg-slate-200/80 text-slate-700">
                        {trip.licensePlate || "Xe FUTA"}
                      </span>
                      <span className="text-[11px] font-bold px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
                        {trip.driverRole === "RELIEF" ? "Tài xế phụ" : "Tài xế chính"}
                      </span>
                    </div>
                    {getStatusBadge(trip.tripStatus)}
                  </div>

                  {/* Thân thẻ chuyến */}
                  <div className="p-4 space-y-3.5">
                    {/* Tuyến đường & Giờ chạy */}
                    <div>
                      <h3 className="text-base font-extrabold text-slate-800 m-0">
                        {trip.routeName || "Chuyến xe liên tỉnh"}
                      </h3>
                      <div className="text-xs text-slate-500 font-medium mt-0.5">
                        Loại xe: <span className="font-bold text-slate-700">{trip.vehicleType || "Giường nằm"}</span> • {trip.capacity || 34} chỗ
                      </div>
                    </div>

                    {/* Timeline xuất bến - đến bến */}
                    <div className="grid grid-cols-2 gap-3 bg-slate-50 rounded-xl p-3 border border-slate-100">
                      <div>
                        <div className="text-[11px] font-bold text-emerald-700 flex items-center gap-1 uppercase">
                          <Clock size={12} /> Giờ xuất bến
                        </div>
                        <div className="text-lg font-black text-slate-800 mt-0.5">{depTime}</div>
                        <div className="text-[11px] text-slate-500 truncate mt-0.5">
                          {trip.stops?.[0]?.stationName || "Bến đầu tuyến"}
                        </div>
                      </div>

                      <div className="border-l border-slate-200 pl-3">
                        <div className="text-[11px] font-bold text-sky-700 flex items-center gap-1 uppercase">
                          <MapPin size={12} /> Giờ dự kiến đến
                        </div>
                        <div className="text-lg font-black text-slate-800 mt-0.5">{arrTime}</div>
                        <div className="text-[11px] text-slate-500 truncate mt-0.5">
                          {trip.stops?.[trip.stops.length - 1]?.stationName || "Bến cuối tuyến"}
                        </div>
                      </div>
                    </div>

                    {/* Nút thao tác vận hành trạng thái */}
                    <div className="flex items-center gap-2">
                      {!isRunning && !isCompleted && (
                        <button
                          type="button"
                          onClick={() => handleStartTrip(trip.tripCode)}
                          className="flex-1 py-2 px-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
                        >
                          <Play size={14} /> Bắt đầu xuất bến
                        </button>
                      )}

                      {isRunning && (
                        <button
                          type="button"
                          onClick={() => handleCompleteTrip(trip.tripCode)}
                          className="flex-1 py-2 px-3 rounded-xl bg-slate-800 hover:bg-slate-900 text-white font-bold text-xs flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
                        >
                          <CheckCircle2 size={14} /> Hoàn thành chuyến xe
                        </button>
                      )}

                      <button
                        type="button"
                        onClick={() =>
                          setSelectedModalTrip({
                            code: trip.tripCode,
                            routeName: trip.routeName,
                            stops: trip.stops || [],
                          })
                        }
                        className="py-2 px-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs flex items-center gap-1 transition-colors cursor-pointer"
                      >
                        <RouteIcon size={14} /> Lộ trình ({trip.stops?.length || 0} trạm)
                      </button>
                    </div>

                    {/* Hai nút chuyển màn hình chính */}
                    <div className="grid grid-cols-2 gap-2.5 pt-1">
                      <Link
                        to={`/driver/trips/${encodeURIComponent(trip.tripCode)}/passengers`}
                        className="py-2.5 px-3 rounded-xl bg-orange-50 hover:bg-orange-100 text-orange-700 font-extrabold text-xs flex items-center justify-center gap-1.5 border border-orange-200 transition-colors text-decoration-none"
                      >
                        <Users size={15} /> Danh sách đón/trả
                      </Link>

                      <Link
                        to={`/driver/trips/${encodeURIComponent(trip.tripCode)}/scan`}
                        className="py-2.5 px-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-xs flex items-center justify-center gap-1.5 transition-colors text-decoration-none shadow-xs"
                      >
                        <QrCode size={15} /> Quét vé QR
                      </Link>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>

      {/* Modal lộ trình trạm dừng */}
      {selectedModalTrip && (
        <RouteStopModal
          isOpen={true}
          onClose={() => setSelectedModalTrip(null)}
          tripCode={selectedModalTrip.code}
          routeName={selectedModalTrip.routeName}
          stops={selectedModalTrip.stops}
        />
      )}
    </div>
  );
}
