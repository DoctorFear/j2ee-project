// Màn hình 2: Danh sách đón/trả khách theo chuỗi trạm dừng (/driver/trips/:code/passengers)
import React, { useEffect, useState, useMemo } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Search,
  Phone,
  QrCode,
  CheckCircle2,
  Clock,
  MapPin,
  RefreshCw,
  AlertCircle,
  Filter,
  Users,
} from "lucide-react";
import { DriverHeader } from "../components/DriverHeader";
import { driverApi } from "../api/driverApi";
import type { DriverPassenger, DriverTripSummary } from "../types/driver";

export default function DriverPassengersPage() {
  const { code: tripCode } = useParams<{ code: string }>();
  const navigate = useNavigate();

  const [summary, setSummary] = useState<DriverTripSummary | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [activeTab, setActiveTab] = useState<"ALL" | "PICKUP" | "DROPOFF">("ALL");

  const loadData = async () => {
    if (!tripCode) return;
    setLoading(true);
    setError(null);
    try {
      const data = await driverApi.getSummary(tripCode);
      setSummary(data);
    } catch (err: any) {
      console.error("Lỗi khi tải danh sách hành khách:", err);
      setError("Không thể tải danh sách hành khách của chuyến này.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [tripCode]);

  // Xử lý đổi trạng thái 1 chạm (Toggle boarded)
  const handleToggle = async (ticketCode: string) => {
    if (!tripCode) return;
    try {
      const updated = await driverApi.toggleBoarded(tripCode, ticketCode);
      // Cập nhật state trực tiếp để UI phản hồi mượt mà
      setSummary((prev) => {
        if (!prev) return null;
        const newPassengers = prev.passengers.map((p) =>
          p.ticketCode === ticketCode ? { ...p, boarded: updated.boarded, boardedAt: updated.boardedAt } : p
        );
        const newBoardedCount = newPassengers.filter((p) => p.boarded).length;
        return {
          ...prev,
          totalBoarded: newBoardedCount,
          passengers: newPassengers,
        };
      });
    } catch (err: any) {
      alert("Lỗi khi cập nhật trạng thái hành khách: " + err.message);
    }
  };

  // Lọc hành khách theo tìm kiếm và tab
  const filteredPassengers = useMemo(() => {
    if (!summary?.passengers) return [];
    const query = searchQuery.trim().toLowerCase();

    return summary.passengers.filter((p) => {
      // Lọc theo từ khóa tìm kiếm (tên, ghế, sđt, mã vé)
      const matchQuery =
        !query ||
        p.customerName.toLowerCase().includes(query) ||
        p.seatCode.toLowerCase().includes(query) ||
        p.customerPhone.includes(query) ||
        p.ticketCode.toLowerCase().includes(query);

      return matchQuery;
    });
  }, [summary, searchQuery]);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col pb-12 font-sans antialiased">
      {/* Header riêng tài xế */}
      <DriverHeader title="Danh sách đón/trả" subtitle={`Chuyến ${tripCode}`} />

      {/* Main Container */}
      <main className="w-full max-w-xl mx-auto px-4 py-3 space-y-3.5">
        {/* Thanh công cụ: Nút quay lại & Nút chuyển sang Quét QR */}
        <div className="flex items-center justify-between">
          <Link
            to="/driver/trips"
            className="flex items-center gap-1.5 text-xs font-bold text-slate-600 hover:text-slate-900 bg-white px-3 py-1.5 rounded-xl border border-slate-200 transition-colors text-decoration-none"
          >
            <ArrowLeft size={14} /> Ca trực
          </Link>

          <Link
            to={`/driver/trips/${encodeURIComponent(tripCode || "")}/scan`}
            className="flex items-center gap-1.5 text-xs font-extrabold text-white bg-emerald-600 hover:bg-emerald-700 px-3.5 py-1.5 rounded-xl shadow-xs transition-colors text-decoration-none"
          >
            <QrCode size={15} /> Quét vé QR
          </Link>
        </div>

        {/* Thanh tiến độ lên xe */}
        {summary && (
          <div className="bg-white rounded-2xl p-4 border border-slate-200 shadow-2xs space-y-2">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-xs font-bold text-slate-500 uppercase">Tiến độ khách lên xe</span>
                <div className="text-xl font-black text-slate-900 mt-0.5">
                  {summary.totalBoarded} / {summary.totalPassengers}{" "}
                  <span className="text-xs font-bold text-slate-500">hành khách</span>
                </div>
              </div>
              <button
                type="button"
                onClick={loadData}
                title="Tải lại danh sách"
                className="w-9 h-9 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors cursor-pointer"
              >
                <RefreshCw size={16} className={loading ? "animate-spin" : ""} />
              </button>
            </div>

            {/* Thanh Progress bar */}
            <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
              <div
                className="h-full bg-emerald-500 transition-all duration-300"
                style={{
                  width: `${
                    summary.totalPassengers > 0
                      ? Math.round((summary.totalBoarded / summary.totalPassengers) * 100)
                      : 0
                  }%`,
                }}
              ></div>
            </div>
          </div>
        )}

        {/* Ô tìm kiếm tức thì */}
        <div className="relative">
          <Search size={18} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Tìm tên khách, số ghế (A03, B11), SĐT, mã vé..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-white border border-slate-200 text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-emerald-500 shadow-2xs"
          />
        </div>

        {/* Bộ lọc tab */}
        <div className="flex items-center gap-1.5 bg-slate-200/70 p-1 rounded-xl">
          <button
            type="button"
            onClick={() => setActiveTab("ALL")}
            className={`flex-1 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              activeTab === "ALL" ? "bg-white text-slate-900 shadow-2xs" : "text-slate-600 hover:text-slate-900"
            }`}
          >
            Tất cả ({summary?.totalPassengers || 0})
          </button>
          <button
            type="button"
            onClick={() => setActiveTab("PICKUP")}
            className={`flex-1 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              activeTab === "PICKUP" ? "bg-white text-emerald-700 shadow-2xs" : "text-slate-600 hover:text-slate-900"
            }`}
          >
            Chờ lên xe ({summary ? summary.totalPassengers - summary.totalBoarded : 0})
          </button>
          <button
            type="button"
            onClick={() => setActiveTab("DROPOFF")}
            className={`flex-1 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer ${
              activeTab === "DROPOFF" ? "bg-white text-sky-700 shadow-2xs" : "text-slate-600 hover:text-slate-900"
            }`}
          >
            Đã lên xe ({summary?.totalBoarded || 0})
          </button>
        </div>

        {/* Danh sách hành khách */}
        {loading ? (
          <div className="py-12 text-center text-slate-500 space-y-2">
            <RefreshCw size={26} className="animate-spin mx-auto text-emerald-600" />
            <p className="text-sm font-medium">Đang tải danh sách đón trả khách...</p>
          </div>
        ) : error ? (
          <div className="bg-rose-50 border border-rose-200 rounded-2xl p-5 text-center text-rose-700 space-y-2">
            <AlertCircle size={28} className="mx-auto text-rose-500" />
            <p className="text-sm font-semibold m-0">{error}</p>
          </div>
        ) : filteredPassengers.length === 0 ? (
          <div className="bg-white rounded-2xl p-8 border border-slate-200 text-center text-slate-500 space-y-2 shadow-2xs">
            <Users size={32} className="mx-auto text-slate-400" />
            <h4 className="text-sm font-bold text-slate-700 m-0">Không tìm thấy hành khách phù hợp</h4>
            <p className="text-xs text-slate-500 m-0">Thử tìm kiếm bằng từ khóa khác hoặc xóa bộ lọc.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {filteredPassengers
              .filter((p) => {
                if (activeTab === "PICKUP") return !p.boarded;
                if (activeTab === "DROPOFF") return p.boarded;
                return true;
              })
              .map((passenger) => {
                const isBoarded = passenger.boarded;
                const isFloor2 = passenger.seatCode.startsWith("B");

                return (
                  <div
                    key={passenger.ticketCode}
                    className={`bg-white rounded-2xl border p-4 shadow-2xs transition-all ${
                      isBoarded ? "border-emerald-200 bg-emerald-50/20" : "border-slate-200 hover:border-slate-300"
                    }`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      {/* Huy hiệu số ghế cỡ lớn */}
                      <div
                        className={`w-14 h-14 rounded-xl flex flex-col items-center justify-center shrink-0 border-2 font-black ${
                          isFloor2
                            ? "bg-purple-50 border-purple-200 text-purple-700"
                            : "bg-amber-50 border-amber-200 text-amber-700"
                        }`}
                      >
                        <span className="text-lg leading-none">{passenger.seatCode}</span>
                        <span className="text-[9px] font-bold mt-0.5 opacity-80">
                          {isFloor2 ? "TẦNG TRÊN" : "TẦNG DƯỚI"}
                        </span>
                      </div>

                      {/* Thông tin khách hàng */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <h4 className="text-base font-extrabold text-slate-900 m-0 truncate">
                            {passenger.customerName}
                          </h4>
                          {isBoarded ? (
                            <span className="text-[10px] font-extrabold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 shrink-0">
                              Đã lên xe
                            </span>
                          ) : (
                            <span className="text-[10px] font-extrabold px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 shrink-0">
                              Chờ đón
                            </span>
                          )}
                        </div>

                        <div className="text-xs text-slate-500 font-medium mt-1 flex items-center gap-2">
                          <span>Mã: <strong className="text-slate-700">{passenger.ticketCode}</strong></span>
                        </div>

                        {passenger.customerPhone && (
                          <div className="text-xs text-slate-600 font-semibold mt-1">
                            SĐT: {passenger.customerPhone}
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Hàng nút thao tác nhanh: Gọi điện & Đổi trạng thái */}
                    <div className="flex items-center gap-2 mt-3 pt-3 border-t border-slate-100">
                      {passenger.customerPhone ? (
                        <a
                          href={`tel:${passenger.customerPhone}`}
                          className="flex-1 py-2 px-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs flex items-center justify-center gap-1.5 transition-colors text-decoration-none"
                        >
                          <Phone size={14} className="text-emerald-600" /> Gọi khách
                        </a>
                      ) : (
                        <button
                          type="button"
                          disabled
                          className="flex-1 py-2 px-3 rounded-xl bg-slate-50 text-slate-400 font-bold text-xs flex items-center justify-center gap-1.5 opacity-50 cursor-not-allowed"
                        >
                          <Phone size={14} /> Không có SĐT
                        </button>
                      )}

                      <button
                        type="button"
                        onClick={() => handleToggle(passenger.ticketCode)}
                        className={`flex-1 py-2 px-3 rounded-xl font-bold text-xs flex items-center justify-center gap-1.5 transition-all cursor-pointer ${
                          isBoarded
                            ? "bg-slate-100 hover:bg-rose-50 text-slate-600 hover:text-rose-600 border border-slate-200 hover:border-rose-200"
                            : "bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs"
                        }`}
                      >
                        <CheckCircle2 size={14} />
                        {isBoarded ? "Hủy soát vé" : "Soát vé (Lên xe)"}
                      </button>
                    </div>
                  </div>
                );
              })}
          </div>
        )}
      </main>
    </div>
  );
}
