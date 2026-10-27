import { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import {
  BarChart3,
  Bus,
  BusFront,
  CalendarDays,
  ClipboardList,
  LayoutDashboard,
  LogOut,
  MapPin,
  Route,
  UserRound,
  Users,
  Waypoints,
  X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/features/auth";

import { transportTitles } from "@/features/admin/transport/navigation";
import { useAdminUiStore } from "@/app/stores/admin-ui-store";

const navigation = [
  {
    group: "ĐIỀU HÀNH",
    items: [
      { resource: "trips", icon: CalendarDays },
      { resource: "trip-drivers", icon: ClipboardList },
    ],
  },
  {
    group: "MẠNG LƯỚI VẬN TẢI",
    items: [
      { resource: "routes", icon: Route },
      { resource: "route-stops", icon: Waypoints },
      { resource: "stations", icon: MapPin },
    ],
  },
  {
    group: "NGUỒN LỰC",
    items: [
      { resource: "vehicles", icon: Bus },
      { resource: "drivers", icon: Users },
    ],
  },
] as const;

export function AdminSidebar() {
  const user = useAuthStore((state) => state.user);
  const logoutClient = useAuthStore((state) => state.logoutClient);
  const collapsed = useAdminUiStore((state) => state.collapsed);
  const closeMobile = useAdminUiStore((state) => state.closeMobile);
  const [loggingOut, setLoggingOut] = useState(false);
  const navigate = useNavigate();
  const hidden = collapsed ? "hidden" : "";
  return (
    <aside className="admin-sidebar" aria-label="Điều hướng quản trị">
      <Link to="/admin" onClick={closeMobile} className="admin-brand">
        <span className="admin-brand-icon">
          <BusFront size={23} />
        </span>
        <span className={hidden}>
          <strong>
            FUTA Transport<span className="text-primary">.</span>
          </strong>
          <small>QUẢN TRỊ VẬN TẢI</small>
        </span>
      </Link>
      <Button
        className="admin-mobile-close absolute right-1 top-1"
        size="icon"
        variant="ghost"
        onClick={closeMobile}
        aria-label="Đóng menu"
      >
        <X size={16} />
      </Button>
      <nav id="admin-navigation" className="min-h-0 flex-1 overflow-y-auto" aria-label="Menu quản trị">
        <NavLink end to="/admin" onClick={closeMobile} className="admin-nav-link" title="Tổng quan">
          <LayoutDashboard size={18} />
          <span className={hidden}>Tổng quan</span>
        </NavLink>
        {navigation.map((group) => (
          <div key={group.group}>
            <div className={`admin-nav-group ${hidden}`}>{group.group}</div>
            {group.items.map((item) => (
              <NavLink
                to={`/admin/${item.resource}`}
                key={item.resource}
                onClick={closeMobile}
                className="admin-nav-link"
                title={transportTitles[item.resource]}
              >
                <item.icon size={18} />
                <span className={hidden}>{transportTitles[item.resource]}</span>
              </NavLink>
            ))}
          </div>
        ))}
        <div className={`admin-nav-group ${hidden}`}>BÁO CÁO</div>
        <NavLink to="/admin/trip-statistics" onClick={closeMobile} className="admin-nav-link" title="Thống kê chuyến">
          <BarChart3 size={18} />
          <span className={hidden}>Thống kê chuyến</span>
        </NavLink>
        <div className={`admin-nav-group ${hidden}`}>TÀI KHOẢN</div>
        <NavLink to="/admin/account" onClick={closeMobile} className="admin-nav-link" title="Thông tin tài khoản">
          <UserRound size={18} />
          <span className={hidden}>Thông tin tài khoản</span>
        </NavLink>
      </nav>
      <div className="admin-sidebar-bottom">
        <div className={`mb-4 rounded-lg bg-muted p-3 text-xs text-muted-foreground ${hidden}`}>
          <div className="mb-1 flex items-center gap-2 font-semibold text-foreground">
            <span className="h-1.5 w-1.5 rounded-full bg-primary" />
            Không gian điều hành
          </div>
          Quản lý mạng lưới và nguồn lực vận tải tại một nơi.
        </div>
        <div className="admin-user">
          <span className="admin-avatar">{user?.fullName?.slice(0, 1).toUpperCase() || "A"}</span>
          <div className={`min-w-0 flex-1 ${hidden}`}>
            <strong className="block truncate text-xs">{user?.fullName || user?.username}</strong>
            <span className="text-[11px] text-muted-foreground">Quản trị viên</span>
          </div>
          <Button
            variant="ghost"
            size="icon"
            className={hidden}
            disabled={loggingOut}
            aria-label="Đăng xuất"
            onClick={async () => {
              setLoggingOut(true);
              await logoutClient();
              navigate("/login", { replace: true });
            }}
          >
            <LogOut size={16} />
          </Button>
        </div>
      </div>
    </aside>
  );
}
