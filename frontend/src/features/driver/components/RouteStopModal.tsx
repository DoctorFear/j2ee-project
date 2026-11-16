import React from "react";
import { X, MapPin, Clock, ArrowDown } from "lucide-react";
import type { DriverTripStop } from "../types/driver";

interface RouteStopModalProps {
  isOpen: boolean;
  onClose: () => void;
  tripCode: string;
  routeName: string;
  stops: DriverTripStop[];
}

export const RouteStopModal: React.FC<RouteStopModalProps> = ({
  isOpen,
  onClose,
  tripCode,
  routeName,
  stops,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/50 backdrop-blur-xs p-0 sm:p-4 animate-in fade-in duration-200">
      <div className="bg-white w-full max-w-lg rounded-t-2xl sm:rounded-2xl max-h-[85vh] flex flex-col shadow-2xl overflow-hidden">
        {/* Modal Header */}
        <div className="px-5 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div>
            <div className="text-xs font-bold text-orange-600 uppercase tracking-wider">
              Lộ trình trạm dừng • {tripCode}
            </div>
            <h3 className="text-base font-bold text-slate-800 m-0">{routeName}</h3>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-200/80 hover:bg-slate-300 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* Modal Body - Stepper */}
        <div className="p-5 overflow-y-auto space-y-4">
          {stops.length === 0 ? (
            <p className="text-center text-slate-500 py-6 text-sm">Chưa có thông tin trạm dừng cho chuyến này.</p>
          ) : (
            <div className="relative pl-6 space-y-6 before:absolute before:left-[11px] before:top-2 before:bottom-2 before:w-[2px] before:bg-slate-200">
              {stops.map((stop, idx) => {
                const isFirst = idx === 0;
                const isLast = idx === stops.length - 1;

                return (
                  <div key={stop.stationId || idx} className="relative flex flex-col gap-1">
                    {/* Stepper Dot */}
                    <div
                      className={`absolute -left-[30px] top-0.5 w-5 h-5 rounded-full border-2 flex items-center justify-center text-[10px] font-bold ${
                        isFirst
                          ? "bg-emerald-500 border-emerald-200 text-white"
                          : isLast
                          ? "bg-rose-500 border-rose-200 text-white"
                          : "bg-white border-slate-400 text-slate-600"
                      }`}
                    >
                      {stop.stopOrder}
                    </div>

                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <h4 className="text-sm font-bold text-slate-800 m-0 flex items-center gap-1.5">
                          {stop.stationName}
                          {isFirst && (
                            <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-emerald-50 text-emerald-700">
                              Bến xuất phát
                            </span>
                          )}
                          {isLast && (
                            <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-rose-50 text-rose-700">
                              Bến đích
                            </span>
                          )}
                        </h4>
                        <p className="text-xs text-slate-500 flex items-center gap-1 mt-0.5 m-0">
                          <MapPin size={12} className="shrink-0" /> {stop.stationAddress}
                        </p>
                      </div>

                      {/* Estimated time */}
                      {stop.estimatedTime && (
                        <span className="text-xs font-bold text-slate-700 bg-slate-100 px-2 py-1 rounded-md shrink-0 flex items-center gap-1">
                          <Clock size={12} />
                          {new Date(stop.estimatedTime).toLocaleTimeString("vi-VN", {
                            hour: "2-digit",
                            minute: "2-digit",
                          })}
                        </span>
                      )}
                    </div>

                    {/* Pick-up / Drop-off badges */}
                    <div className="flex items-center gap-2 mt-1">
                      {stop.allowPickup && (
                        <span className="text-[11px] font-semibold px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
                          Đón khách ({stop.pickupCount || 0})
                        </span>
                      )}
                      {stop.allowDropOff && (
                        <span className="text-[11px] font-semibold px-2 py-0.5 rounded bg-sky-50 text-sky-700 border border-sky-200">
                          Trả khách ({stop.dropOffCount || 0})
                        </span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="p-4 border-t border-slate-100 bg-slate-50">
          <button
            type="button"
            onClick={onClose}
            className="w-full py-2.5 rounded-xl bg-slate-800 hover:bg-slate-900 text-white font-bold text-sm transition-colors"
          >
            Đóng bảng lộ trình
          </button>
        </div>
      </div>
    </div>
  );
};
