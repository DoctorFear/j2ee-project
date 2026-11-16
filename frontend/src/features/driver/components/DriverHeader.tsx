import React from "react";
import { useNavigate } from "react-router-dom";
import { Truck, LogOut, Bell, ShieldCheck } from "lucide-react";
import { useUser } from "@/features/auth";

interface DriverHeaderProps {
  title?: string;
  subtitle?: string;
  driverCode?: string;
}

export const DriverHeader: React.FC<DriverHeaderProps> = ({
  title = "FUTA Driver",
  subtitle = "Ca trực & Soát vé",
  driverCode,
}) => {
  const { user, logoutClient } = useUser();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logoutClient();
    } finally {
      navigate("/login");
    }
  };

  const employeeCode =
    driverCode || (user?.id ? `TX-${String(user.id).padStart(4, "0")}` : "TX-0001");

  return (
    <header className="sticky top-0 z-30 bg-white border-b border-slate-200 px-4 py-3 shadow-xs">
      <div className="max-w-xl mx-auto flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 text-white flex items-center justify-center shadow-xs">
            <Truck size={20} />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-base font-bold text-slate-800 leading-tight m-0">{title}</h1>
              <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center gap-1">
                <ShieldCheck size={11} /> {employeeCode}
              </span>
            </div>
            <p className="text-xs text-orange-600 font-semibold m-0">{subtitle}</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            title="Thông báo điều hành"
            onClick={() => alert("Hiện không có thông báo khẩn từ trung tâm điều hành.")}
            className="w-9 h-9 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors cursor-pointer relative"
          >
            <Bell size={18} />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-orange-500 ring-2 ring-white"></span>
          </button>

          <button
            type="button"
            onClick={handleLogout}
            title="Đăng xuất"
            className="w-9 h-9 rounded-full bg-rose-50 hover:bg-rose-100 text-rose-600 flex items-center justify-center transition-colors cursor-pointer border border-rose-200"
          >
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </header>
  );
};
